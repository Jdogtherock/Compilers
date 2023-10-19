package edu.cornell.cs.sam.ui.components;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/* Produces the Sam About Dialog */
public class SamAboutDialog extends JDialog {
	public SamAboutDialog(String packageName, String packageVersion,
			String component, JFrame parent) {
		super(parent, true);
		Container p = getContentPane();

		setTitle("About");
		setSize(300, 300);

		p.setLayout(new BorderLayout());

		p.add(new JLabel("<html><body>" +
				"<table>" +
					"<tr><td>" +
						"<font size=\"5\">" + packageName + " v" + packageVersion + "</font>" +
					"</td></tr>" +

					"<tr><td>" + component + "</td></tr>" +
				"</table>" +
				"<table><tr>" +
					"<td>Programmers:<br>" +
						"<i> Ivan Gyurdiev </i><br>" +
						"<i> David Levitan </i><br>" +
					"</td>" +
					"<td> Original Design:<br>" +
						"<i>Professor K. Pingali </i><br>" +
						"<i>Professor D. Schwartz</i>" +
					"</td>" +
				"</tr></table>" +
			"</body></html>"), BorderLayout.CENTER);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		p.add(closeButton, BorderLayout.SOUTH);
		pack();
	}
}

