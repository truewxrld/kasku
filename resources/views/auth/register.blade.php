@extends('layouts.app')
@section('title', 'Register')
@section('content')
<div class="card narrow center-card">
    <h1>Create account</h1>
    <p class="muted">It takes less than a minute.</p>
    <form method="POST" action="{{ route('register') }}">
        @csrf
        <label>Name
            <input type="text" name="name" value="{{ old('name') }}" required autofocus>
            @error('name')<small class="err">{{ $message }}</small>@enderror
        </label>
        <label>Email
            <input type="email" name="email" value="{{ old('email') }}" required>
            @error('email')<small class="err">{{ $message }}</small>@enderror
        </label>
        <label>Password (min. 8 characters)
            <input type="password" name="password" required>
            @error('password')<small class="err">{{ $message }}</small>@enderror
        </label>
        <label>Confirm password
            <input type="password" name="password_confirmation" required>
        </label>
        <button type="submit" class="btn block">Register</button>
    </form>
    <p class="muted small-top">Already registered? <a href="{{ route('login') }}">Login</a></p>
</div>
@endsection
