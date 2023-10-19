package edu.cornell.cs.sam.ui.components;
import edu.cornell.cs.sam.core.Memory;
import java.awt.Color;

import static edu.cornell.cs.sam.core.Memory.Data;

/* Class used in Stack and Heap renderers to 
 * store memory contents and display info */

public class MemoryCell {

	/**
	 * The background color used for integers
	 */
	public static final Color COLOR_INT = Color.WHITE;

	/**
	 * The background color used for floats
	 */
	public static final Color COLOR_FLOAT = new Color(255, 255, 204);

	/**
	 * The background color used for memory addresses
	 */
	public static final Color COLOR_MA = new Color(255, 204, 204);

	/**
	 * The background color used for program addresses
	 */
	public static final Color COLOR_PA = new Color(204, 255, 204);

	/**
	 * The background color used for characters
	 */
	public static final Color COLOR_CH = new Color(220, 204, 255);

	/**
	 * The color used for any items that do not have a correct type
	 */
	public static final Color COLOR_DEFAULT = Color.WHITE;

	/* Memory contents */
	private Data data;
	private int address;

	/* Display info */
	private String text;
	private Color color;
	private String tooltip_text;

	public MemoryCell(Data data, int address) {
		this.data = data;
		this.address = address;
		text = address + ": ";
		tooltip_text = "<html> Address: " + address + " <br> Type: ";

		switch (data.getType()) {
			case INT:
				text += "I : " + data;
				tooltip_text += "Integer";
				color = COLOR_INT;
				break;

			case FLOAT:
				text += "F : " + data;
				color = COLOR_FLOAT;
				tooltip_text += "Floting Point";
                                break;

			case MA:
				text += "M : " + data;
				color = COLOR_MA;
				tooltip_text += "Memory Address";
				break;

			case PA:
				text += "P : " + data;
				color = COLOR_PA;
				tooltip_text += "Program Address";
				break;

			case CH:
				text += "C : " + data;
				color = COLOR_CH;
				tooltip_text += "Character";
				break;

			default:
				text += data;
				color = COLOR_DEFAULT;
				tooltip_text += "Unknown";
		}
	}

	public String getText() {
		 return text;
	}

	public String getToolTip() {
		return tooltip_text;
	}

	public Color getColor() {
		return color;
	}
}

