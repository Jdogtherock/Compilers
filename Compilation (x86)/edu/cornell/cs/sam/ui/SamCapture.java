package edu.cornell.cs.sam.ui;

import edu.cornell.cs.sam.core.Program;
import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.core.instructions.Instruction;
import edu.cornell.cs.sam.ui.components.GridBagUtils;
import edu.cornell.cs.sam.ui.components.FileDialogManager;
import edu.cornell.cs.sam.ui.components.SamAboutDialog;
import edu.cornell.cs.sam.ui.components.SamColorReferenceDialog;
import edu.cornell.cs.sam.ui.components.SamRegistersPanel;
import edu.cornell.cs.sam.ui.components.SamStackPanel;
import edu.cornell.cs.sam.ui.components.StatusBar;
import edu.cornell.cs.sam.utils.ProgramState;
import edu.cornell.cs.sam.utils.RegistrationSystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Allows the display of past program states
 */
public class SamCapture extends JFrame implements SamUI.Component {
	
	protected final static int CHOOSER_OPEN = 0;
	
	protected final static int CHOOSER_SAVE = 1;

	protected Container contentPane;

	protected JPanel mainPanel;

	private GridBagConstraints c = new GridBagConstraints();

	private GridBagLayout l = new GridBagLayout();

	protected JList instructionList;

	protected JScrollPane instructionListView;

	protected JMenuItem increaseDisplayMenuItem;

	protected JMenuItem removeDisplayMenuItem;

	protected StepDisplay stepDisplays[];

	protected List<? extends ProgramState> steps;

	protected JPanel instructionListPanel;

	protected File sourceFile = null;

	protected String filename = null;

	protected Program program;

	protected StatusBar statusBar;

	protected JMenuItem saveAsMenuItem;

	protected JMenuItem openSimMenuItem;

	protected JDialog colorsDialog;

	protected SamAboutDialog aboutDialog;
	
	protected FileDialogManager fileDialogs;

        public static int classID = RegistrationSystem.getNextUID();
	
	/**
	 * Creates a new SamCapture
	 */
	protected SamCapture() {
		RegistrationSystem.register(classID, this);
		fileDialogs = new FileDialogManager(2);

		// Set up basic top level layout
		contentPane = getContentPane();
		setTitle("Capture Viewer");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		contentPane.setLayout(new BorderLayout());
		mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		statusBar = new StatusBar();
		contentPane.add(statusBar, BorderLayout.SOUTH);

		// figure out how many step displays to show
		int sdCount = (((int) getToolkit().getScreenSize().getWidth()) - 205 - 10) / 155;
		stepDisplays = new StepDisplay[sdCount];

		// Add components
		createComponents();

		createMenus();

		addNotify();
		setWindowListeners();
		resize();
		pack();
	}

	protected void start(List<? extends ProgramState> steps, Program prog, String filename) {
		setTitle("Capture Viewer - " + filename);
		this.filename = filename;
		this.steps = steps;
		if (steps.size() == 0) {
			close();
			return;
		}
		while (stepDisplays.length > steps.size())
			removeStepDisplay();
		program = prog;
		DefaultListModel instructions = (DefaultListModel) (instructionList.getModel());
		if (prog != null) {
			for (int i = 0; i < steps.size(); i++) {
				Instruction ins = program.getInst(steps.get(i).getLastPC());
				ins.setProgram(prog);
				instructions.addElement(ins);
			}
			loadInstruction(0);
			instructionList.setSelectedIndex(0);
			saveAsMenuItem.setEnabled(true);
			openSimMenuItem.setEnabled(true);
		}
		start();
	}

	protected void start() {
		pack();
		setVisible(true);

		aboutDialog = getAboutDialog();
	}

	public Dimension getMinimumSize() {
		return new Dimension(stepDisplays.length * 150 + 200, 560);
	}

	public Dimension getPreferredSize() {
		return new Dimension(stepDisplays.length * 150 + 200, 560);
	}

	private void resize() {
		setSize(stepDisplays.length * 160 + 210, 560);
	}

	/**
	 * Loads the provided file
	 * 
	 * @param secFile
	 *            The file to load
	 */
	public void loadFile(File secFile) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(secFile));
			String version = (String) ois.readObject();
			Program inProg = (Program) ois.readObject();
			List<ProgramState> inSteps = (List<ProgramState>) ois.readObject();
			ois.close();
			sourceFile = secFile;
			filename = sourceFile.getName();
			start(inSteps, inProg, secFile.getName());
		}
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Could not find file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Invalid file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (ClassCastException e) {
			JOptionPane.showMessageDialog(this, "Invalid file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves the provided file
	 */
	public void saveCapture() {
		File cdir = (sourceFile == null) ? null : sourceFile.getParentFile();
		File secFile = fileDialogs.getSaveFile(this, "sec", "SaM Execution Capture", cdir, CHOOSER_SAVE);
		if (secFile == null) return;

		if (steps == null) return;
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(secFile));
			oos.writeObject(Sys.SAM_VERSION);
			oos.writeObject(program);
			oos.writeObject(steps);
			oos.close();
			sourceFile = secFile;
			filename = sourceFile.getName();
			setTitle("Capture Viewer - " + secFile.getName());
		}
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Could not find file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setWindowListeners() {
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
			}
		});
		// Add Window Listener to handle window closing
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
	}

	// Create the menu structure and add event listeners
	private void createMenus() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem openMenuItem = fileMenu.add("Open...");
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File cdir = (sourceFile == null) ? null : sourceFile.getParentFile();
				File selected = fileDialogs.getOpenFile(SamCapture.this, 
						"sec", "SaM Execution Capture", cdir, CHOOSER_OPEN);
				if (selected != null) loadFile(selected);
			}
		});
		openSimMenuItem = fileMenu.add("Open Program in Simulator");
		openSimMenuItem.setMnemonic(KeyEvent.VK_M);
		openSimMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		openSimMenuItem.setEnabled(false);
		openSimMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamGUI.startUI(program, new String(filename), null);
			}
		});
		saveAsMenuItem = fileMenu.add("Save As...");
		saveAsMenuItem.setMnemonic(KeyEvent.VK_S);
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveAsMenuItem.setEnabled(false);
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCapture();
			}
		});
		JMenuItem closeMenuItem = fileMenu.add("Close Window");
		closeMenuItem.setMnemonic(KeyEvent.VK_C);
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		closeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		JMenuItem exitMenuItem = fileMenu.add("Exit");
		exitMenuItem.setMnemonic(KeyEvent.VK_X);
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
				SamUI.exit();
			}
		});
		JMenu displayMenu = new JMenu("Display");
		displayMenu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(displayMenu);
		increaseDisplayMenuItem = displayMenu.add("Add Instruction Display");
		increaseDisplayMenuItem.setMnemonic(KeyEvent.VK_I);
		increaseDisplayMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
		increaseDisplayMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addStepDisplay();
			}
		});
		removeDisplayMenuItem = displayMenu.add("Remove Instruction Display");
		removeDisplayMenuItem.setMnemonic(KeyEvent.VK_I);
		if (stepDisplays.length <= 1) removeDisplayMenuItem.setEnabled(false);
		removeDisplayMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		removeDisplayMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeStepDisplay();
			}
		});
		displayMenu.addSeparator();
		JMenuItem colorsMenuItem = displayMenu.add("Stack Colors Reference");
		colorsMenuItem.setMnemonic(KeyEvent.VK_S);
		colorsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayColorReference();
			}
		});

		menuBar.add(SamUI.createSamMenu(this));
	}

	// Creates the GUI components
	private void createComponents() {
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 5, 5, 5);

		// Set up main panel
		mainPanel.setLayout(l);

		instructionListPanel = buildInstructionListPanel();
		// Add components
		GridBagUtils.addComponent(instructionListPanel, mainPanel, l, c, 0, 0, 1, 1, 1, 1);
		for (int i = 0; i < stepDisplays.length; i++) {
			stepDisplays[i] = new StepDisplay();
			GridBagUtils.addComponent(stepDisplays[i], mainPanel, l, c, 1 + i, 0, 1, 1, 1, 1);
		}
	}

	// Build Program Code Panel
	private JPanel buildInstructionListPanel() {
		instructionList = new JList(new DefaultListModel());
		instructionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		instructionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				loadInstruction(((JList) e.getSource()).getSelectedIndex());
			}
		});
		JPanel instructionListPanel = new JPanel();
		instructionListPanel.setPreferredSize(new Dimension(200, 350));
		instructionListPanel.setMinimumSize(new Dimension(200, 350));
		instructionListPanel.setLayout(new BorderLayout());
		instructionListView = new JScrollPane(instructionList);
		instructionListView.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		instructionListPanel.add(instructionListView, BorderLayout.CENTER);
		instructionListPanel.add(new JLabel("Instructions Executed:"), BorderLayout.NORTH);
		return instructionListPanel;
	}

	void loadInstruction(int index) {
		if (index < 0) return;
		// find center point
		int currentPosition;
		int startIndex;
		int endIndex;
		if (stepDisplays.length % 2 == 1) {
			currentPosition = (stepDisplays.length - 1) / 2;
		}
		else {
			currentPosition = ((stepDisplays.length) / 2) - 1;
		}
		while ((index - currentPosition) < 0)
			currentPosition--;
		while ((index + (stepDisplays.length - 1 - currentPosition)) >= (steps.size()))
			currentPosition++;
		startIndex = index - currentPosition;
		endIndex = index + (stepDisplays.length - currentPosition - 1);
		if (endIndex > steps.size() - 1) endIndex = steps.size() - 1;
		for (int i = 0; startIndex + i <= endIndex; i++) {
			stepDisplays[i].load(steps.get(startIndex + i), program);
			if (i == currentPosition)
				stepDisplays[i].setCurrent(true);
			else
				stepDisplays[i].setCurrent(false);
		}
	}

	void addStepDisplay() {
		if (steps != null && stepDisplays.length == steps.size()) {
			increaseDisplayMenuItem.setEnabled(false);
			return;
		}
		StepDisplay nsd[] = new StepDisplay[stepDisplays.length + 1];
		System.arraycopy(stepDisplays, 0, nsd, 0, stepDisplays.length);
		stepDisplays = nsd;
		stepDisplays[stepDisplays.length - 1] = new StepDisplay();
		resize();
		GridBagUtils.addComponent(stepDisplays[stepDisplays.length - 1], 
				mainPanel, l, c, stepDisplays.length, 0, 1, 1, 1, 1);
		validate();
		loadInstruction(instructionList.getSelectedIndex());
		if (stepDisplays.length == 2) removeDisplayMenuItem.setEnabled(true);
		if (steps != null && stepDisplays.length == steps.size()) increaseDisplayMenuItem.setEnabled(false);
	}

	void removeStepDisplay() {
		if (stepDisplays.length == 1) return;
		StepDisplay nsd[] = new StepDisplay[stepDisplays.length - 1];
		System.arraycopy(stepDisplays, 0, nsd, 0, stepDisplays.length - 1);
		mainPanel.remove(stepDisplays[stepDisplays.length - 1]);
		stepDisplays = nsd;
		resize();
		pack();
		loadInstruction(instructionList.getSelectedIndex());
		if (stepDisplays.length == 1) removeDisplayMenuItem.setEnabled(false);
	}

	// Help Menu
	private void displayColorReference() {
		if (colorsDialog == null) {
			colorsDialog = new SamColorReferenceDialog(this);
		}
		colorsDialog.setVisible(true);
	}

	public SamAboutDialog getAboutDialog() {
		if (aboutDialog == null) aboutDialog = new SamAboutDialog("SaM", Sys.SAM_VERSION, "SaM Tester", this);
		return aboutDialog;
	}

	/**
	 * Starts SamCapture without loading any file
	 */
	public static void startUI() {
		startUI(null);
	}

	public boolean close() {
		RegistrationSystem.unregister(classID, this);
		dispose();
		return true;
	}

	/**
	 * Starts SamCapture and loads a file
	 * 
	 * @param filename
	 *            the file to load, or null to not load a file
	 */
	public static void startUI(String filename) {
		SamCapture gui = new SamCapture();
		gui.start();
		if (filename != null) gui.loadFile(new File(filename));
	}

	/**
	 * Starts the UI and loads a vector of steps, a program, and a filename
	 */
	public static void startUI(List<? extends ProgramState> steps, Program prog, String filename) {
		SamCapture gui = new SamCapture();
		gui.start(steps, prog, filename);
	}
}

class StepDisplay extends JPanel {
	protected JLabel instructionLabel;
	protected SamStackPanel stack;
	protected SamRegistersPanel registers;

	/**
	 * Builds the a panel that allows the display of the state of the processor
	 * at one instruction
	 */
	public StepDisplay() {
		setPreferredSize(new Dimension(150, 450));
		setMinimumSize(new Dimension(150, 300));
		setLayout(new BorderLayout());
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), ""));

		stack = new SamStackPanel();
		stack.bindSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					((JList) e.getSource()).clearSelection();
			}
		});

		add(stack, BorderLayout.CENTER);
			
		registers = new SamRegistersPanel();
		add(registers, BorderLayout.SOUTH);
	}

	public void setCurrent(boolean active) {
		Color defaultTextColor = Color.BLACK;
		Color defaultBorderColor = getBackground();
		Color currentColor = Color.BLUE;

		if (active) {
			((TitledBorder) getBorder()).setTitleColor(currentColor);
			((TitledBorder) getBorder()).setBorder(
				new EtchedBorder(EtchedBorder.LOWERED, currentColor.brighter(), currentColor.darker())
			);
		}
		else {
			((TitledBorder) getBorder()).setTitleColor(defaultTextColor);
			((TitledBorder) getBorder()).setBorder(
				new EtchedBorder(EtchedBorder.LOWERED, defaultBorderColor.brighter(), 
				defaultBorderColor.darker())
			);
		}
	}

	public void load(ProgramState state, Program program) {
		setBorder(new TitledBorder(
			new EtchedBorder(EtchedBorder.LOWERED), "After " + program.getInst(state.getLastPC()).toString())
		);

		stack.update(state);
		registers.update(state);
	}
}

