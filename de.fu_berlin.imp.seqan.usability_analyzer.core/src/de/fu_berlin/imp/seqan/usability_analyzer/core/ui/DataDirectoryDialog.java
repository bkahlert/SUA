package de.fu_berlin.imp.seqan.usability_analyzer.core.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.dialogs.DirectoryListDialog;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;

public class DataDirectoryDialog extends DirectoryListDialog {

	public DataDirectoryDialog(Shell parentShell, List<File> directories) {
		super(parentShell, directories);
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Data Directories");
		this.setText("Add or remove data directories.");
	}

	public List<IBaseDataContainer> getSelectedDataDirectories() {
		List<IBaseDataContainer> dataResourceContainers = new ArrayList<IBaseDataContainer>();
		for (File directory : this.getSelectedDirectories()) {
			dataResourceContainers.add(new FileBaseDataContainer(
					directory));
		}
		return dataResourceContainers;
	}

}
