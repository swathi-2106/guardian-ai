import { Moon, Sun } from "lucide-react"

function ThemeToggle({ checked, onChange }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={`Switch to ${checked ? "light" : "dark"} mode`}
      onClick={onChange}
      className={`relative inline-flex h-12 w-24 items-center rounded-full border px-1.5 transition-all duration-300 ease-out focus:outline-none focus-visible:ring-2 focus-visible:ring-cyan-400 focus-visible:ring-offset-2 ${
        checked
          ? "border-slate-700 bg-slate-900/90 shadow-[0_12px_30px_rgba(15,23,42,0.45)] focus-visible:ring-offset-slate-950"
          : "border-amber-200 bg-white/95 shadow-[0_12px_30px_rgba(245,158,11,0.18)] focus-visible:ring-offset-white"
      }`}
    >
      <span
        className={`absolute inset-y-1.5 left-1.5 flex h-9 w-9 items-center justify-center rounded-full shadow-lg transition-all duration-300 ease-out ${
          checked
            ? "translate-x-12 bg-slate-800 text-slate-100"
            : "translate-x-0 bg-amber-400 text-slate-950"
        }`}
      >
        {checked ? <Moon size={16} /> : <Sun size={16} />}
      </span>

      <span className="flex w-full items-center justify-between text-[11px] font-semibold uppercase tracking-[0.22em]">
        <span
          className={`pl-2 transition-colors duration-300 ${
            checked ? "text-slate-500" : "text-amber-600"
          }`}
        >
          <Sun size={14} />
        </span>
        <span
          className={`pr-2 transition-colors duration-300 ${
            checked ? "text-cyan-300" : "text-slate-300"
          }`}
        >
          <Moon size={14} />
        </span>
      </span>
    </button>
  )
}

export default ThemeToggle
