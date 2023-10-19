package edu.cornell.cs.sam.ui.components;
import edu.cornell.cs.sam.core.Memory;
import edu.cornell.cs.sam.core.HeapAllocator;
import static edu.cornell.cs.sam.core.HeapAllocator.Allocation;
import static edu.cornell.cs.sam.core.Memory.Data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Iterator;

public class SamHeapPanel extends JPanel {

	/* Heap GUI components */
	private JScrollPane heapScrollPane;
	private JTree heap;	

	public SamHeapPanel() {
		super();
		setPreferredSize(new Dimension(200, 350));
		setMinimumSize(new Dimension(100, 100));
		setLayout(new BorderLayout());
	
		heap = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("Heap")));
		heap.setCellRenderer(new HeapCellRenderer());
		heap.setRootVisible(false);
		ToolTipManager.sharedInstance().registerComponent(heap);

		heapScrollPane = new JScrollPane(heap);
		heapScrollPane.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		
		add(new JLabel("Heap:"), BorderLayout.NORTH);
		add(heapScrollPane, BorderLayout.CENTER);
	}

	public void update(Memory mem) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Heap");
		DefaultMutableTreeNode current = null;

		HeapAllocator allocator = mem.getHeapAllocator();
	
		if (allocator == null) return;

		Iterator<Allocation> iter = allocator.getAllocations();		
		while (iter.hasNext()) {
			Allocation alloc = iter.next();
			int addr = alloc.getAddr();
			int size = alloc.getSize();

			current = new DefaultMutableTreeNode("Allocation (Size: " + alloc.getSize() + ")");
			root.add(current);
			
			int i = 0;
			for (Data value: mem.getAllocation(alloc))
				current.add(new DefaultMutableTreeNode(new MemoryCell(value, addr + i++)));
		}

		((DefaultTreeModel) heap.getModel()).setRoot(root);
		((DefaultTreeModel) heap.getModel()).reload();
		heapScrollPane.revalidate();
		heapScrollPane.repaint();
	}

	private class HeapCellRenderer extends DefaultTreeCellRenderer {
		Color defaultBackgroundNonSelectionColor;

		public HeapCellRenderer() {
			super();
			defaultBackgroundNonSelectionColor = getBackgroundNonSelectionColor();
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			setBackgroundNonSelectionColor(defaultBackgroundNonSelectionColor);

			super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

			if (!(value instanceof DefaultMutableTreeNode) ||
				((DefaultMutableTreeNode) value).getUserObject() == null ||
				!(((DefaultMutableTreeNode) value).getUserObject() instanceof MemoryCell))
				return this;

			MemoryCell cell = (MemoryCell) ((DefaultMutableTreeNode) value).getUserObject();
			setText(cell.getText());
			setToolTipText(cell.getToolTip());
			setBackgroundNonSelectionColor(cell.getColor());
			return this;
		}
	}
}

