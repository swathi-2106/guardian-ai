import { RadialBarChart, RadialBar, PolarAngleAxis } from "recharts"

const getColor = (value) => {
  if (value < 40) return "#4ade80"   // soft green
  if (value < 75) return "#facc15"   // soft yellow
  return "#f87171"                   // soft red
}

function CircleStat({ label, value, unit = "%", darkMode }) {
  const color = getColor(value)

  const data = [{ name: label, value }]

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
        {label}
      </h2>

      {/* Chart */}
      <RadialBarChart
        width={180}
        height={180}
        innerRadius="70%"
        outerRadius="100%"
        data={data}
        startAngle={90}
        endAngle={-270}
      >
        {/* Background track (THIS FIXES BLUNT LOOK) */}
        <PolarAngleAxis
          type="number"
          domain={[0, 100]}
          tick={false}
        />

        {/* Actual value */}
        <RadialBar
          dataKey="value"
          fill={color}
          cornerRadius={15}   // smoother edges
          background={{
            fill: darkMode ? "#374151" : "#e5e7eb" // soft track ring
          }}
        />
      </RadialBarChart>

      {/* Value */}
      <p
        className={`text-2xl font-semibold mt-3 ${
          darkMode ? "text-white" : "text-black"
        }`}
      >
        {value}{unit}
      </p>
    </div>
  )
}

export default CircleStat