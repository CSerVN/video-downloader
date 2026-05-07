package com.videodownloader.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class RemoveButtonRenderer extends JPanel implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	private boolean isHovered = false;
	private JLabel iconLabel;

	private int hoveredRow = -1;
	private int hoveredCol = -1;

	public void updateHoverState(int row, int col) {
		this.hoveredRow = row;
		this.hoveredCol = col;
	}

	public RemoveButtonRenderer() {
		setLayout(new BorderLayout());
		setOpaque(false);

		iconLabel = new JLabel("✖");
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		add(iconLabel, BorderLayout.CENTER);
	}

	public void setHoveredCell(int row, int col) {
		this.hoveredRow = row;
		this.hoveredCol = col;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		isHovered = (row == hoveredRow && column == hoveredCol);

		if (isHovered) {
			iconLabel.setForeground(new Color(220, 20, 60));
		} else {
			iconLabel.setForeground(Color.GRAY);
		}
		return this;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (isHovered) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(255, 230, 230));
			g2.fillRoundRect(5, 3, getWidth() - 10, getHeight() - 6, 8, 8);
			g2.dispose();
		}
	}
}