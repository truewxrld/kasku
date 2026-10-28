<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Category extends Model
{
    public const TYPES = ['income' => 'Income', 'expense' => 'Expense'];

    protected $fillable = ['user_id', 'name', 'type', 'color'];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function transactions(): HasMany
    {
        return $this->hasMany(Transaction::class);
    }

    public function budgets(): HasMany
    {
        return $this->hasMany(Budget::class);
    }

    /** Starter categories created for every new account. */
    public static function createDefaultsFor(User $user): void
    {
        $defaults = [
            ['Salary', 'income', '#0f766e'],
            ['Allowance', 'income', '#0d9488'],
            ['Other Income', 'income', '#14b8a6'],
            ['Food & Drink', 'expense', '#d97706'],
            ['Transport', 'expense', '#2563eb'],
            ['Bills', 'expense', '#7c3aed'],
            ['Shopping', 'expense', '#db2777'],
            ['Entertainment', 'expense', '#ea580c'],
            ['Education', 'expense', '#0891b2'],
            ['Other', 'expense', '#64748b'],
        ];

        foreach ($defaults as [$name, $type, $color]) {
            $user->categories()->create(['name' => $name, 'type' => $type, 'color' => $color]);
        }
    }
}
