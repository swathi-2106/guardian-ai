function TimelineView({ timeline, darkMode }) {
  const getStatusStyles = (status) => {
    if (status?.toLowerCase() === "suspicious") {
      return darkMode
        ? "bg-red-500"
        : "bg-red-300"
    }

    return darkMode
      ? "bg-green-500"
      : "bg-green-300"
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
    <div className="space-y-4">
      {timeline.map((event, index) => (
        <div
          key={index}
          className={`flex items-start space-x-4 rounded-lg p-3 transition-colors duration-150 ${
            darkMode
              ? "hover:bg-gray-800"
              : "hover:bg-gray-300"
          }`}
        >
          <div
            className={`mt-2 h-3 w-3 rounded-full ${getStatusStyles(event.status)}`}
          />

          <div className="flex-1">
            <div
              className={`text-xs ${
                darkMode
                  ? "text-gray-400"
                  : "text-gray-500"
              }`}
            >
              {event.timestamp}
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <div
                className={`font-semibold ${
                  darkMode
                    ? "text-white"
                    : "text-black"
                }`}
              >
                {event.eventType || event.type || "Timeline Event"}
              </div>
              <span className={`inline-flex rounded-full px-2 py-1 text-[11px] font-semibold ${getModeBadgeStyles(event.dataMode)}`}>
                {event.dataMode || "REAL"}
              </span>
            </div>

            <div
              className={`text-sm ${
                darkMode
                  ? "text-gray-100"
                  : "text-gray-700"
              }`}
            >
              {event.description}
            </div>

            <div
              className={`mt-1 text-xs ${
                darkMode
                  ? "text-gray-400"
                  : "text-gray-500"
              }`}
            >
              Source: {event.dataSource || "REAL"} | IP: {event.ipAddress || "LOCAL"}
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

export default TimelineView
