<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Concerns\EnsuresOwnership;
use App\Models\Category;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;
use Illuminate\View\View;

class CategoryController extends Controller
{
    use EnsuresOwnership;

    public function index(Request $request): View
    {
        $categories = Category::withCount('transactions')
            ->where('user_id', $request->user()->id)
            ->orderBy('name')
            ->get()
            ->groupBy('type');

        return view('categories.index', [
            'income' => $categories->get('income', collect()),
            'expense' => $categories->get('expense', collect()),
        ]);
    }

    public function create(): View
    {
        return view('categories.form', [
            'category' => new Category(['type' => 'expense', 'color' => '#0f766e']),
        ]);
    }

    public function store(Request $request): RedirectResponse
    {
        $request->user()->categories()->create($this->validated($request));

        return redirect()->route('categories.index')->with('success', 'Category created.');
    }

    public function edit(Category $category): View
    {
        $this->ownedBy($category);

        return view('categories.form', compact('category'));
    }

    public function update(Request $request, Category $category): RedirectResponse
    {
        $this->ownedBy($category);
        $category->update($this->validated($request, $category));

        return redirect()->route('categories.index')->with('success', 'Category updated.');
    }

    public function destroy(Category $category): RedirectResponse
    {
        $this->ownedBy($category);

        if ($category->transactions()->exists()) {
            return back()->with('error', 'This category is used by transactions, so it cannot be deleted.');
        }

        $category->delete(); // its budgets are removed by the foreign key cascade

        return redirect()->route('categories.index')->with('success', 'Category deleted.');
    }

    private function validated(Request $request, ?Category $category = null): array
    {
        $userId = $request->user()->id;

        return $request->validate([
            'name' => [
                'required', 'string', 'max:100',
                Rule::unique('categories', 'name')
                    ->where(fn ($q) => $q->where('user_id', $userId)->where('type', $request->input('type')))
                    ->ignore($category?->id),
            ],
            'type' => ['required', Rule::in(array_keys(Category::TYPES))],
            'color' => ['required', 'regex:/^#[0-9a-fA-F]{6}$/'],
        ], [
            'name.unique' => 'You already have a category with this name and type.',
        ]);
    }
}
