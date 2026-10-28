<?php

namespace App\Http\Controllers\Concerns;

trait EnsuresOwnership
{
    /** Users may only touch their own records; anything else looks like "not found". */
    protected function ownedBy($model): void
    {
        abort_unless((int) $model->user_id === (int) auth()->id(), 404);
    }
}
