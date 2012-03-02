package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;

public class CodeStoreFactory {

	private static final Logger LOGGER = Logger
			.getLogger(CodeStoreFactory.class);

	private static ICodeStore CODE_STORE;

	private static File getCodeStoreFile() {
		SUAGTPreferenceUtil preferenceUtil = new SUAGTPreferenceUtil();
		Shell parentShell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();

		File codeServiceFile = preferenceUtil.getCodeStoreFile();
		if (codeServiceFile == null || !codeServiceFile.canRead()) {
			MessageDialog messageDialog = new MessageDialog(parentShell,
					"Grounded Theory File", null,
					"No Grounded Theory file could be found"
							+ ((codeServiceFile == null) ? "" : " at "
									+ codeServiceFile.getPath())
							+ ".\nWould you like to create one at "
							+ preferenceUtil.getDefaultCodeStoreFile() + "?",
					MessageDialog.INFORMATION, new String[] {
							"No, choose manually", "Yes" }, 1);
			if (messageDialog.open() == 1) {
				codeServiceFile = preferenceUtil.getDefaultCodeStoreFile();
				try {
					codeServiceFile.createNewFile();
				} catch (IOException e) {
					MessageDialog.openError(parentShell,
							"Grounded Theory File", "The creation of "
									+ codeServiceFile.getAbsolutePath()
									+ " failed!");
					LOGGER.error("Grounded Theory File creation failed", e);
				}
			} else {
				DirectoryDialog directoryDialog = new DirectoryDialog(
						parentShell);
				directoryDialog.setText("Grounded Theory Directory");
				if (codeServiceFile != null)
					directoryDialog.setFilterPath(preferenceUtil
							.getDefaultCodeStoreFile().getAbsolutePath());
				directoryDialog
						.setMessage("Please choose where you want to store your Grounded Theory progress.");
				String filename = directoryDialog.open();
				if (filename != null) {
					codeServiceFile = new File(filename);
					if (codeServiceFile.isDirectory())
						codeServiceFile = new File(filename + File.separator
								+ "CodeStore.xml");
				}
			}
			preferenceUtil.setCodeStoreFile(codeServiceFile);
		}
		return codeServiceFile;
	}

	public CodeStoreFactory() {
	}

	public ICodeStore getCodeStore() {
		if (CODE_STORE == null)
			CODE_STORE = new CodeStore(getCodeStoreFile());
		return CODE_STORE;
	}
}
