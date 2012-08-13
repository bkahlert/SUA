package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class Utils {

	public static String chooseGTFileLocation() {
		DirectoryDialog directoryDialog = new DirectoryDialog(new Shell()); // TODO
		directoryDialog.setText("Grounded Theory Directory");
		directoryDialog
				.setMessage("Please choose where you want to store your grounded theory progress.");
		String filename = directoryDialog.open();
		return filename;
	}

}
