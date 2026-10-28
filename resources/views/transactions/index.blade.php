@extends('layouts.app')
@section('title', 'Transactions')
@section('content')
<div class="page-head">
    <div><p class="eyebrow">{{ $transactions->total() }} record(s)</p><h1>Transactions</h1></div>
    <a class="btn" href="{{ route('transactions.create') }}">+ Add transaction</a>
</div>

<form method="GET" action="{{ route('transactions.index') }}" class="card filters">
    <input type="search" name="q" value="{{ request('q') }}" placeholder="Search notes...">
    <select name="type">
        <option value="">All types</option>
        <option value="income" @selected(request('type') === 'income')>Income</option>
        <option value="expense" @selected(request('type') === 'expense')>Expense</option>
    </select>
    <select name="wallet_id">
        <option value="">All wallets</option>
        @foreach ($wallets as $w)
            <option value="{{ $w->id }}" @selected((string) request('wallet_id') === (string) $w->id)>{{ $w->name }}</option>
        @endforeach
    </select>
    <select name="category_id">
        <option value="">All categories</option>
        @foreach ($categories as $c)
            <option value="{{ $c->id }}" @selected((string) request('category_id') === (string) $c->id)>{{ $c->name }} ({{ $c->type }})</option>
        @endforeach
    </select>
    <input type="date" name="from" value="{{ request('from') }}" title="From">
    <input type="date" name="to" value="{{ request('to') }}" title="To">
    <button type="submit" class="btn">Filter</button>
    <a class="btn ghost" href="{{ route('transactions.index') }}">Reset</a>
    <a class="btn ghost" href="{{ route('transactions.export', request()->query()) }}">Export CSV</a>
</form>

<div class="card">
    @include('transactions._table', ['rows' => $transactions, 'actions' => true])
    {{ $transactions->links('pagination.kasku') }}
</div>
@endsection
