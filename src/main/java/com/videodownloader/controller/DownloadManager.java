package com.videodownloader.controller;

import java.awt.Toolkit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.videodownloader.model.DownloadStrategy;
import com.videodownloader.model.Observer;
import com.videodownloader.model.VideoInfo;
import com.videodownloader.view.AppGUI;
import com.videodownloader.view.FolderSelector;

public class DownloadManager implements Observer {
	private DownloadStrategy strategy;
	private AppGUI gui;
	private int currentRowIndex = -1;

	private final Map<Integer, DownloadTask> pendingTasks = new ConcurrentHashMap<>();
	private final BlockingQueue<DownloadTask> downloadQueue = new LinkedBlockingQueue<>();
	private final Thread queueWorker;

	public DownloadManager() {
		this.queueWorker = new Thread(this::processQueue);
		queueWorker.setDaemon(true);
		queueWorker.start();
	}

	public void setStrategy(DownloadStrategy strategy) {
		this.strategy = strategy;
	}

	public void setGUI(AppGUI gui) {
		this.gui = gui;
	}

	public void enqueuePendingTask(int rowIndex) {
		DownloadTask task = pendingTasks.remove(rowIndex);
		if (task != null) {
			try {
				downloadQueue.put(task);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void processQueue() {
		while (true) {
			try {
				DownloadTask task = downloadQueue.take();
				this.currentRowIndex = task.rowIndex;
				int remaining = downloadQueue.size();
				System.out.println("\n==================================");

				if (gui != null) {
					gui.logToConsole("=> [Queue] Handling link: " + task.url);
					gui.updateQueueItemStatus(task.rowIndex, "Loading...", "0%");
				}

				System.out.println("Remaining: " + remaining + " video.");
				if (strategy != null) {
					strategy.startDownload(task.url, task.savePath, task.format, this);
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				System.err.println("[Queue] Error: " + e.getMessage());
				if (gui != null)
					gui.logToConsole("[Queue Error] " + e.getMessage());
			}
		}
	}

	@Override
	public void onProgressUpdate(String videoId, double percent, String speed) {
		// Log Console
		int width = 50;
		int progress = (int) (percent / 100 * width);

		StringBuilder bar = new StringBuilder("[");
		for (int i = 0; i < width; i++) {
			if (i < progress)
				bar.append("=");
			else if (i == progress)
				bar.append(">");
			else
				bar.append(" ");
		}

		String output = String.format("\r%s] %.1f%% | speed: %s", bar.toString(), percent, speed);
		System.out.print(output);

		// Update GUI
		if (gui != null && currentRowIndex != -1) {
			// Format %
			String progressStr = String.format("%.1f%%", percent);
			if (speed != null && !speed.trim().isEmpty() && !speed.equals("N/A")) {
				progressStr += " (" + speed + ")";
			}
			gui.updateQueueItemStatus(currentRowIndex, "Downloading...", progressStr);
		}
	}

	@Override
	public void onComplete(String videoId, String savedPath) {
		System.out.println("\n>> Video download completed successfully!");
		System.out.println(">> Saved at: " + savedPath);
		System.out.println("--------------------------------------------------");

		if (gui != null && currentRowIndex != -1) {
			gui.updateQueueItemStatus(currentRowIndex, "Completed", "100%");
			gui.logToConsole(">> Download completed: " + savedPath);
		}
	}

	@Override
	public void onError(String videoId, String errorMessage) {
		System.err.println("\n>> Error: failed to download. Details: " + errorMessage);

		if (gui != null && currentRowIndex != -1) {
			gui.updateQueueItemStatus(currentRowIndex, "Error", "0%");
			gui.logToConsole(">> Download failed: " + errorMessage);
		}
	}

	public void processAutoCapture(String url) {
		Toolkit.getDefaultToolkit().beep();

		String[] options = { "1. MP4 - Video", "2. MP3 - Audio", "3. MKV - Original" };
		int choice = JOptionPane.showOptionDialog(null,
				"Video Hunter has already catched the download link!\n\nTarget link: " + url
						+ "\n\nChoose format to download:",
				"New video/playlist detected!", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
				options, options[0]);

		if (choice != JOptionPane.CLOSED_OPTION) {
			String format = (choice == 1) ? "mp3" : (choice == 2 ? "mkv" : "mp4");
			System.out.println("\n[Auto-Capture] Preparing, waiting for user to start...");
			String savePath = FolderSelector.chooseSaveDirectory();

			if (savePath != null && !savePath.isEmpty()) {
				SwingUtilities.invokeLater(() -> {
					if (gui != null) {
						int newRow = gui.addQueueItem(url, format.toUpperCase(), "Waiting...");
						pendingTasks.put(newRow, new DownloadTask(url, savePath, format, newRow));
						gui.logToConsole(
								"=> [Hunter] Added captured link to list (Row " + newRow + "). Ready to download.");
					}
				});
			} else {
				System.out.println("=> Canceled cuz you didn't choose saved directory.");
			}
		} else {
			System.out.println("\n[Auto-Capture] Skip the trash links: " + url);
		}
	}

	public void processLink(String url) {
		new Thread(() -> {
			try {
				System.out.println("\n[Analyzing link] " + url);
				List<String> links = strategy.extractPlaylistLinks(url);
				if (links.isEmpty()) {
					System.out.println("=> Couldn't found video/playlist or internet connection issues.");
					return;
				}

				VideoInfo info = strategy.fetchMetadata(links.get(0));
				String displayTitle = (links.size() > 1) ? "Playlist (" + links.size() + " videos): " + info.getTitle()
						: info.getTitle();

				SwingUtilities.invokeLater(() -> {
					String savePath = FolderSelector.chooseSaveDirectory();
					if (savePath == null || savePath.isEmpty())
						return;

					String[] options = { "1. MP4 - Popular video formatter (Default)", "MP3 - Just audio",
							"WebM/MKV - Original quality" };
					int choice = JOptionPane.showOptionDialog(null, "Choose download format for:\n" + displayTitle,
							"video/playlist", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);

					String format = (choice == 1) ? "mp3" : (choice == 2 ? "webm/mkv" : "mp4");

					for (String link : links) {
						int newRow = (gui != null) ? gui.addQueueItem(link, format.toUpperCase(), "Waiting...") : -1;
						if (newRow != -1) {
							pendingTasks.put(newRow, new DownloadTask(link, savePath, format, newRow));
						}
					}
					if (gui != null)
						gui.logToConsole("=> [System] Added " + links.size() + " items to waitlist.");
				});

			} catch (Exception e) {
				System.err.println("Analytics error! Error: " + e.getMessage());
			}
		}).start();
	}

	public void processApiJson(String jsonResponse) {
		try {
			JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
			JsonObject movie = root.getAsJsonObject("movie");

			String movieName = movie.get("name").getAsString();

			JsonArray episodesArray = root.getAsJsonArray("episodes");
			if (episodesArray.size() > 0) {
				JsonObject server = episodesArray.get(0).getAsJsonObject();
				JsonArray items = server.getAsJsonArray("items");

				System.out.println("\n[API Parser] Found Movie: " + movieName);
				System.out.println("[API Parser] Total episodes: " + items.size());

				String savePath = FolderSelector.chooseSaveDirectory();
				if (savePath == null || savePath.isEmpty())
					return;

				SwingUtilities.invokeLater(() -> {
					for (JsonElement itemElement : items) {
						JsonObject item = itemElement.getAsJsonObject();
						String epNumber = item.get("name").getAsString();
						String m3u8Url = item.get("m3u8").getAsString();

						String fileName = movieName + " - Tập " + epNumber;

						if (gui != null) {
							int newRow = gui.addQueueItem(fileName, "MP4", "Waiting...");
							pendingTasks.put(newRow, new DownloadTask(m3u8Url, savePath, "mp4", newRow));
						}
					}
					if (gui != null)
						gui.logToConsole(
								"=> [API] Successfully loaded " + items.size() + " episodes. Ready to download.");
				});
			}
		} catch (Exception e) {
			System.err.println("API Parse Error: " + e.getMessage());
		}
	}

	private static class DownloadTask {
		String url;
		String savePath;
		String format;
		int rowIndex;

		public DownloadTask(String url, String savePath, String format, int rowIndex) {
			this.url = url;
			this.savePath = savePath;
			this.format = format;
			this.rowIndex = rowIndex;
		}
	}
}