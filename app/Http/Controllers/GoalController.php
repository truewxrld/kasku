<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Concerns\EnsuresOwnership;
use App\Models\Goal;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class GoalController extends Controller
{
    use EnsuresOwnership;

    public function index(Request $request): View
    {
        $goals = Goal::where('user_id', $request->user()->id)->orderBy('created_at')->get();

        return view('goals.index', compact('goals'));
    }

    public function create(): View
    {
        return view('goals.form', ['goal' => new Goal(['saved_amount' => 0])]);
    }

    public function store(Request $request): RedirectResponse
    {
        $request->user()->goals()->create($this->validated($request));

        return redirect()->route('goals.index')->with('success', 'Savings goal created.');
    }

    public function edit(Goal $goal): View
    {
        $this->ownedBy($goal);

        return view('goals.form', compact('goal'));
    }

    public function update(Request $request, Goal $goal): RedirectResponse
    {
        $this->ownedBy($goal);
        $goal->update($this->validated($request));

        return redirect()->route('goals.index')->with('success', 'Savings goal updated.');
    }

    public function destroy(Goal $goal): RedirectResponse
    {
        $this->ownedBy($goal);
        $goal->delete();

        return redirect()->route('goals.index')->with('success', 'Savings goal deleted.');
    }

    /** Add money to a goal without opening the edit form. */
    public function contribute(Request $request, Goal $goal): RedirectResponse
    {
        $this->ownedBy($goal);

        $data = $request->validate([
            'amount' => ['required', 'numeric', 'gt:0', 'max:9999999999999'],
        ]);

        $goal->increment('saved_amount', $data['amount']);

        return redirect()->route('goals.index')->with('success', 'Savings added to "' . $goal->name . '".');
    }

    private function validated(Request $request): array
    {
        return $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'target_amount' => ['required', 'numeric', 'gt:0', 'max:9999999999999'],
            'saved_amount' => ['required', 'numeric', 'min:0', 'max:9999999999999'],
            'deadline' => ['nullable', 'date'],
        ]);
    }
}
