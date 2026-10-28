<?php

namespace App\Http\Controllers;

use App\Services\FinanceService;
use Illuminate\Http\Request;
use Illuminate\View\View;

class ReportController extends Controller
{
    public function index(Request $request, FinanceService $finance): View
    {
        $userId = $request->user()->id;

        [$from, $to, $month] = $finance->monthRange($request->input('month'));
        $totals = $finance->periodTotals($userId, $from->toDateString(), $to->toDateString());

        $prevStart = $from->copy()->subMonth()->startOfMonth();
        $prevEnd = $prevStart->copy()->endOfMonth();
        $previous = $finance->periodTotals($userId, $prevStart->toDateString(), $prevEnd->toDateString());

        $expenseBreakdown = $finance->breakdownByCategory($userId, $from->toDateString(), $to->toDateString(), 'expense');
        $incomeBreakdown = $finance->breakdownByCategory($userId, $from->toDateString(), $to->toDateString(), 'income');

        $savingsRate = $totals['income'] > 0 ? ($totals['net'] / $totals['income']) * 100 : null;

        return view('reports.index', [
            'month' => $month,
            'monthLabel' => $from->format('F Y'),
            'prevLabel' => $prevStart->format('F Y'),
            'totals' => $totals,
            'previous' => $previous,
            'expenseBreakdown' => $expenseBreakdown,
            'incomeBreakdown' => $incomeBreakdown,
            'savingsRate' => $savingsRate,
        ]);
    }
}
