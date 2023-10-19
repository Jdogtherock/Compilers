package edu.cornell.cs.sam.ui.components;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/* Class to manage file open and save dialogs */
public class FileDialogManager {
	private JFileChooser[] fileChooserDialog = null;

	/**
	 * Initialize with n file dialogs - all of which save the last location/type
	 * of file opened
	 * 
	 * @param n the number of file dialogs to have
	 */
	public FileDialogManager(int n) {
		fileChooserDialog = new JFileChooser[n];
		for (int i = 0; i < n; i++)
			fileChooserDialog[i] = new JFileChooser(".");
	}

	/**
	 * Runs open dialog and returns the file selected, or null if canceled
	 * 
	 * @param parent the parent Component
	 * @param filetype the file type to be saved
	 * @param extension the extension for this filetype
	 * @param cdir File to set current directory or null otherwise
	 * @param n Code of the open dialog
	 */
	public File getOpenFile(Component parent, String extension,
			String filetype, File cdir, int n) {
		if (cdir != null)
			fileChooserDialog[n].setCurrentDirectory(cdir);
                fileChooserDialog[n].resetChoosableFileFilters();
		fileChooserDialog[n].setFileFilter(new SimpleFilter(extension, filetype
				+ " (*." + extension + ")"));

		if (fileChooserDialog[n].showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
			return null;

		return fileChooserDialog[n].getSelectedFile();
	}

	/**
	 * Runs file selection dialog and returns the File selected or null if
	 * canceled
	 * 
	 * @param parent the parent Component
	 * @param filetype the file type to be saved
	 * @param extension the extension for this filetype
	 * @param cdir file to set current directory or null
	 * @param n Code of the save dialog
	 */
	public File getSaveFile(Component parent, String extension,
			String filetype, File cdir, int n) {
		if (cdir != null)
			fileChooserDialog[n].setCurrentDirectory(cdir);
		fileChooserDialog[n].resetChoosableFileFilters();
		fileChooserDialog[n].setFileFilter(new SimpleFilter(extension, filetype
				+ " (*." + extension + ")"));

		File file;
		while (true) {
			if (fileChooserDialog[n].showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
				return null;
			file = fileChooserDialog[n].getSelectedFile();
			if (!file.getName().endsWith("." + extension))
				file = new File(file.getAbsolutePath() + "." + extension);
			if (file.exists()) {
				int r = JOptionPane.showConfirmDialog(parent,
						"File already exists. Overwrite?", "Warning",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				switch (r) {
					case JOptionPane.YES_OPTION:
						return file;
					case JOptionPane.NO_OPTION:
						continue;
					case JOptionPane.CANCEL_OPTION:
						return null;
				}
			} else
				return file;
		}
	}
	
	/**
	 * Runs open dialog and returns the directory selected, or null if canceled
	 * @param parent the parent Component
	 * @param cdir File to set current directory or null otherwise
	 * @param n which chooser to display
	 */
	public File getOpenDirectory(Component parent, File cdir, int n) {
		if (cdir != null) fileChooserDialog[n].setCurrentDirectory(cdir);
                fileChooserDialog[n].resetChoosableFileFilters();
		fileChooserDialog[n].setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (fileChooserDialog[n].showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
			return null;
		return fileChooserDialog[n].getSelectedFile();
	}


	/**
	 * A File filter that checks for a simple extension
	 */
	public class SimpleFilter extends javax.swing.filechooser.FileFilter {
		private String description = null;

		private String extension = null;

		public SimpleFilter(String extension, String description) {
			this.description = description;
			this.extension = "." + extension.toLowerCase();
		}

		public String getDescription() {
			return description;
		}

		public boolean accept(File f) {
			if (f == null)
				return false;
			if (f.isDirectory())
				return true;
			return f.getName().toLowerCase().endsWith(extension);
		}
	}

}

