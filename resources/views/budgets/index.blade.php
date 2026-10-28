@extends('layouts.app')
@section('title', 'Budgets')
@section('content')
<div class="page-head">
    <div><p class="eyebrow">Spending limits per category</p><h1>Budgets</h1></div>
    <a class="btn" href="{{ route('budgets.create', ['month' => $month]) }}">+ New budget</a>
</div>

<form method="GET" action="{{ route('budgets.index') }}" class="card filters">
    <label class="inline">Month
        <input type="month" name="month" value="{{ $month }}" onchange="this.form.submit()">
    </label>
    <noscript><button type="submit" class="btn">Show</button></noscript>
</form>

<div class="card">
    @forelse ($items as $item)
        <div class="budget-row">
            <div class="budget-head">
                <strong><span class="dot" style="background: {{ $item->budget->category->color }}"></span>{{ $item->budget->category->name }}</strong>
                <span class="actions">
                    <a href="{{ route('budgets.edit', $item->budget) }}">Edit</a>
                    <form method="POST" action="{{ route('budgets.destroy', $item->budget) }}" onsubmit="return confirm('Delete this budget?')">
                        @csrf
                        @method('DELETE')
                        <button type="submit" class="link danger">Delete</button>
                    </form>
                </span>
            </div>
            @include('budgets._bar', ['item' => $item])
        </div>
    @empty
        <p class="empty">No budgets for this month yet.</p>
    @endforelse
</div>
@endsection
