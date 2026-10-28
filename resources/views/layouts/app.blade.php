<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>@yield('title', 'Welcome') | KasKu</title>
    <link rel="stylesheet" href="{{ asset('css/kasku.css') }}">
</head>
<body>
@auth
    <header class="topbar">
        <div class="wrap bar">
            <a class="brand" href="{{ route('dashboard') }}">KasKu</a>
            <nav class="nav">
                <a href="{{ route('dashboard') }}" class="{{ request()->routeIs('dashboard') ? 'active' : '' }}">Dashboard</a>
                <a href="{{ route('transactions.index') }}" class="{{ request()->routeIs('transactions.*') ? 'active' : '' }}">Transactions</a>
                <a href="{{ route('wallets.index') }}" class="{{ request()->routeIs('wallets.*') ? 'active' : '' }}">Wallets</a>
                <a href="{{ route('categories.index') }}" class="{{ request()->routeIs('categories.*') ? 'active' : '' }}">Categories</a>
                <a href="{{ route('budgets.index') }}" class="{{ request()->routeIs('budgets.*') ? 'active' : '' }}">Budgets</a>
                <a href="{{ route('goals.index') }}" class="{{ request()->routeIs('goals.*') ? 'active' : '' }}">Goals</a>
                <a href="{{ route('reports.index') }}" class="{{ request()->routeIs('reports.*') ? 'active' : '' }}">Reports</a>
            </nav>
            <form method="POST" action="{{ route('logout') }}" class="logout">
                @csrf
                <span class="who">{{ auth()->user()->name }}</span>
                <button type="submit" class="btn ghost small">Logout</button>
            </form>
        </div>
    </header>
@endauth

<main class="wrap">
    @if (session('success'))
        <div class="alert ok">{{ session('success') }}</div>
    @endif
    @if (session('error'))
        <div class="alert bad">{{ session('error') }}</div>
    @endif

    @yield('content')
</main>

<footer class="wrap foot">KasKu &middot; Web Programming (D) &middot; Laravel CRUD project</footer>
</body>
</html>
