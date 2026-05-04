function LogsTable({ logs, darkMode }) {
  const formatTimestamp = (timestamp) => {
    if (!timestamp) {
      return "-"
    }

    const rawTimestamp = String(timestamp).trim()
    const plainTimestampMatch = rawTimestamp.match(
      /^\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}:\d{2}$/,
    )

    if (plainTimestampMatch) {
      return rawTimestamp.replace("T", " ")
    }

    const parsedTimestamp = new Date(rawTimestamp)

    if (Number.isNaN(parsedTimestamp.getTime())) {
      return rawTimestamp.replace("T", " ")
    }

    const pad = (value) => String(value).padStart(2, "0")

    return `${parsedTimestamp.getFullYear()}-${pad(parsedTimestamp.getMonth() + 1)}-${pad(parsedTimestamp.getDate())} ${pad(parsedTimestamp.getHours())}:${pad(parsedTimestamp.getMinutes())}:${pad(parsedTimestamp.getSeconds())}`
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full table-fixed text-left text-sm">
        <thead
          className={`border-b ${
            darkMode
              ? "border-gray-700 text-gray-400"
              : "border-gray-300 text-gray-600"
          }`}
        >
          <tr>
            <th className="w-[18%] py-2 pr-3">Timestamp</th>
            <th className="w-[16%] py-2 px-3">IP</th>
            <th className="w-[14%] py-2 px-3">Event</th>
            <th className="w-[52%] py-2 pl-3">Message</th>
          </tr>
        </thead>

        <tbody>
          {logs.map((log, index) => (
            <tr
              key={index}
              className={`border-b transition-colors duration-150 ${
                darkMode
                  ? "border-gray-800 hover:bg-gray-700"
                  : "border-gray-300 hover:bg-gray-100"
              }`}
            >
              <td className="py-2 pr-3 align-top break-words font-mono text-xs md:text-sm">
                {formatTimestamp(log.timestamp)}
              </td>
              <td className="py-2 px-3 align-top break-words">{log.ipAddress || "-"}</td>
              <td className="py-2 px-3 align-top break-words">{log.eventType || "-"}</td>
              <td className="py-2 pl-3 align-top break-words">{log.description || "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default LogsTable
