package com.videodownloader;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.videodownloader.controller.ClipboardMonitor;
import com.videodownloader.controller.DependencyManager;
import com.videodownloader.controller.DownloadManager;
import com.videodownloader.controller.LocalHttpServer;
import com.videodownloader.controller.NeccessaryToolsAdapter;
import com.videodownloader.view.AppGUI;

public class Program {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
		}

		DependencyManager.checkAndDownloadDependencies();

		SwingUtilities.invokeLater(() -> {
			DownloadManager manager = new DownloadManager();
			AppGUI gui = new AppGUI(manager);
			manager.setGUI(gui);

			manager.setStrategy(new NeccessaryToolsAdapter());
			new LocalHttpServer(manager).start();

			Thread clipboardThread = new Thread(new ClipboardMonitor(manager));
			clipboardThread.setDaemon(true);
			clipboardThread.start();

			gui.setVisible(true);
		});
	}
}