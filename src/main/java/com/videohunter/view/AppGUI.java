package com.videohunter.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.*;

import com.videohunter.controller.*;
import com.videohunter.model.Observer;

public class AppGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField urlInput;
	private JTextArea consoleOutput;
	private JButton btnHunt;
	private JToggleButton btnClipboard;
	private DownloadManager manager;
	private Thread clipboardThread;
	private JProgressBar progressBar;

	public AppGUI() {
		DependencyManager.checkAndDownloadDependencies();
		manager = new DownloadManager();
		manager.setStrategy(new NeccessaryToolsAdapter());
		
		LocalHttpServer server = new LocalHttpServer(manager);
		server.start();

		setTitle("Video Downloader - v1.0.1");
		setSize(700, 450);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(10, 10));

		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		urlInput = new JTextField();
		urlInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
		urlInput.setToolTipText("Paste the link here or leave it blank to browse the web for video/playlist...");
		
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnHunt = new JButton("Hunting / Download (Enter)");
		btnHunt.setBackground(new Color(40, 167, 69));
		btnHunt.setForeground(Color.BLACK);
		
		btnClipboard = new JToggleButton("Open following clipboard");
		
		btnPanel.add(btnClipboard);
		btnPanel.add(btnHunt);

		topPanel.add(new JLabel("Target link:"), BorderLayout.WEST);
		topPanel.add(urlInput, BorderLayout.CENTER);
		topPanel.add(btnPanel, BorderLayout.EAST);

		consoleOutput = new JTextArea();
		consoleOutput.setEditable(false);
		consoleOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
		consoleOutput.setBackground(new Color(30, 30, 30));
		consoleOutput.setForeground(new Color(200, 200, 200));
		JScrollPane scrollPane = new JScrollPane(consoleOutput);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Activity log"));

		redirectSystemStreams();

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

		add(topPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(progressBar, BorderLayout.SOUTH);

		setupEvents();
		checkFirstSetup();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void checkFirstSetup() {
		String profilePath = System.getProperty("user.home") + File.separator + ".VideoDownloaderApp" + File.separator + "ChromeProfile";
		File setupFlag = new File(profilePath, "setup_done.txt");
		if (!setupFlag.exists()) {
			BrowserController.autoSetupExtension();
		}
	}

	private void setupEvents() {
		btnHunt.addActionListener(e -> startHunting());
		urlInput.addActionListener(e -> startHunting());

		btnClipboard.addActionListener(e -> {
			if (btnClipboard.isSelected()) {
				btnClipboard.setText("Following Clipboard...");
				btnClipboard.setForeground(Color.RED);
				clipboardThread = new Thread(new ClipboardMonitor(manager));
				clipboardThread.start();
				System.out.println("=> [System] Following Clipboard opened!");
			} else {
				btnClipboard.setText("Open Clipboard");
				btnClipboard.setForeground(Color.BLACK);
				if (clipboardThread != null) clipboardThread.interrupt();
				System.out.println("=> [System] Following Clipboard closed!");
			}
		});

		manager.setStrategy(new NeccessaryToolsAdapter() {
			@Override
			public void startDownload(String url, String savePath, String format, Observer o) {
				super.startDownload(url, savePath, format, new Observer() {
					@Override
					public void onProgressUpdate(String videoId, double percent, String speed) {
						SwingUtilities.invokeLater(() -> {
							progressBar.setValue((int) percent);
							progressBar.setString(String.format("%.1f%% | Speed: %s", percent, speed));
						});
						o.onProgressUpdate(videoId, percent, speed);
					}
					@Override
					public void onComplete(String videoId, String savedPath) {
						SwingUtilities.invokeLater(() -> {
							progressBar.setValue(100);
							progressBar.setString("Download completed!");
						});
						o.onComplete(videoId, savedPath);
					}
					@Override
					public void onError(String videoId, String errorMessage) {
						o.onError(videoId, errorMessage);
					}
				});
			}
		});
	}

	private void startHunting() {
		String movieUrl = urlInput.getText().trim();
		if (movieUrl.isEmpty()) {
			BrowserController.openCaptureBrowser("");
			System.out.println("=> [Hunter] Chrome is open. Please browse the web and wait for the notification sound!");
		} else {
			String lowerUrl = movieUrl.toLowerCase();
			if (lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be") || 
				lowerUrl.contains("tiktok.com")  || lowerUrl.contains("facebook.com")) {
				manager.processLink(movieUrl);
			} else {
				BrowserController.openCaptureBrowser(movieUrl);
			}
		}
		urlInput.setText("");
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) {
				updateTextArea(String.valueOf((char) b));
			}
			@Override
			public void write(byte[] b, int off, int len) {
				updateTextArea(new String(b, off, len));
			}
			private void updateTextArea(final String text) {
				SwingUtilities.invokeLater(() -> {
					consoleOutput.append(text);
					consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
				});
			}
		};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
}