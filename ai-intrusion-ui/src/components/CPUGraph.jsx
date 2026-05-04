import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid
} from "recharts"
import { ResponsiveContainer } from "recharts"
import { useState, useEffect } from "react"

function CPUGraph({ value, darkMode }) {
  const [data, setData] = useState([])

  useEffect(() => {
    setData(prev => {
      const newData = [...prev]

      const last = prev.length > 0 ? prev[prev.length - 1].cpu : value
      const smoothValue = Math.round(last * 0.7 + value * 0.3)

      newData.push({
        time: newData.length,
        cpu: smoothValue
      })

      return newData.slice(-40)
    })
  }, [value])

  // 🎯 soft professional colors
  const getColor = (val) => {
    if (val < 40) return "#4ade80"   // soft green
    if (val < 75) return "#facc15"   // soft yellow
    return "#f87171"                 // soft red
  }

  const strokeColor = getColor(value)

  return (
    
    <div
      className={`p-4 rounded-lg ${
        darkMode
          ? "bg-gray-800 border border-gray-700"
          : "bg-white border border-gray-200 shadow-sm"
      }`}
    >
      {/* Header */}
      <div className="flex justify-between mb-2">
        <h2 className={`text-sm ${darkMode ? "text-gray-400" : "text-gray-500"}`}>
          CPU
        </h2>
        <span className="text-xs text-gray-400">{value}%</span>
      </div>

      {/* Graph */}
      <ResponsiveContainer width="100%" height={250}>
  <AreaChart data={data}>
    
    <CartesianGrid
      strokeDasharray="3 3"
      stroke={darkMode ? "#374151" : "#e5e7eb"}
    />

    <XAxis hide />
    <YAxis domain={[0, 100]} hide />

    <Area
      type="monotone"
      dataKey="cpu"
      stroke={strokeColor}
      fill={strokeColor}
      fillOpacity={0.2}
      strokeWidth={2}
    />
    
  </AreaChart>
</ResponsiveContainer>
    </div>
  )
}

export default CPUGraph