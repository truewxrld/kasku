@extends('layouts.app')
@section('title', $goal->exists ? 'Edit goal' : 'New goal')
@section('content')
<div class="card narrow">
    <h1>{{ $goal->exists ? 'Edit goal' : 'New goal' }}</h1>
    <form method="POST" action="{{ $goal->exists ? route('goals.update', $goal) : route('goals.store') }}">
        @csrf
        @if ($goal->exists)
            @method('PUT')
        @endif

        <label>Goal name
            <input type="text" name="name" value="{{ old('name', $goal->name) }}" required>
            @error('name')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Target amount (Rp)
            <input type="number" name="target_amount" step="any" min="0" value="{{ old('target_amount', $goal->target_amount) }}" required>
            @error('target_amount')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Already saved (Rp)
            <input type="number" name="saved_amount" step="any" min="0" value="{{ old('saved_amount', $goal->saved_amount) }}" required>
            @error('saved_amount')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Deadline (optional)
            <input type="date" name="deadline" value="{{ old('deadline', $goal->deadline?->format('Y-m-d')) }}">
            @error('deadline')<small class="err">{{ $message }}</small>@enderror
        </label>

        <div class="form-actions">
            <button type="submit" class="btn">Save</button>
            <a class="btn ghost" href="{{ route('goals.index') }}">Cancel</a>
        </div>
    </form>
</div>
@endsection
