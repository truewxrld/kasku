<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Goal extends Model
{
    protected $fillable = ['user_id', 'name', 'target_amount', 'saved_amount', 'deadline'];

    protected function casts(): array
    {
        return ['deadline' => 'date'];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /** Progress in percent, capped at 100. */
    public function percent(): float
    {
        $target = (float) $this->target_amount;

        return $target > 0 ? min(100, ((float) $this->saved_amount / $target) * 100) : 0;
    }

    public function isReached(): bool
    {
        return (float) $this->saved_amount >= (float) $this->target_amount;
    }
}
