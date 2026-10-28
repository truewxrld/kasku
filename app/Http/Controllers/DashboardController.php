<?php

namespace App\Http\Controllers;

use App\Models\Transaction;
use App\Models\Wallet;
use App\Services\FinanceService;
use Illuminate\Http\Request;
use Illuminate\View\View;

class DashboardController extends Controller
{
    public function __invoke(Request $request, FinanceService $finance): View
    {
        $userId = $request->user()->id;

        [$from, $to, $month] = $finance->monthRange(null);
        $totals = $finance->periodTotals($userId, $from->toDateString(), $to->toDateString());

        $net = $finance->walletBalances($userId);
        $totalBalance = Wallet::where('user_id', $userId)->get()
            ->sum(fn (Wallet $w) => (float) $w->initial_balance + ($net[$w->id] ?? 0));

        $recent = Transaction::with(['wallet', 'category'])
            ->where('user_id', $userId)
            ->orderByDesc('transaction_date')
            ->orderByDesc('id')
            ->limit(5)
            ->get();

        $budgets = $finance->budgetProgress($userId, $month);

        return view('dashboard', compact('totals', 'totalBalance', 'recent', 'budgets', 'month'));
    }
}
