package de.fu_berlin.imp.apiua.diff.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;

public class DiffFileRecordCompareEditorInput extends CompareEditorInput {

	private ICompareInput compareInput;
	private IDiffRecord diffRecord;
	private Image titleImage;

	public DiffFileRecordCompareEditorInput(IDiffRecord diffRecord2) {
		super(new CompareConfiguration());
		this.diffRecord = diffRecord2;
	}

	public ICompareInput getCompareInput() {
		return this.compareInput;
	}

	public IDiffRecord getDiffFileRecord() {
		return this.diffRecord;
	}

	@Override
	public String getName() {
		return this.diffRecord != null ? this.diffRecord.getFilename()
				: "NO_CODES";
	}

	@Override
	public String getTitle() {
		return this.diffRecord != null ? this.diffRecord.getDiffFile()
				.getIdentifier()
				+ ": "
				+ this.diffRecord.getFilename()
				+ "@"
				+ this.diffRecord.getDiffFile().getRevision() : "NO_CODES";
	}

	@Override
	public String getToolTipText() {
		if (this.diffRecord == null) {
			return "";
		}
		String predRevision = this.diffRecord.getPredecessor() != null ? this.diffRecord
				.getPredecessor().getDiffFile().getRevision()
				: "-";
		String revision = this.diffRecord.getDiffFile().getRevision();
		return "ID: " + this.diffRecord.getDiffFile().getIdentifier()
				+ "\nFile:" + this.diffRecord.getFilename() + "\nRevisions: "
				+ predRevision + " and " + revision;
	}

	@Override
	public Image getTitleImage() {
		if (this.titleImage == null) {
			this.titleImage = PlatformUI.getWorkbench().getEditorRegistry()
					.getImageDescriptor(this.diffRecord.getFilename())
					.createImage();
			CompareUI.disposeOnShutdown(this.titleImage);
		}
		return this.titleImage;
	}

	@Override
	protected Object prepareInput(IProgressMonitor pm) {
		DiffFileRecordCompareItem left = new DiffFileRecordCompareItem(
				this.diffRecord.getPredecessor(), this.getTitleImage());
		DiffFileRecordCompareItem right = new DiffFileRecordCompareItem(
				this.diffRecord, this.getTitleImage());

		this.compareInput = new DiffNode(null, Differencer.ADDITION
				| Differencer.CHANGE | Differencer.DELETION, null, left, right);
		return this.compareInput;
	}

	@Override
	public boolean canRunAsJob() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.diffRecord == null) ? 0 : this.diffRecord.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		DiffFileRecordCompareEditorInput other = (DiffFileRecordCompareEditorInput) obj;
		if (this.diffRecord == null) {
			if (other.diffRecord != null) {
				return false;
			}
		} else if (!this.diffRecord.equals(other.diffRecord)) {
			return false;
		}
		return true;
	}
}