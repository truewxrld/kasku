@php
    $pct = $item->percent;
    $state = $pct > 100 ? 'over' : ($pct >= 80 ? 'warn' : 'ok');
@endphp
<div class="progress"><div class="fill {{ $state }}" style="width: {{ min(100, $pct) }}%"></div></div>
<div class="progress-meta">
    <span>{{ \App\Support\Money::rp($item->spent) }} of {{ \App\Support\Money::rp($item->limit) }} ({{ number_format($pct, 0) }}%)</span>
    @if ($item->remaining >= 0)
        <span class="pos">{{ \App\Support\Money::rp($item->remaining) }} left</span>
    @else
        <span class="neg">{{ \App\Support\Money::rp(abs($item->remaining)) }} over budget</span>
    @endif
</div>
