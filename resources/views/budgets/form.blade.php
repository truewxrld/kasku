@extends('layouts.app')
@section('title', $budget->exists ? 'Edit budget' : 'New budget')
@section('content')
<div class="card narrow">
    <h1>{{ $budget->exists ? 'Edit budget' : 'New budget' }}</h1>
    <form method="POST" action="{{ $budget->exists ? route('budgets.update', $budget) : route('budgets.store') }}">
        @csrf
        @if ($budget->exists)
            @method('PUT')
        @endif

        <label>Expense category
            <select name="category_id" required>
                <option value="">-- choose --</option>
                @foreach ($categories as $c)
                    <option value="{{ $c->id }}" @selected((int) old('category_id', $budget->category_id) === $c->id)>{{ $c->name }}</option>
                @endforeach
            </select>
            @error('category_id')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Month
            <input type="month" name="month" value="{{ old('month', $budget->month) }}" required>
            @error('month')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Spending limit (Rp)
            <input type="number" name="amount_limit" step="any" min="0" value="{{ old('amount_limit', $budget->amount_limit) }}" required>
            @error('amount_limit')<small class="err">{{ $message }}</small>@enderror
        </label>

        <div class="form-actions">
            <button type="submit" class="btn">Save</button>
            <a class="btn ghost" href="{{ route('budgets.index') }}">Cancel</a>
        </div>
    </form>
</div>
@endsection
