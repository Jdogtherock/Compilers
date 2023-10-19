package edu.cornell.cs.sam.ui.components;
import edu.cornell.cs.sam.core.Processor;
import edu.cornell.cs.sam.utils.ProgramState;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

public class SamRegistersPanel extends JPanel {

	/* Registers Components */
	private JLabel pcRegister;
	private JLabel fbrRegister;
	private JLabel spRegister;
	private JLabel hpRegister;
	private JPanel registersInnerPanel;

	public SamRegistersPanel() {
		super();
		setPreferredSize(new Dimension(100, 150));
		setLayout(new BorderLayout());	
	
		registersInnerPanel = new JPanel();
		registersInnerPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));

		GridBagLayout lr = new GridBagLayout();
		GridBagConstraints cr = new GridBagConstraints();
		cr.fill = GridBagConstraints.BOTH;
		cr.insets = new Insets(5, 5, 5, 5);
		registersInnerPanel.setLayout(lr);
		registersInnerPanel.setBackground(new Color(220, 220, 220));

		cr.anchor = GridBagConstraints.WEST;
		GridBagUtils.addComponent(new JLabel("PC:"), registersInnerPanel, lr, cr, 0, 0, 1, 1, 1, 1);
		GridBagUtils.addComponent(new JLabel("FBR:"), registersInnerPanel, lr, cr, 0, 1, 1, 1, 1, 1);
		GridBagUtils.addComponent(new JLabel("SP:"), registersInnerPanel, lr, cr, 0, 2, 1, 1, 1, 1);

		pcRegister = new JLabel("");
		fbrRegister = new JLabel("");
		spRegister = new JLabel("");
		hpRegister = new JLabel("");

		cr.anchor = GridBagConstraints.EAST;
		GridBagUtils.addComponent(pcRegister, registersInnerPanel, lr, cr, 1, 0, 1, 1, 1, 1);
		GridBagUtils.addComponent(fbrRegister, registersInnerPanel, lr, cr, 1, 1, 1, 1, 1, 1);
		GridBagUtils.addComponent(spRegister, registersInnerPanel, lr, cr, 1, 2, 1, 1, 1, 1);
		GridBagUtils.addComponent(hpRegister, registersInnerPanel, lr, cr, 1, 3, 1, 1, 1, 1);

		add(new JLabel("Registers:"), BorderLayout.NORTH);
		add(registersInnerPanel, BorderLayout.CENTER);
        }

	public void update(Processor proc) {
		pcRegister.setText("" + proc.get(Processor.PC));
		fbrRegister.setText("" + proc.get(Processor.FBR));
		spRegister.setText("" + proc.get(Processor.SP));
        }

	public void update(ProgramState state) {
		pcRegister.setText("" + state.getRegisters()[Processor.PC]);
		fbrRegister.setText("" + state.getRegisters()[Processor.FBR]);
		spRegister.setText("" + state.getRegisters()[Processor.SP]);
	}
}

