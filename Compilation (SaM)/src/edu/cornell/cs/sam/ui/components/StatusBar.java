package edu.cornell.cs.sam.ui.components;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

/**
 * A status bar with a beveled border
 */
public class StatusBar extends Box {
	private JLabel label;
	private LabelThread curThread;

	/**
	 * Creates a new status bar
	 */
	public StatusBar() {
		super(BoxLayout.X_AXIS);
		label = new JLabel();
		setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		add(label);
		label.setText(" ");
	}

	/**
	 * Sets the text - this will be removed after 5 seconds
	 * @param s the text to display
	 */
	public synchronized void setText(String s) {
		if (curThread != null)
			curThread.interrupt();
		label.setText(s);
		curThread = new LabelThread(this);
		curThread.start();
	}

	/**
	 * Set text that won't be cleared till requested
	 * @param s The text to display
	 */
	public synchronized void setPermanentText(String s) {
		if (curThread != null)
			curThread.interrupt();
		label.setText(s);
		curThread = null;
	}

	/**
	 * Clears the current text
	 */
	public synchronized void clearText() {
		label.setText(" ");
	}

	/**
	 * The thread to clear the label
	 */
	class LabelThread extends Thread {
		private StatusBar statusBar;

		/**
		 * Creates a new thread
		 * @param bar the bar to clear
		 */
		LabelThread(StatusBar bar) {
			statusBar = bar;
		}

		/**
		 * Starts the thread
		 */
		public void run() {
			try {
				java.lang.Thread.sleep(5000);
				statusBar.clearText();
			}
			catch (InterruptedException e) { }
		}
	}
}

