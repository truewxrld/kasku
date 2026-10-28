<?php

namespace App\Support;

class Money
{
    /** Format a number as Indonesian Rupiah, e.g. "Rp 1.250.000". */
    public static function rp(float|int|string|null $value): string
    {
        return 'Rp ' . number_format((float) $value, 0, ',', '.');
    }
}
