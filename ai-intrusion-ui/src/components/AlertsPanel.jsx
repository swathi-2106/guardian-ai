function AlertsPanel({ alerts, darkMode }) {

  const getSeverityStyles = (severity) => {
    switch (severity) {
      case "HIGH":
        return darkMode
          ? "bg-red-500 text-white"
          : "bg-red-100 text-red-700"

      case "MEDIUM":
        return darkMode
          ? "bg-yellow-500 text-black"
          : "bg-yellow-100 text-yellow-700"

      case "LOW":
        return darkMode
          ? "bg-green-500 text-black"
          : "bg-green-100 text-green-700"

      default:
        return darkMode
          ? "bg-gray-600 text-white"
          : "bg-gray-200 text-gray-700"
    }
  }

  const getModeBadgeStyles = (dataMode) => {
    if (dataMode === "SIMULATED" || dataMode === "SIMULATION") {
      return darkMode
        ? "bg-sky-500/15 text-sky-200 ring-1 ring-sky-400/30"
        : "bg-sky-50 text-sky-700 ring-1 ring-sky-200"
    }

    return darkMode
      ? "bg-rose-500/15 text-rose-200 ring-1 ring-rose-400/30"
      : "bg-rose-50 text-rose-700 ring-1 ring-rose-200"
  }

  
  return (


    <div className="space-y-3">
      {alerts.map((alert, index) => (
        <div
          key={index}
          className={`p-3 rounded-lg border transition-colors duration-150 ${
            darkMode
              ? "bg-gray-700 border-gray-600 hover:bg-gray-600"
              : "bg-gray-100 border-gray-300 hover:bg-gray-200"
          }`}
        >

          
          
          {/* Top Row */}
          <div className="flex justify-between items-center gap-3">
            <div className="flex min-w-0 items-center gap-2">
              <span className="font-semibold">
                {alert.message || "No Description available"}
              </span>
              <span className={`inline-flex rounded-full px-2 py-1 text-[11px] font-semibold ${getModeBadgeStyles(alert.dataMode)}`}>
                {alert.dataMode || "REAL"}
              </span>
            </div>

            {/* Severity Badge */}
            <span
              className={`text-xs px-2 py-1 rounded font-semibold ${getSeverityStyles(
                alert.severity
              )}`}
            >
              {alert.severity}
            </span>
          </div>

          {/* IP */}
          <div
            className={`text-sm mt-1 ${
              darkMode ? "text-gray-300" : "text-gray-700"
            }`}
          >
           Logs involved: {alert.relatedLogs?.length || 1}
          </div>

          <div
            className={`text-xs ${
              darkMode ? "text-gray-400" : "text-gray-500"
            }`}
          >
            Source: {alert.dataSource || "REAL"}
          </div>

          {/* Time */}
          <div
            className={`text-xs ${
              darkMode ? "text-gray-400" : "text-gray-500"
            }`}
          >
            Time: {alert.timestamp}
          </div>
        </div>
      ))}
    </div>
  )
}

export default AlertsPanel
