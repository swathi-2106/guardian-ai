const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api"

const buildUrl = (path, params = {}) => {
  const url = new URL(`${API_BASE_URL}${path}`, window.location.origin)

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, value)
    }
  })

  return url.toString()
}

const requestJson = async (path, params = {}) => {
  const url = buildUrl(path, params)
  console.debug("[api] requesting", url)

  const response = await fetch(url, {
    headers: {
      Accept: "application/json",
    },
  })

  const contentType = response.headers.get("content-type") || ""
  const rawText = await response.text()

  console.debug("[api] response", {
    url,
    status: response.status,
    contentType,
  })

  if (!response.ok) {
    console.error("[api] non-OK response", {
      url,
      status: response.status,
      contentType,
      body: rawText.slice(0, 500),
    })
    throw new Error(`API request failed for ${url} with status ${response.status}`)
  }

  if (!contentType.toLowerCase().includes("application/json")) {
    console.error("[api] expected JSON but received different content-type", {
      url,
      status: response.status,
      contentType,
      body: rawText.slice(0, 500),
    })
    throw new Error(`Expected JSON response from ${url}, received ${contentType || "unknown content-type"}`)
  }

  try {
    return JSON.parse(rawText)
  } catch (error) {
    console.error("[api] failed to parse JSON response", {
      url,
      status: response.status,
      contentType,
      body: rawText.slice(0, 500),
    })
    throw error
  }
}

export const fetchLogs = async (type = "", mode = "REAL") =>
  requestJson("/logs", { mode, sourceType: mode, type })

export const fetchAlerts = async (mode = "REAL") =>
  requestJson("/alerts", { mode })

export const fetchTimeline = async (mode = "REAL") =>
  requestJson("/timeline", { mode })

export const fetchSystemStats = async () =>
  requestJson("/system-stats")

export const buildExportUrl = (format, mode = "REAL") =>
  buildUrl(`/export/${format}`, { mode })
