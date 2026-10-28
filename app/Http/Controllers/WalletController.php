<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Concerns\EnsuresOwnership;
use App\Models\Wallet;
use App\Services\FinanceService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;
use Illuminate\View\View;

class WalletController extends Controller
{
    use EnsuresOwnership;

    public function index(Request $request, FinanceService $finance): View
    {
        $userId = $request->user()->id;
        $wallets = Wallet::where('user_id', $userId)->orderBy('name')->get();
        $net = $finance->walletBalances($userId);

        return view('wallets.index', compact('wallets', 'net'));
    }

    public function create(): View
    {
        return view('wallets.form', [
            'wallet' => new Wallet(['type' => 'cash', 'initial_balance' => 0]),
        ]);
    }

    public function store(Request $request): RedirectResponse
    {
        $request->user()->wallets()->create($this->validated($request));

        return redirect()->route('wallets.index')->with('success', 'Wallet created.');
    }

    public function edit(Wallet $wallet): View
    {
        $this->ownedBy($wallet);

        return view('wallets.form', compact('wallet'));
    }

    public function update(Request $request, Wallet $wallet): RedirectResponse
    {
        $this->ownedBy($wallet);
        $wallet->update($this->validated($request));

        return redirect()->route('wallets.index')->with('success', 'Wallet updated.');
    }

    public function destroy(Wallet $wallet): RedirectResponse
    {
        $this->ownedBy($wallet);

        if ($wallet->transactions()->exists()) {
            return back()->with('error', 'This wallet still has transactions, so it cannot be deleted.');
        }

        $wallet->delete();

        return redirect()->route('wallets.index')->with('success', 'Wallet deleted.');
    }

    private function validated(Request $request): array
    {
        return $request->validate([
            'name' => ['required', 'string', 'max:100'],
            'type' => ['required', Rule::in(array_keys(Wallet::TYPES))],
            'initial_balance' => ['required', 'numeric', 'min:0', 'max:9999999999999'],
        ]);
    }
}
