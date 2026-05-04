import { LayoutDashboard, FileText, AlertTriangle, Clock } from "lucide-react"
import { useEffect, useState } from "react"
import AlertsPanel from "../components/AlertsPanel"
import CircleStat from "../components/CircleStat"
import CPUGraph from "../components/CPUGraph"
import DashboardHeader from "../components/DashboardHeader"
import LogsTable from "../components/LogsTable"
import NetworkStat from "../components/NetworkStat"
import TimelineView from "../components/TimelineView"
import {
  buildExportUrl,
  fetchAlerts,
  fetchLogs,
  fetchSystemStats,
  fetchTimeline,
} from "../services/api"

const DASHBOARD_THEME_KEY = "guardian-dashboard-theme"
const EXPORT_TABS = new Set(["logs", "alerts", "timeline"])

const navigationItems = [
  { id: "performance", icon: LayoutDashboard, label: "Performance" },
  { id: "logs", icon: FileText, label: "Logs" },
  { id: "alerts", icon: AlertTriangle, label: "Alerts" },
  { id: "timeline", icon: Clock, label: "Timeline" },
]

function Dashboard() {
  const normalizeSourceType = (value) => {
    if (!value) {
      return "REAL"
    }

    const normalizedValue = String(value).trim().toUpperCase()
    return normalizedValue === "SIMULATION" ? "SIMULATED" : normalizedValue
  }

  const [search, setSearch] = useState("")
  const [activeTab, setActiveTab] = useState("performance")
  const [logs, setLogs] = useState([])
  const [timeline, setTimeline] = useState([])
  const [cpu, setCpu] = useState(0)
  const [memory, setMemory] = useState(0)
  const [network, setNetwork] = useState(0)
  const [darkMode, setDarkMode] = useState(() => {
    const savedTheme = window.localStorage.getItem(DASHBOARD_THEME_KEY)
    return savedTheme ? savedTheme === "dark" : true
  })
  const [filterType, setFilterType] = useState("")
  const [eventFilter, setEventFilter] = useState("")
  const [mode, setMode] = useState("REAL")
  const [alerts, setAlerts] = useState([])

  useEffect(() => {
    const loadData = async () => {
      try {
        const [logData, alertData, timelineData] = await Promise.all([
          fetchLogs(filterType, mode),
          fetchAlerts(mode),
          fetchTimeline(mode),
        ])

        setLogs(logData)
        setAlerts(alertData)
        setTimeline(timelineData)
      } catch (error) {
        console.error("[dashboard] failed to load dashboard data", {
          mode,
          filterType,
          error,
        })
      }
    }

    loadData()
  }, [filterType, mode])

  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const data = await fetchSystemStats()
        setCpu(data.cpu)
        setMemory(data.memory)
        setNetwork(data.network)
      } catch (error) {
        console.error("[dashboard] failed to load system stats", error)
      }
    }, 3000)

    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    window.localStorage.setItem(
      DASHBOARD_THEME_KEY,
      darkMode ? "dark" : "light",
    )
  }, [darkMode])

  const filteredLogs = logs.filter((log) => {
    const matchesMode = normalizeSourceType(log.sourceType || log.dataSource) === mode
    const matchesSearch =
      log.ipAddress?.includes(search) ||
      log.eventType?.includes(search) ||
      log.description?.toLowerCase().includes(search.toLowerCase())

    const matchesEvent =
      !eventFilter || log.eventType?.toUpperCase().includes(eventFilter)

    return matchesMode && matchesSearch && matchesEvent
  })

  const showExportActions = EXPORT_TABS.has(activeTab)
  const showModeToggle = activeTab === "performance"
  const showThemeToggle = activeTab === "performance"

  const handleExport = (format) => {
    window.open(buildExportUrl(format, mode), "_blank", "noopener,noreferrer")
  }

  const shellClassName = darkMode
    ? "bg-slate-950 text-white"
    : "bg-slate-50 text-slate-950"

  const panelClassName = darkMode
    ? "bg-slate-900 border border-slate-800"
    : "bg-white border border-slate-200 shadow-sm"

  const sidebarClassName = darkMode ? "bg-slate-900" : "bg-white border-r border-slate-200"

  return (
    <div className={`flex min-h-screen ${shellClassName}`}>
      <div className={`w-16 flex flex-col items-center py-4 space-y-6 ${sidebarClassName}`}>
        {navigationItems.map(({ id, icon: Icon, label }) => (
          <button
            key={id}
            type="button"
            onClick={() => setActiveTab(id)}
            className={`rounded-xl p-2 transition-colors ${
              darkMode ? "hover:bg-slate-800" : "hover:bg-slate-200"
            } ${
              activeTab === id
                ? darkMode
                  ? "bg-slate-800"
                  : "bg-slate-200"
                : ""
            }`}
            title={label}
            aria-label={label}
          >
            <Icon size={20} />
          </button>
        ))}
      </div>

      <div className="flex-1 p-4 md:p-6">
        <DashboardHeader
  darkMode={darkMode}
  mode={mode}
  onModeChange={setMode}
  onThemeToggle={() => setDarkMode((currentMode) => !currentMode)}
  onExport={handleExport}
  showExportActions={showExportActions}
  showModeToggle={showModeToggle}
  showThemeToggle={showThemeToggle}
/>

        <div className="mb-6 grid grid-cols-2 gap-4 md:grid-cols-4">
          <div className={`rounded-lg p-3 ${panelClassName}`}>
            <p className="text-sm text-gray-400">Total Alerts</p>
            <p className="text-xl font-bold">{alerts.length}</p>
          </div>

          <div className={`rounded-lg p-3 ${panelClassName}`}>
            <p className="text-sm text-gray-400">Critical Alerts</p>
            <p className="text-xl font-bold text-red-400">
              {alerts.filter((alert) => alert.severity === "HIGH").length}
            </p>
          </div>

          <div className={`rounded-lg p-3 ${panelClassName}`}>
            <p className="text-sm text-gray-400">CPU Status</p>
            <p className="text-xl font-bold">
              {cpu > 80 ? "High" : cpu > 40 ? "Normal" : "Low"}
            </p>
          </div>

          <div className={`rounded-lg p-3 ${panelClassName}`}>
            <p className="text-sm text-gray-400">Network</p>
            <p className="text-xl font-bold">
              {network > 500 ? "High" : "Normal"}
            </p>
          </div>
        </div>

        {activeTab === "performance" && (
          <div className="space-y-6">
            <div className="w-full">
              <CPUGraph value={cpu} darkMode={darkMode} />
            </div>

            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              <CircleStat label="Memory Usage" value={memory} darkMode={darkMode} />
              <NetworkStat value={network} darkMode={darkMode} />
            </div>
          </div>
        )}

        {activeTab === "logs" && (
          <div className={`rounded-xl p-4 ${panelClassName}`}>
            <h2 className="mb-2 text-lg font-semibold">Logs</h2>

            <input
              type="text"
              placeholder="Search logs..."
              className={`mb-3 w-full rounded p-2 outline-none ${
                darkMode
                  ? "bg-slate-800 text-white"
                  : "border border-slate-200 bg-white text-black"
              }`}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

            <div className="mb-3">
              <select
                value={eventFilter}
                onChange={(e) => setEventFilter(e.target.value)}
                className={`rounded p-2 ${
                  darkMode
                    ? "bg-slate-800 text-white"
                    : "border border-slate-200 bg-white"
                }`}
              >
                <option value="">All Events</option>
                <option value="SYSTEM-INFO">SYSTEM-INFO</option>
                <option value="SYSTEM-ERROR">SYSTEM-ERROR</option>
                <option value="SECURITY-INFO">SECURITY-INFO</option>
                <option value="SECURITY-AUDIT">SECURITY-AUDIT</option>
              </select>
            </div>

            <LogsTable logs={filteredLogs} />
          </div>
        )}

        {activeTab === "alerts" && (
          <div className={`rounded-xl p-4 ${panelClassName}`}>
            <h2 className="mb-2 text-lg font-semibold">Alerts</h2>
            <AlertsPanel alerts={alerts} darkMode={darkMode} />
          </div>
        )}

        {activeTab === "timeline" && (
          <div className={`rounded-xl p-4 ${panelClassName}`}>
            <h2 className="mb-2 text-lg font-semibold">Timeline</h2>
            <TimelineView timeline={timeline} />
          </div>
        )}
      </div>
    </div>
  )
}

export default Dashboard
