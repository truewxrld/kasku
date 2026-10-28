@extends('layouts.app')
@section('title', $category->exists ? 'Edit category' : 'New category')
@section('content')
<div class="card narrow">
    <h1>{{ $category->exists ? 'Edit category' : 'New category' }}</h1>
    <form method="POST" action="{{ $category->exists ? route('categories.update', $category) : route('categories.store') }}">
        @csrf
        @if ($category->exists)
            @method('PUT')
        @endif

        <label>Name
            <input type="text" name="name" value="{{ old('name', $category->name) }}" required>
            @error('name')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Type
            <select name="type">
                @foreach (\App\Models\Category::TYPES as $value => $label)
                    <option value="{{ $value }}" @selected(old('type', $category->type) === $value)>{{ $label }}</option>
                @endforeach
            </select>
            @error('type')<small class="err">{{ $message }}</small>@enderror
        </label>

        <label>Colour
            <input type="color" name="color" value="{{ old('color', $category->color) }}" required>
            @error('color')<small class="err">{{ $message }}</small>@enderror
        </label>

        <div class="form-actions">
            <button type="submit" class="btn">Save</button>
            <a class="btn ghost" href="{{ route('categories.index') }}">Cancel</a>
        </div>
    </form>
</div>
@endsection
