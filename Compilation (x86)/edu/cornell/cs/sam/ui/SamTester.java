package edu.cornell.cs.sam.ui;

import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.ui.components.FileDialogManager;
import edu.cornell.cs.sam.ui.components.GridBagUtils;
import edu.cornell.cs.sam.ui.components.SamAboutDialog;
import edu.cornell.cs.sam.ui.components.StatusBar;
import edu.cornell.cs.sam.utils.RegistrationSystem;
import edu.cornell.cs.sam.utils.SamThread;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.reflect.InvocationTargetException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EnumSet;
import java.util.List;

/**
 * Provides a GUI for testing multiple files.
 */
public class SamTester extends JFrame implements SamUI.Component, SamThread.ThreadParent {
	protected final static int STATUS_NOTREADY = 1;

	protected final static int STATUS_EMPTY = 2;

	protected final static int STATUS_LOADED = 3;

	protected final static int STATUS_RUNNING = 4;
	
	protected final static int CHOOSER_OPEN = 0;
	
	protected final static int CHOOSER_SAVE = 1;
	
	protected final static int CHOOSER_SAMPROGRAM = 2;
	
	protected static String scriptFileExtension = "sts";
	
	protected static String scriptFileTypeName = "SaM Test Script";

	protected static String testFileExtension = "sam";
		
	protected static String testFileTypeName = "SaM Program";

	protected Container contentPane;

	protected JPanel mainPanel;

	protected JPanel buttonPanel;

	protected StatusBar statusBar;

	protected JTable tests;

	protected DefaultTableModel testData;

	protected TestTableCellRenderer testRenderer;

	protected TestScript testScript;

	protected JScrollPane testsView;

	protected JMenuItem newFileMenuItem;

	protected JMenuItem openFileMenuItem;

	protected JMenuItem saveFileMenuItem;

	protected JMenuItem saveAsFileMenuItem;

	protected JMenuItem runRunMenuItem;

	protected JMenuItem stopRunMenuItem;

	protected JMenuItem addTestsMenuItem;

	protected JMenuItem deleteTestsMenuItem;

	protected boolean deleteTestsEnabled;

	protected JButton openButton;

	protected JButton runButton;

	protected JButton stopButton;

	protected boolean modified = false;

	protected SamAboutDialog aboutDialog;

	protected TestScript.TestThread testThread;

	protected FileDialogManager fileDialogs;

	public static int classID = RegistrationSystem.getNextUID();
	
	protected SamTester() {
		System.setProperty("sun.awt.noerasebackground", "true");
		RegistrationSystem.register(classID, this);
		fileDialogs = new FileDialogManager(3);

		// Set up basic top level layout
		contentPane = getContentPane();
		setTitle("SaM Tester");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		contentPane.setLayout(new BorderLayout());
		mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		statusBar = new StatusBar();
		contentPane.add(statusBar, BorderLayout.SOUTH);

		// Add components
		createComponents();
		setJMenuBar(createMenus());

		setStatus(STATUS_NOTREADY);

		addNotify();
		setWindowListeners();
		pack();
	}

	protected void start() {
		setVisible(true);

		aboutDialog = getAboutDialog();
	}

	protected void updateTitle() {
		if (testScript == null || testScript.getSourceFile() == null)
			setTitle("SaM Tester");
		else
			setTitle("SaM Tester - " + testScript.getSourceFile().getName());
	}

	// Create components
	protected void createComponents() {
		buttonPanel = createButtonPanel();

		createTable();

		GridBagLayout l = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 5);
		mainPanel.setLayout(l);

		testsView = new JScrollPane(tests);
		testsView.getViewport().setBackground(Color.WHITE);

		GridBagUtils.addComponent(new JLabel("Tests:"), mainPanel, l, c, 0, 0, 2, 1, 1, 1);
		GridBagUtils.addComponent(testsView, mainPanel, l, c, 0, 1, 1, 1, 1, 1);
		GridBagUtils.addComponent(buttonPanel, mainPanel, l, c, 1, 1, 1, 1, 1, 1);
	}

	protected JPanel createButtonPanel() {
		JPanel p = new JPanel();
		GridBagLayout l = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 5);
		p.setLayout(l);
		openButton = GridBagUtils.addButton("Open", p, l, c, 0, 0, 1, 1, 1, 1);
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openScript();
			}
		});
		runButton = GridBagUtils.addButton("Run Tests", p, l, c, 0, 1, 1, 1, 1, 1);
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runTests();
			}
		});
		stopButton = GridBagUtils.addButton("Stop Run", p, l, c, 0, 2, 1, 1, 1, 1);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopTests();
			}
		});

		return p;
	}

	protected void createTable() {
		// Setup the table first
		String[] columns = { "Name", "Status", "Expected", "Actual", "I/O" };
		testData = new DefaultTableModel(0, 5) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		testData.setColumnIdentifiers(columns);
		tests = new JTable(testData);
		tests.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel source = (ListSelectionModel) e.getSource();
				deleteTestsMenuItem.setEnabled(deleteTestsEnabled && !source.isSelectionEmpty());
			}
		});
		tests.setShowVerticalLines(false);
		tests.setShowHorizontalLines(false);
		tests.setPreferredScrollableViewportSize(new Dimension(500, 100));
		tests.setBackground(Color.WHITE);
		// We need to set the cell renderers in the columns
		Enumeration<TableColumn> e = tests.getColumnModel().getColumns();
		testRenderer = new TestTableCellRenderer();
		TableColumn first = e.nextElement();
		first.setPreferredWidth(first.getPreferredWidth() * 2);
		while (e.hasMoreElements())
			e.nextElement().setCellRenderer(testRenderer);
		tests.doLayout();

		// setup the mouse listener
		tests.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 2) return;
				openTestDetails(tests.rowAtPoint(e.getPoint()));
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
	}

	protected JMenuBar createMenus() {
		JMenuBar menuBar = new JMenuBar();

		menuBar.add(createFileMenu());
		menuBar.add(createRunMenu());
		menuBar.add(createTestsMenu());
		menuBar.add(SamUI.createSamMenu(this));

		return menuBar;
	}

	protected JMenu createFileMenu(){
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		newFileMenuItem = fileMenu.add("New");
		newFileMenuItem.setMnemonic(KeyEvent.VK_N);
		newFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newScript();
			}
		});
		openFileMenuItem = fileMenu.add("Open");
		openFileMenuItem.setMnemonic(KeyEvent.VK_O);
		openFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openScript();
			}
		});
		saveFileMenuItem = fileMenu.add("Save");
		saveFileMenuItem.setMnemonic(KeyEvent.VK_S);
		saveFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveScript(false);
			}
		});
		saveAsFileMenuItem = fileMenu.add("Save As...");
		saveAsFileMenuItem.setMnemonic(KeyEvent.VK_A);
		saveAsFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
		saveAsFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveScript(true);
			}
		});
		JMenuItem closeFileMenuItem = fileMenu.add("Close Window");
		closeFileMenuItem.setMnemonic(KeyEvent.VK_C);
		closeFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		closeFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		JMenuItem exitFileMenuItem = fileMenu.add("Exit");
		exitFileMenuItem.setMnemonic(KeyEvent.VK_X);
		exitFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		exitFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
				SamUI.exit();
			}
		});
		return fileMenu;
	}
	
	protected JMenu createRunMenu(){
		JMenu runMenu = new JMenu("Run");
		runMenu.setMnemonic(KeyEvent.VK_R);
		runRunMenuItem = runMenu.add("Run Tests");
		runRunMenuItem.setMnemonic(KeyEvent.VK_R);
		runRunMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		runRunMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runTests();
			}
		});
		stopRunMenuItem = runMenu.add("Stop Tests");
		stopRunMenuItem.setMnemonic(KeyEvent.VK_S);
		stopRunMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		stopRunMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopTests();
			}
		});
		return runMenu;
	}
	
	protected JMenu createTestsMenu(){
		JMenu testsMenu = new JMenu("Tests");
		testsMenu.setMnemonic(KeyEvent.VK_T);
		addTestsMenuItem = testsMenu.add("Add Test");
		addTestsMenuItem.setMnemonic(KeyEvent.VK_A);
		addTestsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		addTestsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTest();
			}
		});
		deleteTestsMenuItem = testsMenu.add("Delete Test");
		deleteTestsMenuItem.setMnemonic(KeyEvent.VK_D);
		deleteTestsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		deleteTestsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteTest();
			}
		});
		return testsMenu;
	}
	
	protected void newScript() {
		if (!confirmClose()) return;
		clear();
		testScript = getNewTestScript();
		testRenderer.setTestScript(testScript);
		setStatus(STATUS_EMPTY);
	}
	
	protected TestScript getNewTestScript(){
		return new TestScript();
	}

	protected void clear() {
		testScript = null;
		modified = false;
		testData.setRowCount(0);
	}

	protected void openScript() {
		if (!confirmClose()) return;

		File file = fileDialogs.getOpenFile(this, scriptFileExtension, 
				scriptFileTypeName, null, CHOOSER_OPEN);
		if (file != null) {
			clear();
			loadFile(file);
		}
	}

	protected void loadFile(File in) {
		try {
			testScript = getNewTestScript();
			testScript.load(new BufferedInputStream(new FileInputStream(in)));
			testScript.setSourceFile(in);
			testRenderer.setTestScript(testScript);
			updateTable();
			if (testScript.getTests().size() > 0) setStatus(STATUS_LOADED);
			updateTitle();
		}
		catch (FileNotFoundException e) {
			error("Requested File (" + in.getName() + ") Not Found");
		}
		catch (TestScript.TestScriptException e) {
			error("Error parsing test script:\n" + e.getMessage());
		}
	}

	protected boolean saveScript(boolean saveAs) {
		File toSave = testScript.getSourceFile();
		if (saveAs || toSave == null) {
			File cdir = (toSave == null) ? null : toSave.getParentFile();
			toSave = fileDialogs.getSaveFile(this, scriptFileExtension, 
				scriptFileTypeName, cdir, CHOOSER_SAVE);

			if (toSave == null) return false;
		}
		try {
			testScript.save(toSave);
			testScript.setSourceFile(toSave);
			updateTitle();
			modified = false;
			return true;
		}
		catch (TestScript.TestScriptException e) {
			error("Error saving file:\n" + e.getMessage());
			return false;
		}
	}

	protected void updateTable() {
		testData.setRowCount(0);
		if (testScript == null) return;
		for (TestScript.Test t: testScript.getTests())
			testData.addRow(createRow(t));
		pack();
	}

	protected String[] createRow(TestScript.Test t) {
		String[] out = { 
			t.getFileName(), 
			t.isCompleted() ? (t.error() ? "Error" : "Successful") : "Not Run", 
			t.getReturnValue().toString(), "", "" 
		};
		if (t.isCompleted()) {
			out[3] = t.getActualReturnValue().toString();
			out[4] = t.isIoSuccessful() ? "Good" : "Error";
		}
		return out;
	}

	protected synchronized void runTests() {
		List<TestScript.Test> testsToRun;
		List<TestScript.Test> availableTests = testScript.getTests();

		if (tests.getSelectedRow() == -1) {
			testsToRun = availableTests;
			testScript.clearTests();
		}
		else {
			testsToRun = new ArrayList<TestScript.Test>();
			int[] requested = tests.getSelectedRows();
			for (int i:requested) {
				TestScript.Test test = availableTests.get(i);
				test.clear();
				testsToRun.add(test);
			}
		}

		updateTable();
		statusBar.setPermanentText("Running Tests...");
		setStatus(STATUS_RUNNING);

		testThread = new TestScript.TestThread(this, new Sys(), testsToRun);
		testThread.start();
	}

	protected synchronized void stopTests() {
		if (testThread != null) testThread.interrupt();
	}

	public void threadEvent(int code, Object o) {
		switch (code) {
			case TestScript.TestThread.THREAD_TEST_COMPLETED:
				updateTestStatus();
				break;
			case TestScript.TestThread.THREAD_INTERRUPTED:
				statusBar.setText("Tests Interrupted");
				setStatus(STATUS_LOADED);
				break;
			case TestScript.TestThread.THREAD_EXIT_OK:
				statusBar.setText("Tests Completed");
				setStatus(STATUS_LOADED);
				break;
			case TestScript.TestThread.THREAD_EXCEPTION:
				statusBar.setText("Tests Failed");
				setStatus(STATUS_LOADED);
				error("Error running tests: " + ((TestScript.TestScriptException) o).getMessage());
				break;
			default:
				break;
		}
	}

	// make sure swing update works
	protected void updateTestStatus() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					updateTable();
				}
			});
		}
		catch (InterruptedException e) { }
		catch (InvocationTargetException e) { }
	}

	protected void openTestDetails(int row) {
		if (row < 0) return;
		final TestScript.Test t = (TestScript.Test) testScript.getTests().get(row);

		final JFrame f = new JFrame();
		JPanel p = new JPanel();

		JTextArea info = new JTextArea();
		info.setEditable(false);
		info.setText(getInformation(t, false));
		info.setBackground(p.getBackground());

		f.setTitle((new File(t.getFileName()).getName()));
		f.getContentPane().add(p);

		GridBagLayout l = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 5);
		p.setLayout(l);
		GridBagUtils.addLabel("Test Information:", p, l, c, 0, 0, 2, 1, 1, 1);
		GridBagUtils.addComponent(info, p, l, c, 0, 1, 2, 1, 1, 1);

		c.fill = GridBagConstraints.NORTH;

		JButton guiButton = GridBagUtils.addButton("Launch in GUI", p, l, c, 0, 2, 1, 1, 1, 1);
		guiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamGUI.startUI(t.getFile().getAbsolutePath());
			}
		});
		JButton closeButton = GridBagUtils.addButton("Close", p, l, c, 1, 2, 1, 1, 1, 1);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		closeButton.setSize(guiButton.getSize());

		f.setSize(300, 200);
		f.pack();
		f.setVisible(true);
	}

	protected String getInformation(TestScript.Test t, boolean html) {
		StringWriter outS = new StringWriter();
		PrintWriter out = new PrintWriter(outS);
		String ending = "";
		if (html) ending = "<br>";
		if (html) out.println("<html>");
		out.println("Name: " + t.getFileName() + ending);
		out.println("Status: " + 
			(!t.isCompleted() ? "Not Run" : (t.error() ? "Error" : "Completed Successfully")) + 
		ending);
		out.println("Expected Return Value: " + t.getReturnValue() + ending);
		if (t.getActualReturnValue() != null) 
			out.println("Actual Return Value: " + t.getActualReturnValue() + ending);
		if (t.isCompleted()){
			out.println("I/O  Complete: " + t.isIoSuccessful() + ending);
			out.println("Stack Cleared: " + t.isIoSuccessful() + ending);
		}
		if (html) out.println("</html>");
		return outS.toString();
	}

	protected void addTest() {
		TestScript.Test t = AddTestDialog.getNewTest(testScript, this);
		if (t != null) {
			testScript.getTests().add(t);
			modified = true;
			updateTable();
			if (testScript.getTests().size() > 0) setStatus(STATUS_LOADED);
		}
	}

	protected void deleteTest() {
		if (tests.getSelectedRow() == -1) {
			warning("You must select at least one row");
			return;
		}
		int[] toDelete = tests.getSelectedRows();
		if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected tests?", 
			"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
			for (int i: toDelete) {
				TestScript.Test t = testScript.getTests().get(i);
				t.delete();
			}
			testScript.deleteTests();
		}
		modified = true;
		updateTable();
	}

	protected void error(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	protected void warning(String message) {
		JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	protected void setStatus(int status) {
		switch (status) {
			case STATUS_NOTREADY:
				setButtons(true, false, false, false, false);
				break;
			case STATUS_EMPTY:
				setButtons(true, true, false, false, false);
				break;
			case STATUS_LOADED:
				setButtons(true, true, true, true, false);
				break;
			case STATUS_RUNNING:
				setButtons(false, false, false, false, true);
				break;
		}
	}

	// Set Buttons
	protected void setButtons(boolean fileOps, boolean addTests, boolean delTests, boolean run, boolean stop) {
		newFileMenuItem.setEnabled(fileOps);
		openButton.setEnabled(fileOps);
		openFileMenuItem.setEnabled(fileOps);
		runButton.setEnabled(run);
		runRunMenuItem.setEnabled(run);
		stopButton.setEnabled(stop);
		stopRunMenuItem.setEnabled(stop);
		addTestsMenuItem.setEnabled(addTests);
		deleteTestsEnabled = delTests;
		deleteTestsMenuItem.setEnabled(deleteTestsEnabled && (tests.getSelectedRow() != -1));

		saveFileMenuItem.setEnabled(fileOps && delTests);
		saveAsFileMenuItem.setEnabled(fileOps && delTests);

		if (run)
			runButton.setBackground(new Color(204, 255, 204));
		else
			runButton.setBackground(new Color(204, 220, 204));
		if (stop)
			stopButton.setBackground(new Color(255, 204, 204));
		else
			stopButton.setBackground(new Color(220, 204, 204));
		if (fileOps)
			openButton.setBackground(new Color(204, 204, 255));
		else
			openButton.setBackground(new Color(204, 204, 220));
	}

	// Sets the listeners to listent for window closing and resizing
	protected void setWindowListeners() {
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

	// Returns true if the close can continue normally
	protected boolean confirmClose() {
		if (!modified) return true;
		int ret = JOptionPane.showConfirmDialog(this, "Save file before closing?", "Confirm", 
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (ret == JOptionPane.YES_OPTION)
			return saveScript(false);
		else if (ret == JOptionPane.NO_OPTION) return true;
		return false;
	}

	public boolean close() {
		if (!confirmClose()) return false;
		if (aboutDialog != null) {
			aboutDialog.dispose();
		}
		RegistrationSystem.unregister(classID, this);
		dispose();
		return true;
	}

	public SamAboutDialog getAboutDialog() {
		if (aboutDialog == null) aboutDialog = new SamAboutDialog("SaM", Sys.SAM_VERSION, "SaM Tester", this);
		return aboutDialog;
	}

	public static void startUI() {
		startUI(null);
	}

	public FileDialogManager getFileDialog() {
		return fileDialogs;
	}	

	public static void startUI(String filename) {
		SamTester gui = new SamTester();
		gui.start();
		if (filename != null)
			gui.loadFile(new File(filename));
		else
			gui.newScript();
	}

	protected static class AddTestDialog extends JDialog {
		protected JTextField fileTextField;

		protected File testFile;

		protected JTextField returnValueTextField;

		protected JComboBox returnValueTypeList;

		protected Object returnValue;

		protected JList readList;

		protected JList writeList;

		protected JButton readAdd;

		protected JButton readDelete;

		protected JButton writeAdd;

		protected JButton writeDelete;

		protected List<Object> write;

		protected List<Object> read;

		protected static final int READ = 1;

		protected static final int WRITE = 2;

		protected TestScript testScript;

		protected boolean validTest = false;

		protected enum IOType {
			INTEGER("Integer"),
			FLOAT("Floating Point"),
			CHAR("Character"),
			STRING("String");

			private String name;

			IOType(String name) {
				this.name = name;
			}

			public String toString() {
				return name;
			}
		}

		protected AddTestDialog(TestScript ts, SamTester parent) {
			super(parent, true);

			testScript = ts;

			getContentPane().add(createComponents(parent));

			setTitle("Add Test");

			pack();
		}
		
		protected JPanel createComponents(SamTester parent){
			JPanel p = new JPanel();
			p.setPreferredSize(new Dimension(350, 300));
			p.setMinimumSize(new Dimension(250, 300));

			GridBagLayout l = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(5, 5, 5, 5);
			p.setLayout(l);

			int height = 0;
			
			GridBagUtils.addLabel("Enter Test Information:", p, l, c, 0, height, 3, 1, 0, 0);
			height++;
			addFileInput(p, l, c, parent, height);
			height++;
			addReturnValue(p, l, c, height);
			height++;
			addReadSchedule(p, l, c, height);
			height++;
			addWriteSchedule(p, l, c, height);
			height++;

			JPanel bottomPanel = createBottomPanel();
			GridBagUtils.addComponent(bottomPanel, p, l, c, 0, 5, 3, 1, 0, 0);
			return p;
		}

		protected void addFileInput(JPanel p, GridBagLayout l, GridBagConstraints c, 
						final SamTester parent, int height){
			GridBagUtils.addLabel("File:", p, l, c, 0, height, 1, 1, 0, 0);
			fileTextField = new JTextField(30);
			fileTextField.setEditable(false);
			fileTextField.setEnabled(true);
			GridBagUtils.addComponent(fileTextField, p, l, c, 1, height, 1, 1, 1, 1);
			JButton browseButton = GridBagUtils.addButton("Browse", p, l, c, 2, height, 1, 1, .2, 0);
			browseButton.setPreferredSize(new Dimension(75, 30));
			browseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					File cdir;
					if ((testScript == null) || (testScript.getSourceFile() == null))
						cdir = null;
					else
						cdir = testScript.getSourceFile().getParentFile();

					File f = parent.getFileDialog().getOpenFile(parent, testFileExtension, 
						testFileTypeName, cdir, SamTester.CHOOSER_SAMPROGRAM);

					if (f == null) return;
					if (cdir != null && f.getParent().equals(cdir.getName()))
						fileTextField.setText(f.getName());
					else
						fileTextField.setText(f.getAbsolutePath());
					testFile = f;
				}
			});
		}

		public static TestScript.Test getNewTest(TestScript testScript, SamTester parent) {
			AddTestDialog d = new AddTestDialog(testScript, parent);
			d.setVisible(true);
			if (d.validTest) {
				TestScript.Test t = new TestScript.Test(d.testFile.toString());
				t.setReturnValue(d.returnValue);
				t.setScriptFile(d.testScript);
				t.setRead(d.read == null ? new ArrayList<Object>() : d.read);
				t.setWrite(d.write == null ? new ArrayList<Object>() : d.write);
				return t;
			}
			return null;
		}

		protected void addReturnValue(JPanel p, GridBagLayout l, GridBagConstraints c, int height){
			GridBagUtils.addLabel("Return Value:", p, l, c, 0, height, 1, 1, 0, 0);
			returnValueTextField = new JTextField(10);
			GridBagUtils.addComponent(returnValueTextField, p, l, c, 1, height, 1, 1, 1, 1);
			returnValueTypeList = newTypeList(false);
			GridBagUtils.addComponent(returnValueTypeList, p, l, c, 2, height, 1, 1, .2, 0);
		}

		protected void addReadSchedule(JPanel p, GridBagLayout l, GridBagConstraints c, int height){
			GridBagUtils.addLabel("Scheduled to read:", p, l, c, 0, height, 1, 1, 0, 0);
			readList = newIOList(READ);
			read = new ArrayList<Object>();
			GridBagUtils.addComponent(scrollList(readList), p, l, c, 1, height, 1, 1, 1, 1);
			GridBagUtils.addComponent(newIOButtonPanel(READ), p, l, c, 2, height, 1, 1, .2, 0);
		}

		protected void addWriteSchedule(JPanel p, GridBagLayout l, GridBagConstraints c, int height){
			GridBagUtils.addLabel("Expected to write:", p, l, c, 0, height, 1, 1, 0, 0);
			writeList = newIOList(WRITE);
			write = new ArrayList<Object>();
			GridBagUtils.addComponent(scrollList(writeList), p, l, c, 1, height, 1, 1, 1, 1);
			GridBagUtils.addComponent(newIOButtonPanel(WRITE), p, l, c, 2, height, 1, 1, .2, 0);
		}

		protected static JComboBox newTypeList(boolean string) {
			JComboBox cb;
			if (string)
				return new JComboBox(EnumSet.allOf(IOType.class).toArray());
			else 
				return new JComboBox(EnumSet.range(IOType.INTEGER, IOType.CHAR).toArray());
		}

		protected JList newIOList(int io) {
			JList l = new JList();
			l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			l.setModel(new DefaultListModel());
			if (io == READ) {
				l.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						JList source = (JList) e.getSource();
						if (source.getSelectedIndex() == -1)
							readDelete.setEnabled(false);
						else
							readDelete.setEnabled(true);
					}
				});
			}
			else if (io == WRITE) {
				l.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						JList source = (JList) e.getSource();
						if (source.getSelectedIndex() == -1)
							writeDelete.setEnabled(false);
						else
							writeDelete.setEnabled(true);
					}
				});
			}
			return l;
		}

		protected JScrollPane scrollList(JList l) {
			JScrollPane p = new JScrollPane(l);
			p.setPreferredSize(new Dimension(100, 60));
			p.setMinimumSize(new Dimension(100, 60));
			return p;
		}

		protected JPanel createBottomPanel() {
			JPanel p = new JPanel();
			GridBagLayout l = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(5, 5, 5, 5);
			p.setLayout(l);
			JButton addTestButton = GridBagUtils.addButton("Add Test", p, l, c, 0, 0, 1, 1, 0, 0);
			addTestButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (testFile == null) {
						error("You must select a file for this test.");
						return;
					}
					if (returnValueTextField.getText().length() == 0) {
						error("You must fill in a return value.");
						return;
					}
					returnValue = createObject(returnValueTextField.getText(), 
							(IOType) returnValueTypeList.getSelectedItem());
					if (returnValue == null) {
						error("You must fill in a valid return value for the type you selected.");
						return;
					}
					validTest = true;
					dispose();
				}
			});
			JButton cancelButton = GridBagUtils.addButton("Cancel", p, l, c, 1, 0, 1, 1, 0, 0);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			return p;
		}

		protected static Object createObject(String s, IOType type) {
			Object ret = null;
			try {
				switch (type) {
					case INTEGER:
						ret = new Integer(s);
						break;
					case FLOAT:
						ret = new Float(s);
						break;
					case CHAR:
						if (s.length() != 1) return null;
						ret = new Character(s.charAt(0));
						break;
					case STRING:
						ret = s;
						break;
				}
			}
			catch (NumberFormatException e) {
				ret = null;
			}	
			return ret;
		}

		private void error(String message) {
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
		}

		protected JPanel newIOButtonPanel(int io) {
			JPanel p = new JPanel();
			p.setMaximumSize(new Dimension(75, 60));
			p.setMinimumSize(new Dimension(75, 60));
			GridBagLayout l = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			p.setLayout(l);

			c.insets = new Insets(0, 0, 2, 0);
			JButton addIOButton = GridBagUtils.addButton("Add", p, l, c, 0, 0, 1, 1, 1, 1);
			c.insets = new Insets(3, 0, 0, 0);
			JButton delIOButton = GridBagUtils.addButton("Delete", p, l, c, 0, 1, 1, 1, 1, 1);
			delIOButton.setEnabled(false);

			if (io == READ) {
				readAdd = addIOButton;
				readDelete = delIOButton;
				addIOButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addIO(READ);
					}
				});
				delIOButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						deleteIO(READ);
					}
				});
			}
			else if (io == WRITE) {
				writeAdd = addIOButton;
				writeDelete = delIOButton;
				addIOButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addIO(WRITE);
					}
				});
				delIOButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						deleteIO(WRITE);
					}
				});
			}

			return p;
		}

		protected void addIO(int io) {
			Object o = AddIODialog.getIO(this);
			if (o != null) {
				if (io == READ) {
					read.add(o);
					updateIOList(read, readList);
				}
				else if (io == WRITE) {
					write.add(o);
					updateIOList(write, writeList);
				}
			}
		}

		protected void updateIOList(List<?> ioEntries, JList ioList) {
			DefaultListModel ioData = (DefaultListModel) (ioList.getModel());
			ioData.clear();
			for (Object o: ioEntries) 
				ioData.addElement(o);
		}

		protected void deleteIO(int io) {
			JList l;
			List<Object> data;
			if (io == READ) {
				l = readList;
				data = read;
			}
			else if (io == WRITE) {
				l = writeList;
				data = write;
			}
			else
				return;

			if (l.getSelectedIndex() == -1) return;
			if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected I/O?", 
				"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 
				JOptionPane.YES_OPTION) {
				data.remove(l.getSelectedIndex());
				updateIOList(data, l);
			}
		}

		private static class AddIODialog extends JDialog {
			protected JTextField valueField;

			protected JComboBox typeField;

			protected Object io = null;

			protected AddIODialog(AddTestDialog parent) {
				super(parent, true);
				Container p = getContentPane();

				setTitle("Add I/O");
				GridBagLayout l = new GridBagLayout();
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.insets = new Insets(5, 5, 5, 5);
				p.setLayout(l);

				valueField = new JTextField(10);
				GridBagUtils.addComponent(valueField, p, l, c, 0, 0, 1, 1, 1, 1);
				typeField = newTypeList(true);
				GridBagUtils.addComponent(typeField, p, l, c, 1, 0, 1, 1, 1, 1);

				GridBagUtils.addComponent(createBottomPanel(), p, l, c, 0, 1, 2, 1, 1, 1);

				pack();
			}

			public static Object getIO(AddTestDialog parent) {
				AddIODialog d = new AddIODialog(parent);
				d.setVisible(true);
				return d.io;
			}

			protected void error(String message) {
				JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			}

			protected JPanel createBottomPanel() {
				JPanel p = new JPanel();
				GridBagLayout l = new GridBagLayout();
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.insets = new Insets(5, 5, 5, 5);
				p.setLayout(l);
				JButton addIOButton = GridBagUtils.addButton("Add", p, l, c, 0, 0, 1, 1, 0, 0);
				addIOButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						io = createObject(valueField.getText(), 
								(IOType) typeField.getSelectedItem());
						if (io != null) {
							dispose();
							return;
						}
						error("You must provide a valid value for this type");
					}
				});
				JButton cancelButton = GridBagUtils.addButton("Cancel", p, l, c, 1, 0, 1, 1, 0, 0);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				return p;
			}
		}
	}
}

/**
 * This is a class to handle the rendering for the JTable used to display the
 * test results. This renderer differs from the standard one because it checks
 * the results to see if there was an error in the test.
 */

class TestTableCellRenderer extends JLabel implements TableCellRenderer {
	protected TestScript tests;

	public TestTableCellRenderer() {
		setOpaque(true);
	}

	public void setTestScript(TestScript tests) {
		this.tests = tests;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
				boolean isSelected, boolean hasFocus, int row, int column) {
		boolean error = false;

		if (tests != null) {
			TestScript.Test t = (TestScript.Test) tests.getTests().get(row);

			if (t.isCompleted()) error = t.error();
		}

		if (isSelected)
			setBackground(table.getSelectionBackground());
		else
			setBackground(table.getBackground());

		if (error)
			setForeground(Color.RED);
		else
			setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		setText(value.toString());
		setFont(table.getFont());

		if (column == 0)
			setHorizontalAlignment(JLabel.LEADING);
		else
			setHorizontalAlignment(JLabel.CENTER);

		return this;
	}
}
