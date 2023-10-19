package edu.cornell.cs.sam.ui.components;
import java.awt.Rectangle;
import javax.swing.JLabel;

/**
 * This is the parent class for the other renderers and overrides methods for
 * performance reasons
 */

public class CellRenderer extends JLabel {
	public CellRenderer() {
		super();
		setOpaque(true);
	}

	public void validate() { }
	public void revalidate() { }
	public void repaint(long tm, int x, int y, int width, int height) { }
	public void repaint(Rectangle r) { }

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		// Strings get interned...
		if (propertyName == "text") super.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) { }
	public void firePropertyChange(String propertyName, char oldValue, char newValue) { }
	public void firePropertyChange(String propertyName, short oldValue, short newValue) { }
	public void firePropertyChange(String propertyName, int oldValue, int newValue) { }
	public void firePropertyChange(String propertyName, long oldValue, long newValue) { }
	public void firePropertyChange(String propertyName, float oldValue, float newValue) { }
	public void firePropertyChange(String propertyName, double oldValue, double newValue) { }
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }
}
