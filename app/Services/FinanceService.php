<?php

namespace App\Services;

use App\Models\Budget;
use Carbon\Carbon;
use Illuminate\Support\Collection;
use Illuminate\Support\Facades\DB;

/**
 * Aggregation queries shared by the dashboard, budgets and reports.
 * A transaction has no "type" column: income/expense comes from its category.
 */
class FinanceService
{
    /** @return array{0: Carbon, 1: Carbon, 2: string} [first day, last day, "YYYY-MM"] */
    public function monthRange(?string $month): array
    {
        $start = ($month && preg_match('/^\d{4}-(0[1-9]|1[0-2])$/', $month))
            ? Carbon::parse($month . '-01')->startOfDay()
            : now()->startOfMonth();

        return [$start->copy(), $start->copy()->endOfMonth(), $start->format('Y-m')];
    }

    /** @return array{income: float, expense: float, net: float} */
    public function periodTotals(int $userId, string $from, string $to): array
    {
        $row = DB::table('transactions')
            ->join('categories', 'categories.id', '=', 'transactions.category_id')
            ->where('transactions.user_id', $userId)
            ->whereDate('transactions.transaction_date', '>=', $from)
            ->whereDate('transactions.transaction_date', '<=', $to)
            ->selectRaw("COALESCE(SUM(CASE WHEN categories.type = 'income' THEN transactions.amount ELSE 0 END), 0) AS income")
            ->selectRaw("COALESCE(SUM(CASE WHEN categories.type = 'expense' THEN transactions.amount ELSE 0 END), 0) AS expense")
            ->first();

        $income = (float) $row->income;
        $expense = (float) $row->expense;

        return ['income' => $income, 'expense' => $expense, 'net' => $income - $expense];
    }

    /** Net movement (income - expense) of every wallet, keyed by wallet id. */
    public function walletBalances(int $userId): Collection
    {
        return DB::table('transactions')
            ->join('categories', 'categories.id', '=', 'transactions.category_id')
            ->where('transactions.user_id', $userId)
            ->groupBy('transactions.wallet_id')
            ->selectRaw('transactions.wallet_id AS wallet_id')
            ->selectRaw("COALESCE(SUM(CASE WHEN categories.type = 'income' THEN transactions.amount ELSE -transactions.amount END), 0) AS net")
            ->pluck('net', 'wallet_id')
            ->map(fn ($value) => (float) $value);
    }

    /** Total per category for one type ('income' or 'expense'), largest first. */
    public function breakdownByCategory(int $userId, string $from, string $to, string $type): Collection
    {
        return DB::table('transactions')
            ->join('categories', 'categories.id', '=', 'transactions.category_id')
            ->where('transactions.user_id', $userId)
            ->where('categories.type', $type)
            ->whereDate('transactions.transaction_date', '>=', $from)
            ->whereDate('transactions.transaction_date', '<=', $to)
            ->groupBy('categories.id', 'categories.name', 'categories.color')
            ->select('categories.id', 'categories.name', 'categories.color')
            ->selectRaw('SUM(transactions.amount) AS total')
            ->orderByDesc('total')
            ->get();
    }

    /** Budgets of a month with the amount already spent in each category. */
    public function budgetProgress(int $userId, string $month): Collection
    {
        [$from, $to] = $this->monthRange($month);

        $spent = DB::table('transactions')
            ->join('categories', 'categories.id', '=', 'transactions.category_id')
            ->where('transactions.user_id', $userId)
            ->where('categories.type', 'expense')
            ->whereDate('transactions.transaction_date', '>=', $from->toDateString())
            ->whereDate('transactions.transaction_date', '<=', $to->toDateString())
            ->groupBy('transactions.category_id')
            ->selectRaw('transactions.category_id AS category_id')
            ->selectRaw('SUM(transactions.amount) AS spent')
            ->pluck('spent', 'category_id');

        return Budget::with('category')
            ->where('user_id', $userId)
            ->where('month', $month)
            ->get()
            ->map(function (Budget $budget) use ($spent) {
                $limit = (float) $budget->amount_limit;
                $used = (float) ($spent[$budget->category_id] ?? 0);

                return (object) [
                    'budget' => $budget,
                    'limit' => $limit,
                    'spent' => $used,
                    'remaining' => $limit - $used,
                    'percent' => $limit > 0 ? ($used / $limit) * 100 : 0,
                ];
            });
    }
}
