package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class DiffFileRecordStorageEditorInput implements IStorageEditorInput {

	private IStorage storage;

	public DiffFileRecordStorageEditorInput(IStorage storage) {
		this.storage = storage;
	}

	public String getName() {
		return storage.getName();
	}

	public String getToolTipText() {
		return "String-based file: " + storage.getName();
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public IStorage getStorage() {
		return storage;
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

}
