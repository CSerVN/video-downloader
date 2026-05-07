package com.videodownloader;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.videodownloader.view.AppGUI;
import com.videodownloader.controller.DownloadManager;

public class Program {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            DownloadManager manager = new DownloadManager();
            new AppGUI(manager).setVisible(true);
        });
    }
}