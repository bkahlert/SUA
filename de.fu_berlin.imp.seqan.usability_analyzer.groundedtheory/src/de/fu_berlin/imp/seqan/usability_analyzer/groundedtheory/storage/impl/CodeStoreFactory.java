package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;

public class CodeStoreFactory {

	private static final Logger LOGGER = Logger
			.getLogger(CodeStoreFactory.class);

	private static SUAGTPreferenceUtil preferenceUtil = new SUAGTPreferenceUtil();

	private static ICodeStore CODE_STORE;

	private static Shell shell = null; // TODO use injection; tried but not in
										// container, yet

	public File getCodeStoreFile() {
		File codeServiceFile = preferenceUtil.getCodeStoreFile();
		String errorMessage = null;
		if (codeServiceFile == null) {
			errorMessage = "The location of the grounded theory file is not defined.";
		} else if (!codeServiceFile.canRead()) {
			errorMessage = "The grounded theory file \""
					+ codeServiceFile.getPath() + "\" can not be read.";
		}
		if (errorMessage != null) {
			MessageDialog messageDialog = new MessageDialog(shell,
					"Grounded Theory File", null, errorMessage
							+ "\nWould you like to create one at "
							+ preferenceUtil.getDefaultCodeStoreFile() + "?",
					MessageDialog.INFORMATION, new String[] {
							"No, choose manually", "Yes" }, 1);
			if (messageDialog.open() == 1) {
				codeServiceFile = preferenceUtil.getDefaultCodeStoreFile();
				try {
					codeServiceFile.createNewFile();
				} catch (IOException e) {
					MessageDialog.openError(
							shell,
							"Grounded Theory File",
							"The creation of "
									+ codeServiceFile.getAbsolutePath()
									+ " failed!");
					LOGGER.error("Grounded Theory File creation failed", e);
				}
			} else {
				String filename = Utils.chooseGTFileLocation();
				if (filename != null) {
					codeServiceFile = new File(filename);
					if (codeServiceFile.isDirectory()) {
						codeServiceFile = new File(filename + File.separator
								+ "CodeStore.xml");
					}
				}
			}
			preferenceUtil.setCodeStoreFile(codeServiceFile);
		}
		return codeServiceFile;
	}

	public CodeStoreFactory() {
	}

	public ICodeStore getCodeStore() {
		while (CODE_STORE == null) {
			File codeServiceFile = this.getCodeStoreFile();
			try {
				CODE_STORE = CodeStore.load(codeServiceFile);
			} catch (CodeStoreReadException e) {
				MessageDialog messageDialog = new MessageDialog(shell,
						"Grounded Theory File", null,
						"The grounded theory file \n" + codeServiceFile
								+ "\nis invalid."
								+ "\n\nClick OK to create a new one!",
						MessageDialog.ERROR, new String[] { "OK" }, 1);
				messageDialog.open();
				String filename = Utils.chooseGTFileLocation();
				if (filename != null) {
					codeServiceFile = new File(filename);
					if (codeServiceFile.isDirectory()) {
						codeServiceFile = new File(filename + File.separator
								+ "CodeStore.xml");
					}
					try {
						codeServiceFile.createNewFile();
					} catch (IOException e1) {
						MessageDialog
								.openError(
										shell,
										"Grounded Theory File",
										"The file\n"
												+ filename
												+ "\ncould not be created.\n\n"
												+ "Please check if you have write permissions.");
						LOGGER.error(
								"Could not create "
										+ ICodeStore.class.getSimpleName()
										+ " file at " + filename, e);
					}
				}
				preferenceUtil.setCodeStoreFile(codeServiceFile);
			}
		}
		return CODE_STORE;
	}
}
