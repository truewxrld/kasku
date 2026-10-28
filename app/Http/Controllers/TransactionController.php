<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Concerns\EnsuresOwnership;
use App\Models\Category;
use App\Models\Transaction;
use App\Models\Wallet;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;
use Illuminate\View\View;
use Symfony\Component\HttpFoundation\StreamedResponse;

class TransactionController extends Controller
{
    use EnsuresOwnership;

    public function index(Request $request): View
    {
        $transactions = $this->filteredQuery($request)->paginate(10)->withQueryString();

        return view('transactions.index', array_merge(
            compact('transactions'),
            $this->options($request)
        ));
    }

    public function create(Request $request): View
    {
        $transaction = new Transaction(['transaction_date' => now()->toDateString()]);

        return view('transactions.form', array_merge(compact('transaction'), $this->options($request)));
    }

    public function store(Request $request): RedirectResponse
    {
        $request->user()->transactions()->create($this->validated($request));

        return redirect()->route('transactions.index')->with('success', 'Transaction added.');
    }

    public function edit(Request $request, Transaction $transaction): View
    {
        $this->ownedBy($transaction);

        return view('transactions.form', array_merge(compact('transaction'), $this->options($request)));
    }

    public function update(Request $request, Transaction $transaction): RedirectResponse
    {
        $this->ownedBy($transaction);
        $transaction->update($this->validated($request));

        return redirect()->route('transactions.index')->with('success', 'Transaction updated.');
    }

    public function destroy(Transaction $transaction): RedirectResponse
    {
        $this->ownedBy($transaction);
        $transaction->delete();

        return back()->with('success', 'Transaction deleted.');
    }

    /** Download the (filtered) transaction list as a CSV file. */
    public function export(Request $request): StreamedResponse
    {
        $rows = $this->filteredQuery($request)->get();

        return response()->streamDownload(function () use ($rows) {
            $out = fopen('php://output', 'w');
            fputcsv($out, ['Date', 'Type', 'Category', 'Wallet', 'Amount', 'Note'], ',', '"', '\\');

            foreach ($rows as $t) {
                fputcsv($out, [
                    $t->transaction_date->format('Y-m-d'),
                    $t->category->type,
                    $this->csvSafe($t->category->name),
                    $this->csvSafe($t->wallet->name),
                    $t->amount,
                    $this->csvSafe($t->note),
                ], ',', '"', '\\');
            }

            fclose($out);
        }, 'kasku-transactions-' . now()->format('Ymd-His') . '.csv', ['Content-Type' => 'text/csv']);
    }

    /** Search + filters used by both the list page and the CSV export. */
    private function filteredQuery(Request $request): Builder
    {
        $query = Transaction::with(['wallet', 'category'])->where('user_id', $request->user()->id);

        if ($request->filled('q')) {
            $query->where('note', 'like', '%' . $request->input('q') . '%');
        }
        if ($request->filled('type')) {
            $type = $request->input('type');
            $query->whereHas('category', fn ($c) => $c->where('type', $type));
        }
        if ($request->filled('wallet_id')) {
            $query->where('wallet_id', $request->input('wallet_id'));
        }
        if ($request->filled('category_id')) {
            $query->where('category_id', $request->input('category_id'));
        }
        if ($request->filled('from')) {
            $query->whereDate('transaction_date', '>=', $request->input('from'));
        }
        if ($request->filled('to')) {
            $query->whereDate('transaction_date', '<=', $request->input('to'));
        }

        return $query->orderByDesc('transaction_date')->orderByDesc('id');
    }

    /** Wallets and categories for the select boxes. */
    private function options(Request $request): array
    {
        $userId = $request->user()->id;

        return [
            'wallets' => Wallet::where('user_id', $userId)->orderBy('name')->get(),
            'categories' => Category::where('user_id', $userId)->orderBy('type')->orderBy('name')->get(),
        ];
    }

    private function validated(Request $request): array
    {
        $userId = $request->user()->id;

        return $request->validate([
            // The chosen wallet/category must belong to the logged-in user.
            'wallet_id' => ['required', Rule::exists('wallets', 'id')->where('user_id', $userId)],
            'category_id' => ['required', Rule::exists('categories', 'id')->where('user_id', $userId)],
            'amount' => ['required', 'numeric', 'gt:0', 'max:9999999999999'],
            'transaction_date' => ['required', 'date'],
            'note' => ['nullable', 'string', 'max:255'],
        ]);
    }

    /** Stop spreadsheet formula injection when the CSV is opened in Excel. */
    private function csvSafe(?string $value): string
    {
        $value = (string) $value;

        return preg_match('/^[=+\-@]/', $value) ? "'" . $value : $value;
    }
}
