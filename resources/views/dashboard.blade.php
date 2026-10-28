@extends('layouts.app')
@section('title', 'Dashboard')
@section('content')
<div class="page-head">
    <div>
        <p class="eyebrow">{{ \Carbon\Carbon::parse($month . '-01')->format('F Y') }}</p>
        <h1>Dashboard</h1>
    </div>
    <a class="btn" href="{{ route('transactions.create') }}">+ Add transaction</a>
</div>

<div class="stats">
    <div class="stat"><span>Total balance</span><strong>{{ \App\Support\Money::rp($totalBalance) }}</strong></div>
    <div class="stat"><span>Income this month</span><strong class="pos">{{ \App\Support\Money::rp($totals['income']) }}</strong></div>
    <div class="stat"><span>Expense this month</span><strong class="neg">{{ \App\Support\Money::rp($totals['expense']) }}</strong></div>
    <div class="stat"><span>Net this month</span><strong class="{{ $totals['net'] >= 0 ? 'pos' : 'neg' }}">{{ \App\Support\Money::rp($totals['net']) }}</strong></div>
</div>

<div class="grid-2">
    <section class="card">
        <div class="card-head"><h2>Budgets</h2><a href="{{ route('budgets.index', ['month' => $month]) }}">Manage</a></div>
        @forelse ($budgets as $item)
            <div class="budget-row">
                <strong><span class="dot" style="background: {{ $item->budget->category->color }}"></span>{{ $item->budget->category->name }}</strong>
                @include('budgets._bar', ['item' => $item])
            </div>
        @empty
            <p class="empty">No budgets for this month. <a href="{{ route('budgets.create') }}">Create one</a>.</p>
        @endforelse
    </section>

    <section class="card">
        <div class="card-head"><h2>Recent transactions</h2><a href="{{ route('transactions.index') }}">View all</a></div>
        @include('transactions._table', ['rows' => $recent, 'actions' => false])
    </section>
</div>
@endsection
