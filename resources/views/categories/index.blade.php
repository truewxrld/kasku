@extends('layouts.app')
@section('title', 'Categories')
@section('content')
<div class="page-head">
    <div><p class="eyebrow">Organise your transactions</p><h1>Categories</h1></div>
    <a class="btn" href="{{ route('categories.create') }}">+ New category</a>
</div>

<div class="grid-2">
    @foreach (['income' => $income, 'expense' => $expense] as $type => $list)
        <section class="card">
            <h2>{{ ucfirst($type) }}</h2>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Name</th><th class="num">Used</th><th></th></tr></thead>
                    <tbody>
                    @forelse ($list as $category)
                        <tr>
                            <td><span class="dot" style="background: {{ $category->color }}"></span>{{ $category->name }}</td>
                            <td class="num">{{ $category->transactions_count }}</td>
                            <td class="actions">
                                <a href="{{ route('categories.edit', $category) }}">Edit</a>
                                <form method="POST" action="{{ route('categories.destroy', $category) }}" onsubmit="return confirm('Delete this category?')">
                                    @csrf
                                    @method('DELETE')
                                    <button type="submit" class="link danger">Delete</button>
                                </form>
                            </td>
                        </tr>
                    @empty
                        <tr><td colspan="3" class="empty">No {{ $type }} categories.</td></tr>
                    @endforelse
                    </tbody>
                </table>
            </div>
        </section>
    @endforeach
</div>
@endsection
