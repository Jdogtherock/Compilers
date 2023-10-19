package edu.cornell.cs.sam.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

/**
 * Ths is a dialog box that includes the reference for the colors use in the
 * stack and heap
 */

public class SamColorReferenceDialog extends JDialog {
	public SamColorReferenceDialog(JFrame parent) {
		super(parent, false);
		JPanel colorPanel = new JPanel();
		Container p = getContentPane();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 5, 5, 5);
		GridBagLayout l = new GridBagLayout();
		colorPanel.setLayout(l);

		setTitle("Stack Color Reference");
		setSize(300, 300);

		p.setLayout(new BorderLayout());
		
		p.add(new JLabel("Stack Colors:"), BorderLayout.NORTH);

		colorPanel.setLayout(l);
		c.anchor = GridBagConstraints.EAST;
		GridBagUtils.addComponent(new JLabel("Integers:"), colorPanel, l, c, 0, 0, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.CENTER;
		GridBagUtils.addComponent(createColorPanel(MemoryCell.COLOR_INT), colorPanel, l, c, 1, 0, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.EAST;
		GridBagUtils.addComponent(new JLabel("Floats:"), colorPanel, l, c, 0, 1, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.CENTER;
		GridBagUtils.addComponent(createColorPanel(MemoryCell.COLOR_FLOAT), colorPanel, l, c, 1, 1, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.EAST;
		GridBagUtils.addComponent(new JLabel("Memory Addresses:"), colorPanel, l, c, 0, 2, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.CENTER;
		GridBagUtils.addComponent(createColorPanel(MemoryCell.COLOR_MA), colorPanel, l, c, 1, 2, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.EAST;
		GridBagUtils.addComponent(new JLabel("Program Addresses:"), colorPanel, l, c, 0, 3, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.CENTER;
		GridBagUtils.addComponent(createColorPanel(MemoryCell.COLOR_PA), colorPanel, l, c, 1, 3, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.EAST;
		GridBagUtils.addComponent(new JLabel("Characters:"), colorPanel, l, c, 0, 4, 1, 1, 1, 1);
		c.anchor = GridBagConstraints.CENTER;
		GridBagUtils.addComponent(createColorPanel(MemoryCell.COLOR_CH), colorPanel, l, c, 1, 4, 1, 1, 1, 1);
		p.add(colorPanel, BorderLayout.CENTER);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
		});
		p.add(closeButton, BorderLayout.SOUTH);
		pack();
	}

	private JPanel createColorPanel(Color c) {
		JPanel p = new JPanel();
		p.add(new JLabel("        "));
		p.setBackground(c);
		p.setMinimumSize(new Dimension(90, 15));
		return p;
	}
}

