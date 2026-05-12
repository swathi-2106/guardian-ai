🛡️ Guardian AI – AI-Based Intrusion Detection System

A real-time AI-powered Host-Based Intrusion Detection System (HIDS) built using Spring Boot, React, and Python for intelligent threat monitoring and security event analysis.

📌 Overview

Guardian AI is a modern Intrusion Detection System designed to monitor Windows system and security logs in real time, detect suspicious activities, and generate actionable security alerts.

The system combines:

✅ Rule-Based Detection
✅ AI-Based Anomaly Detection
✅ Event Correlation
✅ Real-Time Log Monitoring
✅ Interactive Security Dashboard
✅ PDF & CSV Report Generation

The project focuses on providing a lightweight and scalable cybersecurity monitoring solution suitable for educational, research, and small-scale enterprise environments.

🚀 Features
🔍 Real-Time Log Monitoring
Collects logs from Windows Event Viewer
Supports:
System Logs
Security Logs
Custom Log Sources
🧠 Hybrid Detection Engine

Combines:

Rule-based intrusion detection
AI-powered anomaly detection

Detects:

Multiple failed login attempts
Suspicious file access
Unknown IP activity
Abnormal behavioral patterns
📊 Interactive Dashboard

Built using React for real-time visualization.

Dashboard Includes:
📄 Logs Table
🚨 Alerts Panel
📈 Timeline Visualization
🔎 Search & Filtering
📊 Security Statistics
⚡ Live Updates
📁 Report Generation

Export:

PDF Security Reports
CSV Log Reports

Includes:

Alert summaries
Attack timeline
Severity levels
Event tracking
🏗️ System Architecture
Windows Event Logs
        ↓
 Log Ingestion Layer
        ↓
  Preprocessing Engine
        ↓
 Hybrid Detection Engine
 (Rules + AI)
        ↓
  Correlation Module
        ↓
    Alert Engine
        ↓
 React Dashboard UI
 
⚙️ Tech Stack
Layer	Technology
Frontend	React + Vite
Backend	Spring Boot (Java)
AI Module	Python
Log Collection	PowerShell + Windows Event Viewer
Reporting	PDF / CSV Export
Database	H2 / File-based
Visualization	React Dashboard
🧩 Detection Modules
✅ Rule-Based Detection

Implemented modules:

Multiple Failed Login Detection
Unknown IP Detection
Suspicious File Access Detection
🤖 AI-Based Detection

AI engine analyzes:

Log behavior patterns
Event anomalies
Severity correlation

📡 API Endpoints
Endpoint	Description
/api/logs	Fetch logs
/api/alerts	Fetch alerts
/api/timeline	Timeline analysis
/api/export/csv	Export CSV report
/api/export/pdf	Export PDF report

Windows Log Monitoring
🛠️ Installation
1️⃣ Clone Repository
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd YOUR_REPO
2️⃣ Backend Setup
cd backend
mvn spring-boot:run

Backend runs on:

http://localhost:8080
3️⃣ Frontend Setup
cd frontend
npm install
npm run dev

Frontend runs on:

http://localhost:5173
📈 Future Enhancements
🔥 Advanced AI Threat Detection
🔥 Real-Time Streaming Pipeline
🔥 Database Integration
🔥 Attack Graph Visualization
🔥 JWT Authentication
🔥 Email Alert Notifications
🔥 SIEM Integration
🎯 Project Goals

This project aims to:

Improve real-time intrusion detection
Combine AI with cybersecurity workflows
Provide intelligent security monitoring
Build a scalable HIDS platform

👩‍💻 Author
Swathi S

If you like this project:

⭐ Star the repository
🍴 Fork the project
📢 Share feedback & suggestions

📜 License

This project is developed for educational and research purposes.
