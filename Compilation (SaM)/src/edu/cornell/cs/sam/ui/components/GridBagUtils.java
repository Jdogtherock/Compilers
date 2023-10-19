package edu.cornell.cs.sam.ui.components;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;

public class GridBagUtils {

	public static JLabel addLabel(String label, JPanel panel, GridBagLayout layout, GridBagConstraints c,
		int x, int y, int width, int height, double weightx, double weighty) {
		JLabel comp = new JLabel(label);
		addComponent(comp, panel, layout, c, x, y, width, height, weightx, weighty);
		return comp;
	}

	public static JButton addButton(String label, JPanel panel, GridBagLayout layout, GridBagConstraints c,
		int x, int y, int width, int height, double weightx, double weighty) {
		JButton comp = new JButton(label);
		addComponent(comp, panel, layout, c, x, y, width, height, weightx, weighty);
		return comp;
	}

	public static void addComponent(JComponent comp, Container panel, GridBagLayout layout, GridBagConstraints c,
		int x, int y, int width, int height, double weightx, double weighty) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		layout.setConstraints(comp, c);
		panel.add(comp);
	}
}
