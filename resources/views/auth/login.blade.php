@extends('layouts.app')
@section('title', 'Login')
@section('content')
<div class="card narrow center-card">
    <h1>Login</h1>
    <p class="muted">Track your money, one transaction at a time.</p>
    <form method="POST" action="{{ route('login') }}">
        @csrf
        <label>Email
            <input type="email" name="email" value="{{ old('email') }}" required autofocus>
            @error('email')<small class="err">{{ $message }}</small>@enderror
        </label>
        <label>Password
            <input type="password" name="password" required>
            @error('password')<small class="err">{{ $message }}</small>@enderror
        </label>
        <label class="check"><input type="checkbox" name="remember" value="1"> Remember me</label>
        <button type="submit" class="btn block">Login</button>
    </form>
    <p class="muted small-top">No account yet? <a href="{{ route('register') }}">Register</a></p>
</div>
@endsection
