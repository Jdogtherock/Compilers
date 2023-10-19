package edu.cornell.cs.sam.ui.components;
import edu.cornell.cs.sam.core.Memory;
import static edu.cornell.cs.sam.core.Memory.Data;
import edu.cornell.cs.sam.utils.ProgramState;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionListener;

public class SamStackPanel extends JPanel {

	/* Sam Stack GUI elements */
	private JScrollPane stackScrollPane;
	private JPanel stackInnerPanel;
	private JList stack;

	public SamStackPanel() {
		super();
		setPreferredSize(new Dimension(100,350));
		setMinimumSize(new Dimension(100,100));
		setLayout(new BorderLayout());

		stack  = new JList(new DefaultListModel());
		stack.setCellRenderer(new StackCellRenderer());
		stack.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		stack.setFont(new Font("Monospaced", Font.BOLD, 12));
		
		stackInnerPanel = new JPanel();
		stackInnerPanel.setLayout(new BorderLayout());
		stackInnerPanel.setBackground(Color.white);
		stackInnerPanel.add(stack, BorderLayout.SOUTH);	
	
		stackScrollPane = new JScrollPane(stackInnerPanel);
		stackScrollPane.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		
		add(new JLabel("Stack:"), BorderLayout.NORTH);
		add(stackScrollPane, BorderLayout.CENTER);
	}	

	public void bindSelectionListener(ListSelectionListener l) {
		stack.addListSelectionListener(l);
	}

	public void update(Memory mem) {
		DefaultListModel stak = (DefaultListModel) (stack.getModel());
		stak.clear();

		int addr = 0;
		for (Data item: mem.getStack()) 
			stak.add(0, new MemoryCell(item, addr++));

		stack.ensureIndexIsVisible(0);
		stackScrollPane.revalidate();
		stackScrollPane.repaint();
	}

	public void update(ProgramState state) {
		DefaultListModel stak = ((DefaultListModel) (stack.getModel()));
		stak.clear();

		int addr =0;
		for (Data item: state.getStack())
			stak.add(0, new MemoryCell(item, addr++)); 

		stack.ensureIndexIsVisible(0);
		stackScrollPane.revalidate();
		stackScrollPane.repaint();
	}

	private class StackCellRenderer extends CellRenderer implements ListCellRenderer {

		public StackCellRenderer() {
			super();
		}

		public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
	
			if (isSelected) setBackground(list.getSelectionBackground());
	
			MemoryCell cell = (MemoryCell) value;
	
			setText(cell.getText());
			setBackground(cell.getColor());
			setToolTipText(cell.getToolTip());
	
			setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			return this;
		}
	}
}
