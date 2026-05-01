package com.videohunter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.videohunter.view.AppGUI;

public class Program {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}

		SwingUtilities.invokeLater(() -> {
			new AppGUI().setVisible(true);
		});
	}
}