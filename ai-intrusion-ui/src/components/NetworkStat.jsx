import { RadialBarChart, RadialBar, PolarAngleAxis } from "recharts"

const getColor = (value) => {
  if (value < 300) return "#4ade80"   // soft green
  if (value < 700) return "#facc15"   // soft yellow
  return "#f87171"                   // soft red
}

function NetworkStat({ value, darkMode }) {
  // convert kb/s → percentage scale (0–100)
  const percentage = Math.min(value / 10, 100)

  const color = getColor(value)

  const data = [{ name: "Network", value: percentage }]

  return (
    <div
      className={`flex flex-col items-center justify-center p-6 rounded-xl ${
        darkMode
          ? "bg-gray-800 border border-gray-700"
          : "bg-white border border-gray-200 shadow-sm"
      }`}
    >
      {/* Label */}
      <h2
        className={`text-sm mb-3 ${
          darkMode ? "text-gray-400" : "text-gray-500"
        }`}
      >
        Network
      </h2>

      {/* Circle */}
      <RadialBarChart
        width={180}
        height={180}
        innerRadius="70%"
        outerRadius="100%"
        data={data}
        startAngle={90}
        endAngle={-270}
      >
        <PolarAngleAxis type="number" domain={[0, 100]} tick={false} />

        <RadialBar
          dataKey="value"
          fill={color}
          cornerRadius={15}
          background={{
            fill: darkMode ? "#374151" : "#e5e7eb"
          }}
        />
      </RadialBarChart>

      {/* Value */}
      <p
        className={`text-2xl font-semibold mt-3 ${
          darkMode ? "text-white" : "text-black"
        }`}
      >
        {value} kb/s
      </p>
    </div>
  )
}

export default NetworkStat