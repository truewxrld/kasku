@extends('layouts.app')
@section('title', 'Monthly report')
@section('content')
@php
    $change = function (float $now, float $before) {
        return $before > 0 ? (($now - $before) / $before) * 100 : null;
    };
    $rows = [
        'Income' => [$totals['income'], $previous['income']],
        'Expense' => [$totals['expense'], $previous['expense']],
        'Net' => [$totals['net'], $previous['net']],
    ];
@endphp

<div class="page-head">
    <div><p class="eyebrow">{{ $monthLabel }}</p><h1>Monthly report</h1></div>
    <form method="GET" action="{{ route('reports.index') }}" class="inline-form">
        <input type="month" name="month" value="{{ $month }}" onchange="this.form.submit()">
        <noscript><button type="submit" class="btn small">Show</button></noscript>
    </form>
</div>

<div class="stats">
    <div class="stat"><span>Income</span><strong class="pos">{{ \App\Support\Money::rp($totals['income']) }}</strong></div>
    <div class="stat"><span>Expense</span><strong class="neg">{{ \App\Support\Money::rp($totals['expense']) }}</strong></div>
    <div class="stat"><span>Net</span><strong class="{{ $totals['net'] >= 0 ? 'pos' : 'neg' }}">{{ \App\Support\Money::rp($totals['net']) }}</strong></div>
    <div class="stat"><span>Savings rate</span><strong>{{ $savingsRate === null ? '-' : number_format($savingsRate, 0) . '%' }}</strong></div>
</div>

<section class="card">
    <h2>{{ $monthLabel }} vs {{ $prevLabel }}</h2>
    <div class="table-wrap">
        <table>
            <thead><tr><th></th><th class="num">{{ $monthLabel }}</th><th class="num">{{ $prevLabel }}</th><th class="num">Change</th></tr></thead>
            <tbody>
            @foreach ($rows as $label => [$now, $before])
                @php $delta = $change((float) $now, (float) $before); @endphp
                <tr>
                    <td>{{ $label }}</td>
                    <td class="num">{{ \App\Support\Money::rp($now) }}</td>
                    <td class="num">{{ \App\Support\Money::rp($before) }}</td>
                    <td class="num">{{ $delta === null ? '-' : ($delta >= 0 ? '+' : '') . number_format($delta, 1) . '%' }}</td>
                </tr>
            @endforeach
            </tbody>
        </table>
    </div>
</section>

<div class="grid-2">
    @foreach (['Expenses by category' => [$expenseBreakdown, $totals['expense']], 'Income by category' => [$incomeBreakdown, $totals['income']]] as $title => [$list, $sum])
        <section class="card">
            <h2>{{ $title }}</h2>
            @forelse ($list as $row)
                @php $share = $sum > 0 ? ($row->total / $sum) * 100 : 0; @endphp
                <div class="budget-row">
                    <div class="budget-head">
                        <strong><span class="dot" style="background: {{ $row->color }}"></span>{{ $row->name }}</strong>
                        <span>{{ \App\Support\Money::rp($row->total) }} &middot; {{ number_format($share, 0) }}%</span>
                    </div>
                    <div class="progress"><div class="fill" style="width: {{ $share }}%; background: {{ $row->color }}"></div></div>
                </div>
            @empty
                <p class="empty">No data for this month.</p>
            @endforelse
        </section>
    @endforeach
</div>
@endsection
