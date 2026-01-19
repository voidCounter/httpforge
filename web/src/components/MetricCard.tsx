import { Info } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string;
  unit: string;
  subtitle: string;
  color?: string;
  tooltip?: string;
}

export function MetricCard({ title, value, unit, subtitle, color, tooltip }: MetricCardProps) {
  return (
    <div className="bg-white border border-gray-200 p-6 relative group">
      <div className="font-mono text-xs text-gray-500 uppercase tracking-wider mb-3 flex items-center justify-between">
        <span>{title}</span>
        {tooltip && (
          <div className="relative">
            <Info className="w-4 h-4 text-gray-400 cursor-help" strokeWidth={1.5} />
            <div className="absolute bottom-full right-0 mb-2 w-72 bg-white border border-gray-200 text-gray-700 text-xs font-mono p-4 rounded-none shadow-sm opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-10 leading-relaxed">
              {tooltip}
            </div>
          </div>
        )}
      </div>
      <div className="flex items-baseline gap-2 mb-2">
        <div className={`text-4xl font-mono font-bold ${color? color: ""}`}>{value}</div>
        <div className="text-xl font-mono text-gray-400">{unit}</div>
      </div>
      <div className="font-mono text-xs text-gray-500">{subtitle}</div>
    </div>
  );
}
