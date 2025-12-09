# MAP IT — Dead Reckoning Android Application

An Android application built on an **Empty Activity Template**, implementing a **Dead Reckoning engine**, custom **sensor-driven UI components**, path visualization, orientation indicators, and session persistence.

This project demonstrates how raw **accelerometer + gyroscope** sensor data can be used to estimate motion, render device orientation, build telemetry dashboards, and display motion paths using **custom Android Views**.

---

## 📌 Project Overview

Dead Reckoning is a technique used to estimate positional change by integrating sensor data over time.

This app includes:

- 📡 **IMU Sensor Processing**
  - Reads Accelerometer data  
  - Reads Gyroscope data  

- 🧠 **Custom DeadReckoner Engine**
  - Performs motion integration  
  - Estimates position, velocity, orientation  

- 🎨 **Live Telemetry UI Components**
  - 🧭 Compass  
  - 🔺 Orientation 3D Indicator  
  - ⚡ Speedometer  
  - 📈 Acceleration Graph  
  - 🗺️ Path Overlay  

- 💾 **Session Management**
  - Stores user session data  
  - Handles app state persistence  

- 🖼️ **Custom UI Assets**
  - Drawable XMLs  
  - Clean minimal layout  

---

