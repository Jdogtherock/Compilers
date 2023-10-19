package edu.cornell.cs.sam.ui;

import edu.cornell.cs.sam.core.AssemblerException;
import edu.cornell.cs.sam.core.HeapAllocator;
import static edu.cornell.cs.sam.core.HeapAllocator.Allocation;
import edu.cornell.cs.sam.core.Memory;
import static edu.cornell.cs.sam.core.Memory.Type;
import edu.cornell.cs.sam.core.Processor;
import edu.cornell.cs.sam.core.Program;
import edu.cornell.cs.sam.core.SamAssembler;
import edu.cornell.cs.sam.core.SymbolTable;
import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.core.SystemException;
import edu.cornell.cs.sam.core.Video;
import edu.cornell.cs.sam.core.instructions.Instruction;
import edu.cornell.cs.sam.ui.components.CellRenderer;
import edu.cornell.cs.sam.ui.components.FileDialogManager;
import edu.cornell.cs.sam.ui.components.GridBagUtils;
import edu.cornell.cs.sam.ui.components.SamAboutDialog;
import edu.cornell.cs.sam.ui.components.SamColorReferenceDialog;
import edu.cornell.cs.sam.ui.components.SamHeapPanel;
import edu.cornell.cs.sam.ui.components.SamStackPanel;
import edu.cornell.cs.sam.ui.components.SamRegistersPanel;
import edu.cornell.cs.sam.ui.components.StatusBar;
import edu.cornell.cs.sam.utils.ClassFileLoader;
import edu.cornell.cs.sam.utils.ProgramState;
import edu.cornell.cs.sam.utils.RegistrationSystem;
import edu.cornell.cs.sam.utils.SamThread;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Provides a GUI for the SaM Simulator that can run SaM programs and capture
 * their state after each instruction execution for later review. To start, call
 * one of the SamGUI.startUI calls.
 */
public class SamGUI extends JFrame implements Video, SamUI.Component, SamThread.ThreadParent {
	private final static String BR = System.getProperty("line.separator");

	private static enum ExecutionSpeed { 
		SPEED_NONE(0, "Very Fast Execution"),
		SPEED_VF(0, "Very Fast Execution"),
		SPEED_F(25, "Fast Execution"),
		SPEED_M(100, "Medium Execution"),
		SPEED_S(400, "Slow Execution"),
		SPEED_VS(1600, "Very Slow Execution");
	
		private int delay;
		private String text;

		ExecutionSpeed(int delay, String text) {
			this.delay = delay;
			this.text = text;
		}

		public int getCode() {
			return ordinal();
		}

		public String getText() {
			return text;
		}

		public int getDelay() {
			return delay;
		}

		public static ExecutionSpeed fromCode(int code) {
			switch(code) {
				case 0: return SPEED_NONE;
				case 1: return SPEED_VF;
				case 2: return SPEED_F;
				case 3: return SPEED_M;
				case 4: return SPEED_S;
				case 5: return SPEED_VS;
				default: return SPEED_NONE;
			}
		}
	}

	/* File management */
	private File sourceFile = null;
	private String filename;	
	private FileDialogManager fileDialogs;
	private final static int CHOOSER_OPEN = 0;
	private final static int CHOOSER_SAVE = 1;
	private final static int CHOOSER_LOADINST = 2;

	/* Saved preferences */
	private Preferences prefs;

	/* Core integration */
	private Sys sys;
	private Processor proc;
	private Memory mem;

	/* Backing processor thread */
	private RunThread runThread;
	private int runDelay = 64;

	/* UI integration */
	public static int classID = RegistrationSystem.getNextUID();

	/* Components, and component layout */
	private JPanel mainPanel, programCodePanel, consolePanel, buttonPanel;
	private SamStackPanel stackPanel;
	private SamHeapPanel heapPanel;
	private SamRegistersPanel registerPanel;
	private StatusBar statusBar;
	private GridBagLayout componentLayout;
	private GridBagConstraints componentLayoutCons;

	/* Menu */
	private JMenuItem openMenuItem, saveAsMenuItem, loadInstructionsMenuItem, resetMenuItem, runMenuItem, 
		captureMenuItem, stepMenuItem, stopMenuItem, toggleBreakpointMenuItem;
	private JMenu speedMenu;

	/* Buttons */
	private JButton openButton, resetButton, runButton, captureButton, stepButton, stopButton;

	/* Supplementary dialogs */
	private JDialog colorsDialog = null;
	private SamAboutDialog aboutDialog = null;

	/* Code Panel features */
	private JList programCode;
	private JScrollPane programCodeView;
	private boolean breakpointEditingEnabled = false;
	private BreakpointList breakpoints = new BreakpointList();
	private boolean breakpointStop = false;
	private int lastExecuted = -1;

	/* Capture feature */
	private boolean capture = false;
	private List<ProgramState> steps;

	/* Output console */
	private JTextArea simulatorOutput;	
	
	/* State management */
	private int curStatus = DEFAULT;
	private static final int DEFAULT = 0;
	private static final int RUNCOMPLETED = 1;
	private static final int READYTORUN = 2;
	private static final int RUNNING = 3;
	private static final int CAPTURING = 4;
	private static final int STOPPED = 5;

	/**
	 * Creates a new SamGUI with an existing system Use start() to actually show
	 * the window Use loadFile() to load a file at startup
	 * 
	 * @param sys
	 *            The system to use
	 */
	protected SamGUI(Sys sys) {
		System.setProperty("sun.awt.noerasebackground", "true");
		RegistrationSystem.register(classID, this);
		fileDialogs = new FileDialogManager(3);

		// Set up basic top level layout
		Container contentPane = getContentPane();
		setTitle("SaM Simulator");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		contentPane.setLayout(new BorderLayout());
		mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		statusBar = new StatusBar();
		contentPane.add(statusBar, BorderLayout.SOUTH);

		// Set up SaM
		this.sys = sys;
		proc = sys.cpu();
		mem = sys.mem();
		sys.setVideo(this);

		prefs = Preferences.userRoot().node("/edu/cornell/cs/SaM/SamGUI");

		// Add components
		createComponents();

		createMenus();

		reset();

		addNotify();
		setWindowListeners();
		pack();
	}

	// Sets the listeners to listent for window closing and resizing
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

	// Creates the components
	private void createComponents() {
		// Set up panels
		programCodePanel = buildProgramCodePanel();
		registerPanel = new SamRegistersPanel();
		stackPanel = new SamStackPanel();
		consolePanel = buildConsolePanel();
		buttonPanel = buildButtonPanel();
		heapPanel = new SamHeapPanel();

		componentLayoutCons = new GridBagConstraints();
		componentLayoutCons.fill = GridBagConstraints.BOTH;
		componentLayoutCons.anchor = GridBagConstraints.CENTER;
		componentLayoutCons.insets = new Insets(5, 5, 5, 5);

		// Set up main panel
		componentLayout = new GridBagLayout();
		mainPanel.setLayout(componentLayout);

		reorderComponents();
	}

	// This repositions the components after one is removed/added
	private void reorderComponents() {
		int width = 0;
		mainPanel.removeAll();
		int position = 0;
		if (prefs.getBoolean("showProgramCodePanel", true)) {
			width += 225;
			GridBagUtils.addComponent(programCodePanel, mainPanel, componentLayout, 
				componentLayoutCons, position, 0, 1, 2, 1, 1);
			position++;
		}
		if (prefs.getBoolean("showStackPanel", true)) {
			width += 175;
			GridBagUtils.addComponent(stackPanel, mainPanel, componentLayout, 
				componentLayoutCons, position, 0, 1, 2, 1, 1);
			position++;
		}
		if (prefs.getBoolean("showHeapPanel", false)) {
			width += 225;
			GridBagUtils.addComponent(heapPanel, mainPanel, componentLayout, 
				componentLayoutCons, position, 0, 1, 2, 1, 1);
			position++;
		}

		GridBagUtils.addComponent(registerPanel, mainPanel, componentLayout, 
			componentLayoutCons, position, 0, 1, 1, 0, 0);
		Insets oldInsets = componentLayoutCons.insets;
		componentLayoutCons.insets = new Insets(5, 5, 1, 5);
		GridBagUtils.addComponent(buttonPanel, mainPanel, componentLayout, 
			componentLayoutCons, position, 1, 1, 1, 0, 1);
		position++;
		width += 125;

		componentLayoutCons.insets = oldInsets;
		GridBagUtils.addComponent(consolePanel, mainPanel, componentLayout, 
			componentLayoutCons, 0, 2, position, 1, 1, 0.1);

		validate();
		pack();
		setSize(new Dimension((width < 560) ? 560 : width, 560));
	}

	// Build Program Code Panel
	public JPanel buildProgramCodePanel() {
		programCode = new JList(new DefaultListModel());
		programCode.setCellRenderer(new ProgramCodeCellRenderer(breakpoints));
		programCode.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		programCode.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList source = (JList) e.getSource();
				toggleBreakpointMenuItem.setEnabled(breakpointEditingEnabled && 
					(source.getSelectedIndex() != -1));
			}
		});
		programCode.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 2) return;
				toggleBreakpoint();
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
		});

		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(150, 350));
		p.setMinimumSize(new Dimension(150, 100));
		p.setLayout(new BorderLayout());
		programCodeView = new JScrollPane(programCode);
		programCodeView.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		p.add(programCodeView, BorderLayout.CENTER);
		p.add(new JLabel("Program Code:"), BorderLayout.NORTH);
		return p;
	}

	// Build Console Panel
	public JPanel buildConsolePanel() {
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(525, 100));
		p.setMinimumSize(new Dimension(100, 100));
		p.setLayout(new BorderLayout());
		simulatorOutput = new JTextArea();
		JScrollPane simulatorScrollPane = new JScrollPane(simulatorOutput);
		simulatorScrollPane.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		p.add(simulatorScrollPane, BorderLayout.CENTER);
		p.add(new JLabel("Console:"), BorderLayout.NORTH);
		simulatorOutput.setEditable(false);
		simulatorOutput.setLineWrap(true);
		return p;
	}

	// Build Button Panel
	public JPanel buildButtonPanel() {
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(100, 175));
		p.setLayout(new GridLayout(6, 1, 0, 5));

		openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File cdir = (sourceFile == null) ? null : sourceFile.getParentFile();
				File selected = fileDialogs.getOpenFile(SamGUI.this, "sam", 
						"SaM Program", cdir, CHOOSER_OPEN);
				if (selected != null) {
					reset();
					loadFile(selected);
				}
			}
		});
		p.add(openButton);

		stepButton = new JButton("Step");
		stepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (capture) resetCapture();
				step();
			}
		});
		p.add(stepButton);

		runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (capture) resetCapture();
				run();
			}
		});
		p.add(runButton);

		captureButton = new JButton("Capture");
		captureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (capture) resetCapture();
				capture();
			}
		});
		p.add(captureButton);

		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
		p.add(stopButton);

		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		p.add(resetButton);

		return p;
	}

	// Create the menu structure and add event listeners
	private void createMenus() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		openMenuItem = fileMenu.add("Open...");
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File cdir = (sourceFile == null) ? null : sourceFile.getParentFile();
				File selected = fileDialogs.getOpenFile(SamGUI.this, "sam", 
						"SaM Program", cdir, CHOOSER_OPEN);
				if (selected != null) {
					reset();
					loadFile(selected);
				}
			}
		});
		loadInstructionsMenuItem = fileMenu.add("Load Instruction...");
		loadInstructionsMenuItem.setMnemonic(KeyEvent.VK_L);
		loadInstructionsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = fileDialogs.getOpenFile(SamGUI.this, "class", 
						"Instruction Bytecode", null, CHOOSER_LOADINST);
				if(f != null) loadInstruction(f);
			}
		});
		saveAsMenuItem = fileMenu.add("Save As...");
		saveAsMenuItem.setMnemonic(KeyEvent.VK_S);
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveAsMenuItem.setEnabled(false);
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File cdir = (sourceFile == null) ? null : sourceFile.getParentFile();
				File savefile = fileDialogs.getSaveFile(SamGUI.this, "sam", 
						"SaM Program", cdir, CHOOSER_SAVE);
				if (savefile != null) save(savefile);
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

		JMenu runMenu = new JMenu("Run");
		runMenu.setMnemonic(KeyEvent.VK_R);
		menuBar.add(runMenu);
		runMenuItem = runMenu.add("Run");
		runMenuItem.setMnemonic(KeyEvent.VK_R);
		runMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		runMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (capture) resetCapture();
				run();
			}
		});
		stepMenuItem = runMenu.add("Step");
		stepMenuItem.setMnemonic(KeyEvent.VK_T);
		stepMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		stepMenuItem.setEnabled(false);
		stepMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (capture) resetCapture();
				step();
			}
		});
		stopMenuItem = runMenu.add("Stop");
		stopMenuItem.setMnemonic(KeyEvent.VK_S);
		stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		stopMenuItem.setEnabled(false);
		stopMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
		resetMenuItem = runMenu.add("Reset");
		resetMenuItem.setMnemonic(KeyEvent.VK_E);
		resetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		resetMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		speedMenu = new JMenu("Execution Speed");
		speedMenu.setMnemonic(KeyEvent.VK_X);
		runMenu.add(speedMenu);
		ButtonGroup speedGroup = new ButtonGroup();

		ExecutionSpeed speed = ExecutionSpeed.fromCode(
			prefs.getInt("executionSpeed", ExecutionSpeed.SPEED_NONE.getCode())
		);
		for (ExecutionSpeed setting : EnumSet.range(ExecutionSpeed.SPEED_VF, 
				ExecutionSpeed.SPEED_VS)) {
			JMenuItem item = new JRadioButtonMenuItem(setting.getText());
			speedMenu.add(item);
			speedGroup.add(item);
			if (setting.compareTo(speed) == 0) {
				item.setSelected(true);

                        	runDelay = speed.getDelay();
                        	prefs.putInt("executionSpeed", speed.getCode());
                        	prefs.putInt("customExecutionSpeedSetting", 0);
			}

			final ExecutionSpeed sp = setting;
			item.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						runDelay = sp.getDelay();
                                		prefs.putInt("executionSpeed", sp.getCode());
						prefs.putInt("customExecutionSpeedSetting", 0);
					}
				}
			);
		}	

		JMenu debugMenu = new JMenu("Debug");
		debugMenu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(debugMenu);
		captureMenuItem = debugMenu.add("Capture");
		captureMenuItem.setMnemonic(KeyEvent.VK_C);
		captureMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		captureMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (capture) resetCapture();
				capture();
			}
		});
		toggleBreakpointMenuItem = debugMenu.add("Toggle Breakpoint");
		toggleBreakpointMenuItem.setMnemonic(KeyEvent.VK_B);
		toggleBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		toggleBreakpointMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleBreakpoint();
			}
		});
		JMenuItem deleteBreakpointsMenuItem = debugMenu.add("Remove All Breakpoints");
		deleteBreakpointsMenuItem.setMnemonic(KeyEvent.VK_R);
		deleteBreakpointsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteBreakpoints();
			}
		});

		JMenu displayMenu = new JMenu("Display");
		displayMenu.setMnemonic(KeyEvent.VK_P);
		menuBar.add(displayMenu);
		JMenuItem colorsMenuItem = displayMenu.add("Stack Colors Reference");
		colorsMenuItem.setMnemonic(KeyEvent.VK_C);
		colorsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayColorReference();
			}
		});
		displayMenu.addSeparator();
		JCheckBoxMenuItem programCodeMenuItem = new JCheckBoxMenuItem("Program Code");
		programCodeMenuItem.setMnemonic(KeyEvent.VK_C);
		displayMenu.add(programCodeMenuItem);
		if (prefs.getBoolean("showProgramCodePanel", true)) programCodeMenuItem.setState(true);
		programCodeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JCheckBoxMenuItem) e.getSource()).getState())
					prefs.putBoolean("showProgramCodePanel", true);
				else
					prefs.putBoolean("showProgramCodePanel", false);
				reorderComponents();
				updateProgram();
			}
		});
		JCheckBoxMenuItem stackMenuItem = new JCheckBoxMenuItem("Stack");
		stackMenuItem.setMnemonic(KeyEvent.VK_S);
		displayMenu.add(stackMenuItem);
		if (prefs.getBoolean("showStackPanel", true)) stackMenuItem.setState(true);
		stackMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					prefs.putBoolean("showStackPanel", true);
					stackPanel.update(mem);
				}
				else
					prefs.putBoolean("showStackPanel", false);
				reorderComponents();
			}
		});
		JCheckBoxMenuItem heapMenuItem = new JCheckBoxMenuItem("Heap");
		heapMenuItem.setMnemonic(KeyEvent.VK_H);
		displayMenu.add(heapMenuItem);
		if (prefs.getBoolean("showHeapPanel", false)) heapMenuItem.setState(true);
		heapMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					prefs.putBoolean("showHeapPanel", true);
					heapPanel.update(mem);
				}
				else
					prefs.putBoolean("showHeapPanel", false);
				reorderComponents();
			}
		});

		menuBar.add(SamUI.createSamMenu(this));
	}

	/**
	 * Destroys the window, system, and any dialogs
	 */
	public boolean close() {
		if (colorsDialog != null) {
			colorsDialog.dispose();
		}
		if (aboutDialog != null) {
			aboutDialog.dispose();
		}
		RegistrationSystem.unregister(classID, this);
		dispose();
		return true;
	}

	/**
	 * Makes the window visible
	 */
	protected void start() {
		pack();
		setVisible(true);

		// Set up the dialogs now to avoid loading time later
		colorsDialog = new SamColorReferenceDialog(this);
		aboutDialog = getAboutDialog();
	}

	// Clears out all textboxes and resets processor
	private void reset() {
		proc.init();
		mem.init();
		programCode.clearSelection();

		if (prefs.getBoolean("showStackPanel", true)) 
			stackPanel.update(mem);

		if (prefs.getBoolean("showHeapPanel", true)) 
			heapPanel.update(mem);

		registerPanel.update(proc);
		updateProgram(true);
		if (proc.getProgram() != null)
			setStatus(READYTORUN);
		else
			setStatus(DEFAULT);
		updateProgram(false);
		simulatorOutput.setText("");
		breakpointStop = false;
		resetCapture();
	}

	private void resetCapture() {
		capture = false;
		steps = new ArrayList<ProgramState>();
	}

	// Updates the program without reloading the data
	private void updateProgram() {
		updateProgram(false);
	}

	// Updates the program - if true, then reload data
	private void updateProgram(boolean update) {
		if (!prefs.getBoolean("showProgramCodePanel", true)) return;

		DefaultListModel prog = (DefaultListModel) (programCode.getModel());
		if (update) {
			prog.clear();
			Program code = proc.getProgram();
			lastExecuted = -1;
			if (code != null) {
				SymbolTable ST = code.getSymbolTable();
				for (int i = 0; i < code.getLength(); i++) {
					String label = ST.resolveSymbol(i);
					prog.addElement(
						new ProgramCodeCellRenderer.ProgramCodeCell(
							i, code.getInst(i).toString(), label
						)
					);
				}
				((ProgramCodeCellRenderer.ProgramCodeCell) prog.get(0)).setExecuting(true);
			}
		}
		if (prog.size() > 0) setNextExecuting(proc.get(Processor.PC));
		programCode.ensureIndexIsVisible(proc.get(Processor.PC));
		programCodeView.revalidate();
		programCodeView.repaint();
	}

	// Toggles a breakpoint
	private void toggleBreakpoint() {
		int ind = programCode.getSelectedIndex();

		if (ind == -1) return;
		if (breakpoints.checkBreakpoint(ind))
			breakpoints.deleteBreakpoint(ind);
		else
			breakpoints.addBreakpoint(ind);

		programCodeView.revalidate();
		programCodeView.repaint();

	}

	// Removes all breakpoints
	private void deleteBreakpoints() {
		breakpoints.deleteAll();
		programCodeView.revalidate();
		programCodeView.repaint();
	}

	/**
	 * Loads the provided file
	 * 
	 * @param samFile
	 *            The file to load
	 */
	public void loadFile(File samFile) {
		try {
			Program prog = SamAssembler.assemble(new FileReader(samFile));
			loadProgram(prog, samFile.getName());
			sourceFile = samFile;
		}
		catch (AssemblerException e) {
			statusBar.setText("Could not open file");
			simulatorOutput.setText("Assembler Error:" + BR + e);
		}
		catch (FileNotFoundException e) {
			statusBar.setText("Could not find file");
			simulatorOutput.setText("Could not find file");
		}
		catch (IOException e) {
			statusBar.setText("Could not load file");
			simulatorOutput.setText("I/O Error while processing file");
		}
	}

	/**
	 * Loads the provided program
	 * 
	 * @param prog
	 *            the program to load
	 * @param filename
	 *            the name of the file being loaded
	 */
	public void loadProgram(Program prog, String filename) {
		proc.init();
		mem.init();
		if (prog == null) {
			setStatus(DEFAULT);
		}
		else {
			try {
				proc.load(prog);
			}
			catch (SystemException e) {
				statusBar.setText("Could not load program");
				simulatorOutput.setText("Processor Error:" + BR + e);
				return;
			}

			breakpoints.deleteAll();
			breakpointStop = false;
			updateProgram(true);
			programCode.clearSelection();
			setStatus(READYTORUN);
		}
		sourceFile = null;
		this.filename = filename;
		setTitle("SaM Simulator - " + filename);
	}

	/**
	 * Saves the provided file
	 * 
	 * @param samFile
	 *            The file to save to
	 */
	public void save(File samFile) {
		if (steps == null) return;
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(samFile));
			Program code = proc.getProgram();
			if (code != null) {
				SymbolTable ST = code.getSymbolTable();
				for (int i = 0; i < code.getLength(); i++) {
					Collection<String> labels = ST.resolveSymbols(i);
					if (labels != null) 
						for (String label: labels)
							out.write(label + ";" + BR);
					out.write(code.getInst(i) + BR);
				}
			}
			out.flush();
			out.close();
			sourceFile = samFile;
			filename = sourceFile.getName();
			setTitle("SaM Simulator - " + samFile.getName());
		}
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Could not find file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	// Loads a new instruction into the cache
	private void loadInstruction(File f){
		ClassFileLoader cl = new ClassFileLoader(this.getClass().getClassLoader());
		
		String className = f.getName();
		if(className.indexOf('.') < 0){
			JOptionPane.showMessageDialog(this, "Could not load instruction - improper filename.", 
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(!className.startsWith("SAM_")){
			JOptionPane.showMessageDialog(this, "Class name is missing the SAM_ prefix.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		className = className.substring(0, className.indexOf('.'));
		String instructionName = className.substring(4);

		try {
			Class<? extends Instruction> c = 
				(Class<? extends Instruction>)(cl.getClass(f, className));
			Instruction i = c.newInstance();
			SamAssembler.instructions.addInstruction(instructionName, c);
			statusBar.setText("Loaded instruction " + instructionName);
		}
		catch (ClassCastException err) {
			JOptionPane.showMessageDialog(this, "Class does not implement the Instruction interface.",
				"Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (NoClassDefFoundError err){
			JOptionPane.showMessageDialog(this, "Could not load instruction. " + BR + 
				"Check that it is marked public and does not belong to any package.", 
				"Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(ClassNotFoundException err){
			JOptionPane.showMessageDialog(this, "Could not load instruction. " + BR +
				"Check that it is marked public and does not belong to any package.",
				"Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(InstantiationException err){
			JOptionPane.showMessageDialog(this, "Could not load instruction. " + BR +
				"Check that it is marked public and does not belong to any package.",
				"Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(IllegalAccessException err){
			JOptionPane.showMessageDialog(this, "Could not load instruction. " + BR +
				"Check that it is marked public and does not belong to any package.",
				"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Runs the program by creating a sam simulator
	private synchronized void run() {
		runThread = new RunThread(this, sys, runDelay);
		runThread.setBreakpointList(breakpoints);
		if (breakpointStop) {
			step();
			breakpointStop = false;
		}
		setStatus(RUNNING);
		statusBar.setPermanentText("Running...");
		runThread.start();
	}

	// Runs the program and captures states;
	private synchronized void capture() {
		capture = true;
		runThread = new RunThread(this, sys, runDelay);
		runThread.setBreakpointList(breakpoints);
		if (breakpointStop) {
			step();
			breakpointStop = false;
		}
		setStatus(CAPTURING);
		statusBar.setPermanentText("Capturing...");
		runThread.start();
	}

	public void threadEvent(final int code, final Object o) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					threadEventReal(code, o);
				}
			});
		}
		catch (InterruptedException e) { }
		catch (InvocationTargetException e) { }
	}

	private synchronized void threadEventReal(int code, Object o) {
		switch (code) {

			case RunThread.THREAD_STEP:
				int lastpc = (Integer) o;
				if (prefs.getBoolean("showStackPanel", true))
					stackPanel.update(mem);

				if (prefs.getBoolean("showHeapPanel", true))
					heapPanel.update(mem);

				registerPanel.update(proc);
				updateProgram();
				if (capture) steps.add(
					new ProgramState(lastpc, mem.getStack(), proc.getRegisters()));
				break;

			case RunThread.THREAD_EXIT_OK:
				try {
					simulatorOutput.setText(
						simulatorOutput.getText() + "Exit Code: " + mem.getMem(0) + BR
					);

					if (proc.get(Processor.SP) != 1) 
						simulatorOutput.setText(simulatorOutput.getText() + 
						"Warning: You do not have one item remaining on the stack" + BR);

					HeapAllocator heap = mem.getHeapAllocator();
					if (heap != null) {
						Iterator<Allocation> iter = heap.getAllocations();
						if (iter.hasNext())
							simulatorOutput.setText(simulatorOutput.getText() + 
							"Warning: Your program leaks memory" + BR);
					}
				}
				catch (SystemException e) {
					simulatorOutput.setText(simulatorOutput.getText() + "No exit code provided" + BR);
				}
				if (capture) {
					statusBar.setText("Capture Completed");
					SamCapture.startUI(steps, proc.getProgram(), filename);
				}
				else
					statusBar.setText("Run Completed");
				setStatus(RUNCOMPLETED);
				setNextExecuting(-1);
				break;

			case RunThread.THREAD_EXCEPTION:
				simulatorOutput.setText(simulatorOutput.getText() + "Processor Error: " + o.toString() + BR);
				statusBar.setText("Processor Error");
				setStatus(RUNCOMPLETED);
				break;

			case RunThread.THREAD_INTERRUPTED:
				setStatus(STOPPED);
				if (capture) {
					statusBar.setText("Capture Interrupted");
					SamCapture.startUI(steps, proc.getProgram(), filename);
				}
				else
					statusBar.setText("Execution Stopped");
				break;

			case RunThread.THREAD_BREAKPOINT:
				setStatus(STOPPED);
				statusBar.setText("Breakpoint Reached");
				if (capture) {
					statusBar.setText("Capture Completed");
					SamCapture.startUI(steps, proc.getProgram(), filename);
				}
				breakpointStop = true;
				break;

			default:
				statusBar.clearText();
				setStatus(RUNCOMPLETED);
				break;
		}
		programCodeView.revalidate();
		programCodeView.repaint();
		validate();
	}

	// This marks the last instruction that was executed and highlights the next
	// instruction
	private void setNextExecuting(int pc) {
		if (lastExecuted > -1) ((ProgramCodeCellRenderer.ProgramCodeCell) 
				((DefaultListModel) (programCode.getModel())).get(lastExecuted)).setExecuting(false);
		lastExecuted = pc;
		if (pc > -1) ((ProgramCodeCellRenderer.ProgramCodeCell) 
				((DefaultListModel) (programCode.getModel())).get(pc)).setExecuting(true);
	}

	// Stop the running program
	private synchronized void stop() {
		if (runThread != null) runThread.interrupt();
	}

	// Step
	private void step() {
		setStatus(STOPPED);
		int lastpc = proc.get(Processor.PC);
		try {
			proc.step();
			if (proc.get(Processor.HALT) != 0) {
				simulatorOutput.setText(
					simulatorOutput.getText() + "Exit Code: " + mem.getMem(0) + BR
				);		
				if (proc.get(Processor.SP) != 1) 
					simulatorOutput.setText(simulatorOutput.getText() + 
					"Warning: You do not have one item remaining on the stack" + BR);

				HeapAllocator heap = mem.getHeapAllocator();
				if (heap != null) {
					Iterator<Allocation> iter = heap.getAllocations();
					if (iter.hasNext())
						simulatorOutput.setText(simulatorOutput.getText() + 
						"Warning: Your program leaks memory" + BR);
				}
		
				setStatus(RUNCOMPLETED);
				statusBar.setText("Execution completed");
				threadEventReal(RunThread.THREAD_STEP, lastpc);
				setNextExecuting(-1);
				programCodeView.revalidate();
				programCodeView.repaint();
				validate();
				return;
			}
		}
		catch (SystemException e) {
			simulatorOutput.setText(simulatorOutput.getText() + e + BR);
			setStatus(RUNCOMPLETED);
			setNextExecuting(-1);
			programCodeView.revalidate();
			programCodeView.repaint();
			validate();
			return;
		}
		threadEventReal(RunThread.THREAD_STEP, lastpc);
	}

	// sets GUI status
	private void setStatus(int status) {
		switch (status) {
			case DEFAULT:
				enableButtons(false, false, false, false, true, false, true, false);
				break;
			case RUNCOMPLETED:
				enableButtons(false, true, false, false, true, true, true, true);
				break;
			case READYTORUN:
				enableButtons(true, true, true, false, true, false, true, true);
				break;
			case RUNNING:
				enableButtons(false, false, false, true, false, false, false, false);
				break;
			case CAPTURING:
				enableButtons(false, false, false, true, false, false, false, false);
				break;
			case STOPPED:
				enableButtons(true, true, true, false, true, true, true, true);
				break;
		}

		curStatus = status;
	}

	// Sets the GUI components to values depending on the arguments provided
	private void enableButtons(boolean runStep, boolean breakpoint, boolean capture, 
				   boolean stop, boolean open, boolean reset, 
				   boolean runOptions, boolean save) {
		resetMenuItem.setEnabled(reset);
		resetButton.setEnabled(reset);
		stopMenuItem.setEnabled(stop);
		stopButton.setEnabled(stop);
		captureMenuItem.setEnabled(capture);
		captureButton.setEnabled(capture);
		runMenuItem.setEnabled(runStep);
		runButton.setEnabled(runStep);
		speedMenu.setEnabled(runOptions);
		stepMenuItem.setEnabled(runStep);
		stepButton.setEnabled(runStep);
		openMenuItem.setEnabled(open);
		openButton.setEnabled(open);
		saveAsMenuItem.setEnabled(save);
		breakpointEditingEnabled = breakpoint;

		// also make sure their background looks enabled/disabled
		if (runStep) {
			runButton.setBackground(new Color(204, 255, 204));
			stepButton.setBackground(new Color(255, 255, 204));
		}
		else {
			runButton.setBackground(new Color(204, 220, 204));
			stepButton.setBackground(new Color(220, 220, 204));
		}
		if (capture)
			captureButton.setBackground(new Color(220, 204, 255));
		else
			captureButton.setBackground(new Color(212, 204, 220));
		if (stop)
			stopButton.setBackground(new Color(255, 204, 204));
		else
			stopButton.setBackground(new Color(220, 204, 204));
		if (open)
			openButton.setBackground(new Color(204, 204, 255));
		else
			openButton.setBackground(new Color(204, 204, 220));
		if (reset)
			resetButton.setBackground(new Color(255, 255, 255));
		else
			resetButton.setBackground(new Color(220, 220, 220));
	}

	// Help Menu
	private void displayColorReference() {
		if (colorsDialog == null) {
			colorsDialog = new SamColorReferenceDialog(this);
		}
		colorsDialog.setVisible(true);
	}

	private void displayAbout() {
	}

	// Video Stuff
	public void writeString(String str) {
		simulatorOutput.setText(simulatorOutput.getText() + "Processor Output: " + str + BR);
	}

	public void writeInt(int i) {
		simulatorOutput.setText(simulatorOutput.getText() + "Processor Output: " + i + BR);
	}

	public void writeChar(char c) {
		simulatorOutput.setText(simulatorOutput.getText() + "Processor Output: " + c + BR);
	}

	public void writeFloat(float f) {
		simulatorOutput.setText(simulatorOutput.getText() + "Processor Output: " + f + BR);
	}

	public String readString() {
		String str = JOptionPane.showInputDialog(this, 
				"Enter a String:", "Prompt", JOptionPane.QUESTION_MESSAGE);
		return (str == null) ? "" : str;
	}

	public int readInt() {
		int i;
		while (true)
			try {
				String ans = JOptionPane.showInputDialog(this, "Enter an Integer:", 
						"Prompt", JOptionPane.QUESTION_MESSAGE);
				i = (ans == null) ? 0 : Integer.parseInt(ans);
				return i;
			}
			catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter an integer", 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
	}

	public float readFloat() {
		float f;
		while (true)
			try {
				String ans = JOptionPane.showInputDialog(this, "Enter a Float:", 
						"Prompt", JOptionPane.QUESTION_MESSAGE);
				f = (ans == null) ? 0 : Float.parseFloat(ans);
				return f;
			}
			catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter a float", 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
	}

	public char readChar() {
		while (true) {
			String ans = JOptionPane.showInputDialog(this, "Enter a Character:", 
					"Prompt", JOptionPane.QUESTION_MESSAGE);
			if (ans == null)
				return (char) 0;
			else if (ans.length() == 1) return ans.charAt(0);
			JOptionPane.showMessageDialog(this, 
				"Please enter an Character", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the aboutDialog for this program
	 */
	public SamAboutDialog getAboutDialog() {
		if (aboutDialog == null) aboutDialog = new SamAboutDialog("SaM", Sys.SAM_VERSION, "SaM Simulator", this);
		return aboutDialog;
	}

	/**
	 * Starts SamGUI without a file loaded
	 */
	public static void startUI() {
		startUI(null);
	}

	/**
	 * Starts SamGUI and loads the provided file
	 * 
	 * @param filename
	 *            loads this file if it is not null
	 */
	public static void startUI(String filename) {
		SamGUI gui = new SamGUI(new Sys());
		gui.start();
		if (filename != null) gui.loadFile(new File(filename));
	}

	/**
	 * Starts SamGUI with the provided program/filename/system
	 * 
	 * @param prog
	 *            The program to load
	 * @param filename
	 *            The filename to associates with the program
	 * @param sys
	 *            The system to use
	 */
	public static void startUI(Program prog, String filename, Sys sys) {
		SamGUI gui;
		if (sys != null)
			gui = new SamGUI(sys);
		else
			gui = new SamGUI(new Sys());
		gui.start();
		if (prog != null && filename != null) gui.loadProgram(prog, filename);
	}

	/**
	 * Allows a GUI to run a program with breakpoints and with the ability
	 * to stop execution at any time
	 */
	public class RunThread extends SamThread {
		/**
		 * Provides access to the processor
		 */
		protected Processor proc;

		/**
		 * Provides access to the system
		 */
		protected Sys sys;

		/**
		 * The delay after each intruction
		 */
		protected int delay = 50;

		/**
		 * A list of all current breakpoints. When RunThread encounters a breakpoint
		 * it interrupts execution.
		 */
		protected BreakpointList breakpoints = null;

		public static final int THREAD_BREAKPOINT = 3;

		public static final int THREAD_STEP = 4;

		/**
		 * Creates a new thread
		 * @param parent the frontend that needs to be updated
		 * @param sys the system to use
		 * @param delay # of milliseconds between command execution
		 */
		public RunThread(SamThread.ThreadParent parent, Sys sys, int delay) {
			setParent(parent);
			this.sys = sys;
			this.delay = delay;
			proc = sys.cpu();
		}

		public void setBreakpointList(BreakpointList l) {
			breakpoints = l;
		}

		public BreakpointList getBreakpointList() {
			return breakpoints;
		}

		/**
		 * Starts the thread
		 */
		public void execute() throws Exception {
			SamThread.ThreadParent parent = getParent();

			while (proc.get(Processor.HALT) == 0) {
				if (interruptRequested()) {
					parent.threadEvent(THREAD_INTERRUPTED, null);
					return;
				}

				int executing = proc.get(Processor.PC);
				if (breakpoints != null && breakpoints.checkBreakpoint(executing)) {
					parent.threadEvent(THREAD_BREAKPOINT, null);
					return;
				}

				proc.step();
				parent.threadEvent(THREAD_STEP, executing);

				try {
					if (delay > 0)
						Thread.sleep(delay);
				} catch (InterruptedException e) {
					continue;
				}
			}
			parent.threadEvent(THREAD_EXIT_OK, null);
		}
	}

	/**
	 * Stores breakpoints and provides convenience functions
	 * We currently use a hashtable for fast access with key=PC of break
	 * and value=Boolean(true)
	 */
	public static class BreakpointList {
		/**
		 * Stores the breakpoints
		 */
		protected HashMap <Integer, Boolean> breakpoints = 
			new HashMap <Integer, Boolean>();

		/**
		 * Adds a PC to the list of breakpoints
		 * @param pc The PC at which to break
		 */
		public void addBreakpoint(int pc) {
			if (!checkBreakpoint(pc))
				breakpoints.put(pc, true);
		}

		/**
		 * Adds a list of breakpoints
		 * @param l the list of breakpoints to add
		 */
		public void addBreakpoints(BreakpointList l) {
			breakpoints.putAll(l.breakpoints);
		}

		/**
		 * Checks if the PC provided is a breakpoint
		 * @param pc The PC to check
		 * @return true if pc is a breakpoint, false otherwise
		 */
		public boolean checkBreakpoint(int pc) {
			return breakpoints.containsKey(pc);
		}

		/**
		 * Deletes the pc from the list of breakpoints
		 * @param pc The PC to delete
		 */
		public void deleteBreakpoint(int pc) {
			breakpoints.remove(pc);
		}

		/**
		 * Deletes a list of breakpoints
		 * @param l The list of brakpoints to delete
		 */
		public void deleteBreakpoints(BreakpointList l) {
			for (Integer e : l.breakpoints.keySet()) 
				breakpoints.remove(e);
		}

		/**
		 * Clears all of the breakpoints
		 */
		public void deleteAll() {
			breakpoints.clear();
		}
	}

}

/**
 * This is a class to handle the rendering for the JList used to display the
 * program code. This renderer differs from the standard one because it marks
 * the breakpoints and also highlights the next instruction to execute
 */

class ProgramCodeCellRenderer extends CellRenderer implements ListCellRenderer {
	private SamGUI.BreakpointList breakpoints;

	public ProgramCodeCellRenderer(SamGUI.BreakpointList breakpoints) {
		super();
		this.breakpoints = breakpoints;
	}

	public Component getListCellRendererComponent(JList list, Object value, 
		int index, boolean isSelected, boolean cellHasFocus) {
		BreakpointIcon bp = new BreakpointIcon(Color.WHITE);

		if (breakpoints.checkBreakpoint(index))
			bp.setColor(Color.RED);
		else if (isSelected)
			bp.setColor(list.getSelectionBackground());
		else if (value instanceof ProgramCodeCell && ((ProgramCodeCell) value).isExecuting()) 
			bp.setColor(new Color(204, 255, 204));

		if (isSelected)
			setBackground(list.getSelectionBackground());
		else if (value instanceof ProgramCodeCell && ((ProgramCodeCell) value).isExecuting()) {
			setBackground(new Color(204, 255, 204));
		}
		else
			setBackground(list.getBackground());

		setIcon(bp);
		setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
		setText(value.toString());

		return this;
	}

	public void setBreakpoints(SamGUI.BreakpointList list) {
		breakpoints = list;
	}

	/**
	 * The icon used for breakpoints - currently an 8x8 circle
	 */
	class BreakpointIcon implements Icon {
		private int height = 8;

		private int width = 8;

		private Color c;

		public BreakpointIcon(Color c) {
			this.c = c;
		}

		public void setColor(Color c) {
			this.c = c;
		}

		public int getIconHeight() {
			return height;
		}

		public int getIconWidth() {
			return width;
		}

		public void paintIcon(Component cm, Graphics g, int x, int y) {
			g.translate(x, y);
			g.setColor(c);
			g.fillOval(0, 0, width, height);
			g.translate(-x, -y);
		}
	}

	public static class ProgramCodeCell {
		private int id;

		private String instruction;

		private String label;

		private boolean executing = false;

		public ProgramCodeCell(int id, String instruction, String label) {
			this.id = id;
			this.instruction = instruction;
			this.label = label;
		}

		public String toString() {
			return id + ": " + instruction + (label == null ? "" : "  (<= " + label + " )");
		}

		public boolean isExecuting() {
			return executing;
		}

		public void setExecuting(boolean b) {
			executing = b;
		}
	}
}
