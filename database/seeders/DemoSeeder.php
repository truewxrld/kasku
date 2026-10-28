<?php

namespace Database\Seeders;

use App\Models\Category;
use App\Models\User;
use Illuminate\Database\Seeder;

/**
 * Sample data for screenshots and demos.
 * Login: demo@kasku.test / password123
 */
class DemoSeeder extends Seeder
{
    public function run(): void
    {
        $user = User::firstOrCreate(
            ['email' => 'demo@kasku.test'],
            ['name' => 'Demo User', 'password' => 'password123']
        );

        if (! $user->wasRecentlyCreated) {
            $this->command?->info('Demo user already exists - nothing to do.');

            return;
        }

        Category::createDefaultsFor($user);

        $cash = $user->wallets()->create(['name' => 'Cash', 'type' => 'cash', 'initial_balance' => 500000]);
        $bank = $user->wallets()->create(['name' => 'BCA Savings', 'type' => 'bank', 'initial_balance' => 5000000]);
        $pay = $user->wallets()->create(['name' => 'GoPay', 'type' => 'e-wallet', 'initial_balance' => 250000]);

        $cat = fn (string $name) => $user->categories()->where('name', $name)->first()->id;

        // [day offset, category, wallet, amount, note]
        $thisMonth = [
            [0, 'Salary', $bank, 6000000, 'Monthly internship salary'],
            [1, 'Food & Drink', $pay, 35000, 'Lunch at the canteen'],
            [2, 'Transport', $pay, 22000, 'Online ride to campus'],
            [3, 'Bills', $bank, 350000, 'Internet and electricity'],
            [4, 'Food & Drink', $cash, 18000, 'Breakfast'],
            [5, 'Entertainment', $pay, 85000, 'Game top-up'],
            [6, 'Shopping', $bank, 420000, 'Mechanical keyboard'],
            [7, 'Education', $bank, 150000, 'Programming course'],
            [8, 'Food & Drink', $cash, 27000, 'Dinner'],
            [9, 'Transport', $cash, 15000, 'Fuel'],
            [10, 'Allowance', $cash, 500000, 'Weekly allowance'],
            [11, 'Food & Drink', $pay, 42000, 'Coffee with friends'],
            [12, 'Other', $cash, 50000, 'Donation'],
        ];

        $lastMonth = [
            [0, 'Salary', $bank, 6000000, 'Monthly internship salary'],
            [2, 'Bills', $bank, 340000, 'Internet and electricity'],
            [3, 'Food & Drink', $cash, 210000, 'Groceries'],
            [5, 'Transport', $pay, 120000, 'Online rides'],
            [7, 'Shopping', $bank, 300000, 'New shoes'],
            [9, 'Entertainment', $pay, 100000, 'Cinema and snacks'],
            [12, 'Education', $bank, 200000, 'Books'],
            [15, 'Food & Drink', $pay, 95000, 'Weekend dinner'],
            [18, 'Allowance', $cash, 500000, 'Weekly allowance'],
        ];

        $startThis = now()->startOfMonth();
        $startLast = now()->startOfMonth()->subMonth();
        $maxOffset = max(0, now()->day - 1); // never create future-dated rows

        foreach ($thisMonth as [$offset, $category, $wallet, $amount, $note]) {
            $user->transactions()->create([
                'wallet_id' => $wallet->id,
                'category_id' => $cat($category),
                'amount' => $amount,
                'transaction_date' => $startThis->copy()->addDays(min($offset, $maxOffset))->toDateString(),
                'note' => $note,
            ]);
        }

        foreach ($lastMonth as [$offset, $category, $wallet, $amount, $note]) {
            $user->transactions()->create([
                'wallet_id' => $wallet->id,
                'category_id' => $cat($category),
                'amount' => $amount,
                'transaction_date' => $startLast->copy()->addDays($offset)->toDateString(),
                'note' => $note,
            ]);
        }

        $month = now()->format('Y-m');
        foreach ([['Food & Drink', 500000], ['Transport', 150000], ['Entertainment', 200000], ['Shopping', 400000]] as [$name, $limit]) {
            $user->budgets()->create(['category_id' => $cat($name), 'month' => $month, 'amount_limit' => $limit]);
        }

        $user->goals()->create(['name' => 'Laptop upgrade', 'target_amount' => 15000000, 'saved_amount' => 4500000, 'deadline' => now()->addMonths(6)->toDateString()]);
        $user->goals()->create(['name' => 'Emergency fund', 'target_amount' => 10000000, 'saved_amount' => 2000000, 'deadline' => null]);
        $user->goals()->create(['name' => 'Weekend trip', 'target_amount' => 3000000, 'saved_amount' => 3000000, 'deadline' => now()->addMonth()->toDateString()]);
    }
}
