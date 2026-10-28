<div class="table-wrap">
    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Category</th>
                <th>Wallet</th>
                <th>Note</th>
                <th class="num">Amount</th>
                @if ($actions)<th></th>@endif
            </tr>
        </thead>
        <tbody>
        @forelse ($rows as $t)
            <tr>
                <td>{{ $t->transaction_date->format('d M Y') }}</td>
                <td><span class="dot" style="background: {{ $t->category->color }}"></span>{{ $t->category->name }}</td>
                <td>{{ $t->wallet->name }}</td>
                <td>{{ $t->note }}</td>
                <td class="num {{ $t->category->type === 'income' ? 'pos' : 'neg' }}">
                    {{ $t->category->type === 'income' ? '+' : '-' }} {{ \App\Support\Money::rp($t->amount) }}
                </td>
                @if ($actions)
                    <td class="actions">
                        <a href="{{ route('transactions.edit', $t) }}">Edit</a>
                        <form method="POST" action="{{ route('transactions.destroy', $t) }}" onsubmit="return confirm('Delete this transaction?')">
                            @csrf
                            @method('DELETE')
                            <button type="submit" class="link danger">Delete</button>
                        </form>
                    </td>
                @endif
            </tr>
        @empty
            <tr><td colspan="6" class="empty">No transactions found.</td></tr>
        @endforelse
        </tbody>
    </table>
</div>
