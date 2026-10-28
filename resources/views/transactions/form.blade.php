@extends('layouts.app')
@section('title', $transaction->exists ? 'Edit transaction' : 'New transaction')
@section('content')
<div class="card narrow">
    <h1>{{ $transaction->exists ? 'Edit transaction' : 'New transaction' }}</h1>

    @if ($wallets->isEmpty())
        <p class="alert bad">You need at least one wallet first. <a href="{{ route('wallets.create') }}">Create a wallet</a>.</p>
    @endif

    <form method="POST" action="{{ $transaction->exists ? route('transactions.update', $transaction) : route('transactions.store') }}">
        @csrf
        @if ($transaction->exists)
            @method('PUT')
        @endif

        <label>Wallet
            <select name="wallet_id" required>
                <option value="">-- choose --</option>
                @foreach ($wallets as $w)
                    <option value="{{ $w->id }}" @selected((int) old('wallet_id', $transaction->wallet_id) === $w->id)>{{ $w->name }}</option>
                @endforeach
            </select>
            @error('wallet_id')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Category
            <select name="category_id" required>
                <option value="">-- choose --</option>
                @foreach (['expense', 'income'] as $type)
                    <optgroup label="{{ ucfirst($type) }}">
                        @foreach ($categories->where('type', $type) as $c)
                            <option value="{{ $c->id }}" @selected((int) old('category_id', $transaction->category_id) === $c->id)>{{ $c->name }}</option>
                        @endforeach
                    </optgroup>
                @endforeach
            </select>
            @error('category_id')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Amount (Rp)
            <input type="number" name="amount" step="any" min="0" value="{{ old('amount', $transaction->amount) }}" required>
            @error('amount')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Date
            <input type="date" name="transaction_date" value="{{ old('transaction_date', $transaction->transaction_date?->format('Y-m-d')) }}" required>
            @error('transaction_date')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Note (optional)
            <input type="text" name="note" maxlength="255" value="{{ old('note', $transaction->note) }}">
            @error('note')<small class="err">{{ $message }}</small>@enderror
        </label>

        <div class="form-actions">
            <button type="submit" class="btn">Save</button>
            <a class="btn ghost" href="{{ route('transactions.index') }}">Cancel</a>
        </div>
    </form>
</div>
@endsection
