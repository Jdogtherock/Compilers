package edu.cornell.cs.sam.ui;

import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.ui.components.SamAboutDialog;
import edu.cornell.cs.sam.utils.RegistrationSystem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This provides various shared functions used among all the SaM UI classes
 */

public abstract class SamUI{
	public static void exit() {
		int[] targets = { SamGUI.classID, SamCapture.classID, SamTester.classID };			

		for (int id : targets) {
			Collection<?> cl =  RegistrationSystem.getElements(id);

			if (cl != null) 
				for (Object element: cl) 
					if (!((SamUI.Component) element).close()) return;	
		}

		System.exit(0);
	}

	private static void printUsage(String caller) {
		if (caller.equals("SamGUI"))
			System.out.print("SaM Simulator");
		else if (caller.equals("SamCapture"))
			System.out.print("SaM Capture Viewer");
		else if (caller.equals("SamTester"))
			System.out.print("SaM Tester");
		System.out.println(" (SaM " + Sys.SAM_VERSION + ")\n");
		System.out.println("Usage:");
		System.out.println("java ui." + caller + " [-<program> [<filename>]]");
		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		} 
		catch (ClassNotFoundException e) { 
			System.err.println("Unable to initialize look and feel.");
		}
		catch (InstantiationException e) { 
			System.err.println("Unable to initialize look and feel.");
		}
		catch (IllegalAccessException e) { 
			System.err.println("Unable to intiailize look and feel.");
		}
		catch (UnsupportedLookAndFeelException e) {
			System.err.println("Unable to initialize look and feel.");
		}

		String filename = null;
		String component = "SamGUI";

		/* Parse arguments */
		if (args.length > 2)
			printUsage(component);

		else if (args.length >= 1) {
			component = "";
			if (args[0].equals("-gui"))
				component = "SamGUI";
			else if (args[0].equals("-capture"))
				component = "SamCapture";
			else if (args[0].equals("-tester"))
				component = "SamTester";
			else if(args.length == 2)
				printUsage("SamGUI");
			
			if(component == null){
				component = "SamGUI";
				filename = args[0];
			}
			else if(args.length == 2)
				filename = args[1];
		}

		/* Launch component */
		if (component.equals("SamGUI"))
			SamGUI.startUI(filename);
		else if (component.equals("SamCapture"))
			SamCapture.startUI(filename);
		else if (component.equals("SamTester"))
			SamTester.startUI(filename);
		else printUsage("SamGUI");

	}

	public static JMenu createSamMenu(final SamUI.Component parent) {
		JMenu samMenu = new JMenu("SaM");
		samMenu.setMnemonic(KeyEvent.VK_S);
		JMenuItem simulatorMenuItem = samMenu.add("Simulator");
		simulatorMenuItem.setMnemonic(KeyEvent.VK_S);
		simulatorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		simulatorMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamGUI.startUI();
			}
		});
		JMenuItem captureMenuItem = samMenu.add("Capture Viewer");
		captureMenuItem.setMnemonic(KeyEvent.VK_C);
		captureMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		captureMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamCapture.startUI();
			}
		});
		JMenuItem testerMenuItem = samMenu.add("Tester");
		testerMenuItem.setMnemonic(KeyEvent.VK_T);
		testerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		testerMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamTester.startUI();
			}
		});
		samMenu.addSeparator();
		JMenuItem aboutMenuItem = samMenu.add("About SaM");
		aboutMenuItem.setMnemonic(KeyEvent.VK_A);
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.getAboutDialog().setVisible(true);
			}
		});
		return samMenu;
	}

	public static interface Component {
		public SamAboutDialog getAboutDialog();

		public boolean close();

	}
}
