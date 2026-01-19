export function CustomTooltip({ active, payload, label }: {
  active?: boolean;
  payload?: Array<{ name: string; value: number; color: string }>;
  label?: string;
}) {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white px-4 py-3 border border-gray-200">
        <div className="font-mono text-xs text-gray-400 mb-2">
          c={label}
        </div>
        {payload.map((entry, index) => (
          <div key={index} className="font-mono text-xs mb-1" style={{ color: entry.color }}>
            {entry.name}: <span className="text-gray-900">{entry.value.toLocaleString()}</span>
          </div>
        ))}
      </div>
    );
  }
  return null;
}
