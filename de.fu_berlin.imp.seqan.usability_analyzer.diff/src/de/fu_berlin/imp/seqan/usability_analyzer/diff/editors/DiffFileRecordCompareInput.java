package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;

public class DiffFileRecordCompareInput extends CompareEditorInput {
	private IDiffRecord diffRecord;
	private Image titleImage;

	public DiffFileRecordCompareInput(IDiffRecord diffRecord) {
		super(new CompareConfiguration());
		this.diffRecord = diffRecord;
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
				.getIdentifier() + ": " + this.diffRecord.getFilename()
				: "NO_CODES";
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

		return new DiffNode(null, Differencer.ADDITION | Differencer.CHANGE
				| Differencer.DELETION, null, left, right);
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
		DiffFileRecordCompareInput other = (DiffFileRecordCompareInput) obj;
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