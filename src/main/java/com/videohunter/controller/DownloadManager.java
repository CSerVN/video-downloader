package com.videohunter.controller;

import java.awt.Toolkit;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.videohunter.model.DownloadStrategy;
import com.videohunter.model.Observer;
import com.videohunter.model.VideoInfo;
import com.videohunter.view.FolderSelector;

public class DownloadManager implements Observer {
	private DownloadStrategy strategy;
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

	private void processQueue() {
		while (true) {
			try {
				DownloadTask task = downloadQueue.take();
				
				int remaining = downloadQueue.size();
				System.out.println("\n==================================");
				System.out.println("Handling link: " + task.url);
				System.out.println("Remaining: " + remaining + " video.");
				
				if (strategy != null) {
					strategy.startDownload(task.url, task.savePath, task.format, this);
				}
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				System.err.println("[Queue] Error: " + e.getMessage());
			}
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
				String displayTitle = (links.size() > 1) ? 
					"Playlist (" + links.size() + " videos): " + info.getTitle() : 
					info.getTitle();
				System.out.println("Found video/playlist: " + displayTitle);

				SwingUtilities.invokeLater(() -> {
					String savePath = FolderSelector.chooseSaveDirectory();
					if (savePath == null || savePath.isEmpty()) {
						System.out.println("=> Canceled cuz you didn't choose saved directory.");
						return;
					}

					String[] options = { "1. MP4 - Popular video formatter (Default)", "MP3 - Just audio", "WebM/MKV - Original quality" };
					int choice = JOptionPane.showOptionDialog(null, 
							"Choose download format for:\n" + displayTitle,
							"video/playlist", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

					String format;
					if (choice == 1) format = "mp3";
					else if (choice == 2) format = "webm/mkv";
					else format = "mp4";

					new Thread(() -> {
						try {
							for (String link : links) {
								downloadQueue.put(new DownloadTask(link, savePath, format));
							}
							System.out.println("=> [System] Added " + links.size() + " to queue. Process is running in background. You can catch other links!");
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}).start();
				});

			} catch (Exception e) {
				System.err.println("Analytics error! Error: " + e.getMessage());
			}
		}).start();
	}

	@Override
	public void onProgressUpdate(String videoId, double percent, String speed) {
		int width = 50; // Length of process bar
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
	}

	@Override
	public void onComplete(String videoId, String savedPath) {
		System.out.println("\n>> Video download completed successfully!");
		System.out.println(">> Saved at: " + savedPath);
		System.out.println("--------------------------------------------------");
	}

	@Override
	public void onError(String videoId, String errorMessage) {
		System.err.println("\n>> Error: failed to download. Details: " + errorMessage);
	}

	public void processAutoCapture(String url) {
		Toolkit.getDefaultToolkit().beep();

		JOptionPane pane = new JOptionPane("Video Hunter has already catched the download link!\n\n" + "Target link: "
				+ url + "\n\n" + "Do you want to download it/them?", JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION);

		JDialog dialog = pane.createDialog("New video/playlist detected!");
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);

		Object selectedValue = pane.getValue();

		if (selectedValue != null && selectedValue.equals(JOptionPane.YES_OPTION)) {
			try {
				System.out.println("\n[Auto-Capture] TAKE IT! Preparing to download...");
				String savePath = FolderSelector.chooseSaveDirectory();
				String format = "mp4";

				Thread autoDownloadThread = new Thread(() -> {
					strategy.startDownload(url, savePath, format, this);
				});
				autoDownloadThread.start();
				System.out.println("=> [Hunter] Installing in the background. Let's continue with others!");

			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		} else {
			System.out.println("\n[Auto-Capture] Skip the trash links: " + url);
		}
	}
	private static class DownloadTask {
		String url;
		String savePath;
		String format;

		public DownloadTask(String url, String savePath, String format) {
			this.url = url;
			this.savePath = savePath;
			this.format = format;
		}
	}
}
