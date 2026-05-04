import { Download } from "lucide-react"
import ThemeToggle from "./ThemeToggle"

function DashboardHeader({
  darkMode,
  mode,
  onModeChange,
  onThemeToggle,
  onExport,
  showExportActions,
  showModeToggle,
   showThemeToggle, 
}) {
  const modeBadgeClassName =
    mode === "REAL"
      ? darkMode
        ? "border border-rose-500/40 bg-rose-500/10 text-rose-200"
        : "border border-rose-200 bg-rose-50 text-rose-700"
      : darkMode
        ? "border border-sky-500/40 bg-sky-500/10 text-sky-200"
        : "border border-sky-200 bg-sky-50 text-sky-700"

  const exportButtonClass = (variant) =>
    `inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200 ${
      variant === "csv"
        ? darkMode
          ? "bg-emerald-500/15 text-emerald-300 ring-1 ring-emerald-400/30 hover:bg-emerald-500/25"
          : "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200 hover:bg-emerald-100"
        : darkMode
          ? "bg-cyan-500/15 text-cyan-200 ring-1 ring-cyan-400/30 hover:bg-cyan-500/25"
          : "bg-sky-50 text-sky-700 ring-1 ring-sky-200 hover:bg-sky-100"
    }`

  return (
    <div className="mb-6 flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
      <div>
        <h1 className="text-2xl font-bold">AI Intrusion Detection Dashboard</h1>
       
        <div className="mt-3 flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] ${modeBadgeClassName}`}>
            {mode === "REAL" ? "Real Monitoring" : "Simulation Sandbox"}
          </span>
        
        </div>
      </div>

      <div className="flex flex-col items-stretch gap-3 md:flex-row md:flex-wrap md:items-center md:justify-end">
        {showModeToggle && (
          <div
            className={`flex rounded-2xl p-1 ${
              darkMode
                ? "border border-slate-700 bg-slate-950/60"
                : "border border-slate-200 bg-slate-100"
            }`}
          >
            <button
              type="button"
              onClick={() => onModeChange("REAL")}
              className={`rounded-xl px-4 py-2 text-sm font-semibold transition-all duration-200 ${
                mode === "REAL"
                  ? "bg-rose-600 text-white shadow-sm"
                  : darkMode
                    ? "text-slate-300 hover:bg-slate-800"
                    : "text-slate-600 hover:bg-white"
              }`}
            >
              REAL
            </button>
            <button
              type="button"
              onClick={() => onModeChange("SIMULATED")}
              className={`rounded-xl px-4 py-2 text-sm font-semibold transition-all duration-200 ${
                mode === "SIMULATED"
                  ? "bg-sky-600 text-white shadow-sm"
                  : darkMode
                    ? "text-slate-300 hover:bg-slate-800"
                    : "text-slate-600 hover:bg-white"
              }`}
            >
              SIMULATED
            </button>
          </div>
        )}

        {showExportActions && (
          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => onExport("csv")}
              className={exportButtonClass("csv")}
            >
              <Download size={16} />
              Download CSV
            </button>

            <button
              type="button"
              onClick={() => onExport("pdf")}
              className={exportButtonClass("pdf")}
            >
              <Download size={16} />
              Download PDF
            </button>
          </div>
        )}

        {showThemeToggle && (
  <ThemeToggle checked={darkMode} onChange={onThemeToggle} />
)}
      </div>
    </div>
  )
}

export default DashboardHeader
