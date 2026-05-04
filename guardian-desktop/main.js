const { app, BrowserWindow } = require("electron");
const { spawn } = require("child_process");
const path = require("path");
const http = require("http");

let backendProcess;

function startBackend() {
  backendProcess = spawn("java", ["-jar", "backend.jar"], {
    cwd: path.join(__dirname, "backend"),
    shell: true,
  });

  backendProcess.stdout.on("data", (data) => {
    console.log(`Backend: ${data}`);
  });

  backendProcess.stderr.on("data", (data) => {
    console.error(`Backend Error: ${data}`);
  });
}

// 🔥 Wait until backend is actually ready
function waitForBackend(url, callback) {
  const check = () => {
    http
      .get(url, () => {
        console.log("Backend is ready ✅");
        callback();
      })
      .on("error", () => {
        console.log("Waiting for backend...");
        setTimeout(check, 1000);
      });
  };
  check();
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
  });

  // 🔥 ADD THIS LINE HERE
  win.webContents.openDevTools();

  waitForBackend("http://localhost:8080", () => {
  win.loadFile(
    path.join(__dirname, "../ai-intrusion-ui/dist/index.html")
  );
});
}

app.whenReady().then(() => {
  startBackend();
  createWindow();
});

app.on("window-all-closed", () => {
  if (backendProcess) backendProcess.kill();
  if (process.platform !== "darwin") app.quit();
});