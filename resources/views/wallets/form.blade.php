@extends('layouts.app')
@section('title', $wallet->exists ? 'Edit wallet' : 'New wallet')
@section('content')
<div class="card narrow">
    <h1>{{ $wallet->exists ? 'Edit wallet' : 'New wallet' }}</h1>
    <form method="POST" action="{{ $wallet->exists ? route('wallets.update', $wallet) : route('wallets.store') }}">
        @csrf
        @if ($wallet->exists)
            @method('PUT')
        @endif

        <label>Name
            <input type="text" name="name" value="{{ old('name', $wallet->name) }}" required>
            @error('name')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Type
            <select name="type">
                @foreach (\App\Models\Wallet::TYPES as $value => $label)
                    <option value="{{ $value }}" @selected(old('type', $wallet->type) === $value)>{{ $label }}</option>
                @endforeach
            </select>
            @error('type')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Initial balance (Rp)
            <input type="number" name="initial_balance" step="any" min="0" value="{{ old('initial_balance', $wallet->initial_balance) }}" required>
            @error('initial_balance')<small class="err">{{ $message }}</small>@enderror
        </label>

        <div class="form-actions">
            <button type="submit" class="btn">Save</button>
            <a class="btn ghost" href="{{ route('wallets.index') }}">Cancel</a>
        </div>
    </form>
</div>
@endsection
