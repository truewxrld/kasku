<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Concerns\EnsuresOwnership;
use App\Models\Budget;
use App\Models\Category;
use App\Services\FinanceService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;
use Illuminate\View\View;

class BudgetController extends Controller
{
    use EnsuresOwnership;

    public function index(Request $request, FinanceService $finance): View
    {
        [, , $month] = $finance->monthRange($request->input('month'));
        $items = $finance->budgetProgress($request->user()->id, $month);

        return view('budgets.index', compact('items', 'month'));
    }

    public function create(Request $request): View
    {
        return view('budgets.form', [
            'budget' => new Budget(['month' => $request->input('month', now()->format('Y-m'))]),
            'categories' => $this->expenseCategories($request),
        ]);
    }

    public function store(Request $request): RedirectResponse
    {
        $data = $this->validated($request);
        $request->user()->budgets()->create($data);

        return redirect()->route('budgets.index', ['month' => $data['month']])->with('success', 'Budget created.');
    }

    public function edit(Request $request, Budget $budget): View
    {
        $this->ownedBy($budget);

        return view('budgets.form', [
            'budget' => $budget,
            'categories' => $this->expenseCategories($request),
        ]);
    }

    public function update(Request $request, Budget $budget): RedirectResponse
    {
        $this->ownedBy($budget);
        $data = $this->validated($request, $budget);
        $budget->update($data);

        return redirect()->route('budgets.index', ['month' => $data['month']])->with('success', 'Budget updated.');
    }

    public function destroy(Budget $budget): RedirectResponse
    {
        $this->ownedBy($budget);
        $month = $budget->month;
        $budget->delete();

        return redirect()->route('budgets.index', ['month' => $month])->with('success', 'Budget deleted.');
    }

    private function expenseCategories(Request $request)
    {
        return Category::where('user_id', $request->user()->id)->where('type', 'expense')->orderBy('name')->get();
    }

    private function validated(Request $request, ?Budget $budget = null): array
    {
        $userId = $request->user()->id;

        return $request->validate([
            'category_id' => [
                'required',
                Rule::exists('categories', 'id')->where(fn ($q) => $q->where('user_id', $userId)->where('type', 'expense')),
                Rule::unique('budgets', 'category_id')
                    ->where(fn ($q) => $q->where('user_id', $userId)->where('month', $request->input('month')))
                    ->ignore($budget?->id),
            ],
            'month' => ['required', 'regex:/^\d{4}-(0[1-9]|1[0-2])$/'],
            'amount_limit' => ['required', 'numeric', 'gt:0', 'max:9999999999999'],
        ], [
            'category_id.unique' => 'A budget for this category and month already exists.',
        ]);
    }
}
