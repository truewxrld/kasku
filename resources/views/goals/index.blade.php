@extends('layouts.app')
@section('title', 'Savings goals')
@section('content')
<div class="page-head">
    <div><p class="eyebrow">Save for what matters</p><h1>Savings goals</h1></div>
    <a class="btn" href="{{ route('goals.create') }}">+ New goal</a>
</div>

<div class="grid-2">
    @forelse ($goals as $goal)
        <section class="card">
            <div class="budget-head">
                <h2>{{ $goal->name }}</h2>
                <span class="actions">
                    <a href="{{ route('goals.edit', $goal) }}">Edit</a>
                    <form method="POST" action="{{ route('goals.destroy', $goal) }}" onsubmit="return confirm('Delete this goal?')">
                        @csrf
                        @method('DELETE')
                        <button type="submit" class="link danger">Delete</button>
                    </form>
                </span>
            </div>

            <div class="progress"><div class="fill {{ $goal->isReached() ? 'ok' : 'warn' }}" style="width: {{ $goal->percent() }}%"></div></div>
            <div class="progress-meta">
                <span>{{ \App\Support\Money::rp($goal->saved_amount) }} of {{ \App\Support\Money::rp($goal->target_amount) }} ({{ number_format($goal->percent(), 0) }}%)</span>
                @if ($goal->isReached())
                    <span class="pos">Goal reached</span>
                @elseif ($goal->deadline)
                    <span class="muted">Due {{ $goal->deadline->format('d M Y') }}</span>
                @endif
            </div>

            @unless ($goal->isReached())
                <form method="POST" action="{{ route('goals.contribute', $goal) }}" class="inline-form">
                    @csrf
                    <input type="number" name="amount" step="any" min="0" placeholder="Amount to add (Rp)" required>
                    <button type="submit" class="btn small">Add savings</button>
                </form>
            @endunless
        </section>
    @empty
        <p class="empty">No savings goals yet.</p>
    @endforelse
</div>
@endsection
