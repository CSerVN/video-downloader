# 🚀 Installation Guide

Welcome to the setup guide for **Video Downloader**! Please follow the instructions below carefully to get the application running smoothly on your machine.

## Step 1: Requirements

Before running the application, ensure your system meets the following requirements:
* **Java 21 (or higher):** This application is built with Java and requires the Java Runtime Environment (JRE) to run. You can download it from [Adoptium (Eclipse Temurin)](https://adoptium.net/temurin/releases/?version=21).
* **Google Chrome:** Required specifically if you want to use the browser extension "Hunting" mode.

## Step 2: Download the App
1. Navigate to the **[Releases](../../releases)** page of this repository.
2. Download the compressed package that matches your Operating System:
   * **Windows:** Download `VideoDownloader-v1.0.0-Windows.rar`
   * **macOS:** Download `VideoDownloader-v1.0.0-macOS.tar.gz`
   * **Linux:** Download `VideoDownloader-v1.0.0-Linux.tar.gz`

## Step 3: Run the Application

### 🪟 For Windows Users
1. Extract the downloaded `.rar` archive to a folder of your choice (e.g., `C:\Downloads\VideoDownloader`).
2. Ensure that `yt-dlp.exe` and `ffmpeg.exe` are located in the exact same folder as the `.jar` file.
3. Simply **double-click** the `VideoDownloader.jar` file to launch the app!
   *(Troubleshooting: If double-clicking doesn't work, open Command Prompt in that folder and run: `java -jar VideoDownloader.jar`)*

### 🍎 For macOS & 🐧 For Linux Users
Unix-based systems require explicit permission to run executable files. 
1. Extract your downloaded archive.
2. Open your **Terminal** and navigate to the extracted folder.
3. **Grant execution permissions** to the core engine tools by running this command:
   ```bash
   chmod +x yt-dlp* ffmpeg*
   ```
