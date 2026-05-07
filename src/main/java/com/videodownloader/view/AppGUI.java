package com.videodownloader.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.videodownloader.controller.BrowserController;
import com.videodownloader.controller.DownloadManager;

public class AppGUI extends JFrame {
	private JTextField urlInput;
	private JButton btnClipboard;
	private JButton btnHunt;
	private DefaultTableModel tableModel;
	private JTable queueTable;
	private JTextArea consoleLog;
	private JButton btnDownloadSelected;
	private JProgressBar progressBar;

	private final DownloadManager manager;

	public AppGUI(DownloadManager manager) {
		this.manager = manager;

		setTitle("Video Downloader - v1.0.2");
		setSize(850, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(10, 10));

		JPanel topPanel = new JPanel(new BorderLayout(10, 10));
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
		inputPanel.add(new JLabel("Target link:"), BorderLayout.WEST);
		urlInput = new JTextField();
		inputPanel.add(urlInput, BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		btnClipboard = new JButton("Open following clipboard");
		btnHunt = new JButton("Hunting / Download (Enter)");
		btnPanel.add(btnClipboard);
		btnPanel.add(btnHunt);

		topPanel.add(inputPanel, BorderLayout.CENTER);
		topPanel.add(btnPanel, BorderLayout.EAST);

		JPanel queuePanel = new JPanel(new BorderLayout());
		queuePanel.setBorder(BorderFactory.createTitledBorder("Download Queue"));

		String[] columns = { "STT", "Link video/playlist", "Status", "Progress" };
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		queueTable = new JTable(tableModel);
		
		queueTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		queueTable.getColumnModel().getColumn(0).setMaxWidth(50);
		queueTable.setRowHeight(25);

		JScrollPane tableScroll = new JScrollPane(queueTable);
		queuePanel.add(tableScroll, BorderLayout.CENTER);

		JPanel bottomContainer = new JPanel(new BorderLayout(5, 5));
		bottomContainer.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		JPanel consolePanel = new JPanel(new BorderLayout());
		consolePanel.setBorder(BorderFactory.createTitledBorder("Console Log"));
		consoleLog = new JTextArea(6, 50);
		consoleLog.setEditable(false);
		consoleLog.setBackground(new Color(30, 30, 30));
		consoleLog.setForeground(new Color(200, 200, 200));
		consoleLog.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
		consolePanel.add(new JScrollPane(consoleLog), BorderLayout.CENTER);

		JPanel actionPanel = new JPanel(new BorderLayout(0, 5));
		btnDownloadSelected = new JButton("Download Selected");
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);

		actionPanel.add(btnDownloadSelected, BorderLayout.NORTH);
		actionPanel.add(progressBar, BorderLayout.SOUTH);

		bottomContainer.add(consolePanel, BorderLayout.CENTER);
		bottomContainer.add(actionPanel, BorderLayout.SOUTH);

		add(topPanel, BorderLayout.NORTH);
		add(queuePanel, BorderLayout.CENTER);
		add(bottomContainer, BorderLayout.SOUTH);

		btnHunt.addActionListener(e -> startHunting());

		urlInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					startHunting();
				}
			}
		});

		btnDownloadSelected.addActionListener(e -> {
			int[] selectedRows = queueTable.getSelectedRows();
			if (selectedRows.length == 0) {
				JOptionPane.showMessageDialog(this, "Vui lòng bôi đen ít nhất 1 dòng trong bảng để tải!", "Chưa chọn file", JOptionPane.WARNING_MESSAGE);
				return;
			}
			logToConsole("=> Bắt đầu xử lý tải " + selectedRows.length + " mục đã chọn...");
		});
	}
	private void startHunting() {
		String movieUrl = urlInput.getText().trim();
		String lowerUrl = movieUrl.toLowerCase();

		if (movieUrl.isEmpty()) {
			logToConsole("=> [Hunter] Launching Native Chrome for manual browsing...");
			BrowserController.openCaptureBrowser("");
			return;
		}

		if (lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be")
				|| lowerUrl.contains("tiktok.com") || lowerUrl.contains("facebook.com")) {
			logToConsole("=> [System] Detected native platform. Direct download...");
			manager.processLink(movieUrl); 
		}
		else {
			logToConsole("=> [Hunter] Launching Native Chrome Engine...");
			BrowserController.openCaptureBrowser(movieUrl);
		}

		urlInput.setText("");
	}

	public void logToConsole(String message) {
		SwingUtilities.invokeLater(() -> {
			consoleLog.append(message + "\n");
			consoleLog.setCaretPosition(consoleLog.getDocument().getLength());
		});
	}
	public void addQueueItem(String url, String status) {
		SwingUtilities.invokeLater(() -> {
			int stt = tableModel.getRowCount() + 1;
			tableModel.addRow(new Object[] { stt, url, status, "0%" });
		});
	}

	public void updateQueueItemStatus(int rowIndex, String status, String progress) {
		SwingUtilities.invokeLater(() -> {
			if (rowIndex >= 0 && rowIndex < tableModel.getRowCount()) {
				tableModel.setValueAt(status, rowIndex, 2);
				tableModel.setValueAt(progress, rowIndex, 3);
			}
		});
	}
}