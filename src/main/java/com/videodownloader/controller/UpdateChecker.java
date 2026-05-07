package com.videodownloader.controller;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UpdateChecker {
	private static final String CURRENT_VERSION = "v1.0.2";

	private static final String REPO_OWNER = "ThinhGitHubName"; // VD: tên acc github của bạn
	private static final String REPO_NAME = "VideoDownloaderRepo"; // VD: tên kho chứa

	private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".VideoDownloaderApp";
	private static final File SKIP_FILE = new File(CONFIG_DIR, "skipped_version.txt");

	public static void checkForUpdates() {
		new Thread(() -> {
			try {
				URL url = new URI("https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest")
						.toURL();
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

				if (conn.getResponseCode() == 200) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					reader.close();

					JsonObject release = JsonParser.parseString(response.toString()).getAsJsonObject();
					String latestVersion = release.get("tag_name").getAsString(); // VD: "v1.0.3"
					String releaseUrl = release.get("html_url").getAsString();

					if (!CURRENT_VERSION.equals(latestVersion) && !isVersionSkipped(latestVersion)) {
						showUpdateDialog(latestVersion, releaseUrl);
					}
				}
			} catch (Exception e) {
				System.err.println("[System] Update check failed (No internet or API limit).");
			}
		}).start();
	}

	private static boolean isVersionSkipped(String version) {
		try {
			if (SKIP_FILE.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(SKIP_FILE));
				String skippedVersion = reader.readLine();
				reader.close();
				return version.equals(skippedVersion);
			}
		} catch (Exception e) {
			// Ignore
		}
		return false;
	}

	private static void saveSkippedVersion(String version) {
		try {
			if (!SKIP_FILE.getParentFile().exists()) {
				SKIP_FILE.getParentFile().mkdirs();
			}
			FileWriter writer = new FileWriter(SKIP_FILE);
			writer.write(version);
			writer.close();
		} catch (Exception e) {
			System.err.println("Could not save skip config.");
		}
	}

	private static void showUpdateDialog(String latestVersion, String releaseUrl) {
		SwingUtilities.invokeLater(() -> {
			String message = "A new version (" + latestVersion + ") is available!\n" + "You are currently using "
					+ CURRENT_VERSION + ".\n\n" + "Do you want to download the update now?";

			String[] options = { "Yes, update now", "Skip this version", "Remind me later" };
			int choice = JOptionPane.showOptionDialog(null, message, "Update Available", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

			if (choice == 0) { // Yes, update now
				try {
					Desktop.getDesktop().browse(new URI(releaseUrl));
				} catch (Exception e) {
					System.err.println("Could not open browser.");
				}
			} else if (choice == 1) { // Skip this version
				saveSkippedVersion(latestVersion);
				System.out.println("=> [System] Skipped update for version: " + latestVersion);
			}
		});
	}
}