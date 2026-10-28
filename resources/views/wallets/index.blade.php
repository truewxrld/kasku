@extends('layouts.app')
@section('title', 'Wallets')
@section('content')
<div class="page-head">
    <div><p class="eyebrow">Where your money lives</p><h1>Wallets</h1></div>
    <a class="btn" href="{{ route('wallets.create') }}">+ New wallet</a>
</div>

<div class="card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr><th>Name</th><th>Type</th><th class="num">Initial balance</th><th class="num">Current balance</th><th></th></tr>
            </thead>
            <tbody>
            @forelse ($wallets as $wallet)
                @php $balance = (float) $wallet->initial_balance + ($net[$wallet->id] ?? 0); @endphp
                <tr>
                    <td><strong>{{ $wallet->name }}</strong></td>
                    <td>{{ \App\Models\Wallet::TYPES[$wallet->type] ?? $wallet->type }}</td>
                    <td class="num">{{ \App\Support\Money::rp($wallet->initial_balance) }}</td>
                    <td class="num {{ $balance >= 0 ? 'pos' : 'neg' }}">{{ \App\Support\Money::rp($balance) }}</td>
                    <td class="actions">
                        <a href="{{ route('wallets.edit', $wallet) }}">Edit</a>
                        <form method="POST" action="{{ route('wallets.destroy', $wallet) }}" onsubmit="return confirm('Delete this wallet?')">
                            @csrf
                            @method('DELETE')
                            <button type="submit" class="link danger">Delete</button>
                        </form>
                    </td>
                </tr>
            @empty
                <tr><td colspan="5" class="empty">No wallets yet.</td></tr>
            @endforelse
            </tbody>
        </table>
    </div>
</div>
@endsection
