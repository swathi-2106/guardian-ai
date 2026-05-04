/* eslint-disable react-hooks/exhaustive-deps */
import { useNavigate } from "react-router-dom"
import { useEffect, useState } from "react"

function Home() {
  const navigate = useNavigate()

  const [progress, setProgress] = useState(0)
  const [status, setStatus] = useState("Initializing system...")

  useEffect(() => {
  const steps = [
    "Initializing system...",
    "Loading security modules...",
    "Analyzing threat patterns...",
    "Establishing secure connection...",
    "System ready"
  ]

  let stepIndex = 0

  const interval = setInterval(() => {
    setProgress(prev => {
      const next = prev + 20

      if (stepIndex < steps.length) {
        setStatus(steps[stepIndex])
        stepIndex++
      }

      if (next >= 100) {
        clearInterval(interval)
      }

      return next
    })
  }, 700)

  return () => clearInterval(interval)
}, [])

useEffect(() => {
  if (progress >= 100) {
    setTimeout(() => {
      navigate("/dashboard")
    }, 800)
  }
}, [progress])

  return (
   <div
 className="min-h-screen flex flex-col items-center justify-center text-white bg-cover bg-center relative overflow-hidden"
  style={{
    backgroundImage: "url('/backdrop.png')" // 🔥 PLACEHOLDER
  }}
>

  <div className="absolute inset-0 bg-black/60 z-0"></div>


<div className="relative z-10 flex flex-col items-center">
      {/* 🔥 Logo */}
      <img
  src="/ids-logo.png"   // 🔥 PLACEHOLDER
  alt="GuardianAI Logo"
  className="w-32 h-32 mb-4 object-contain animate-pulse"
/>
      {/* Subtitle */}
      <p className="text-gray-400 mb-6 text-sm">
        AI-Powered Intrusion Detection System
      </p>

      {/* 🧠 Status */}
      <p className="text-green-400 text-sm mb-4">
        {status}
      </p>

      {/* 📊 Progress Bar */}
      <div className="w-64 h-2 bg-gray-700 rounded overflow-hidden mb-6">
        <div
          className="h-2 bg-green-500 transition-all duration-500"
          style={{ width: `${progress}%` }}
        />
      </div>

      </div>

    </div>
  )
}

export default Home