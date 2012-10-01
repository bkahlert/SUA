package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;

public class DiffFileRecordCompareInput extends CompareEditorInput {
	private DiffRecord diffRecord;
	private Image titleImage;

	public DiffFileRecordCompareInput(DiffRecord diffRecord) {
		super(new CompareConfiguration());
		this.diffRecord = diffRecord;
	}

	public DiffRecord getDiffFileRecord() {
		return this.diffRecord;
	}

	@Override
	public String getName() {
		return diffRecord != null ? diffRecord.getFilename() : "EMPTY";
	}

	@Override
	public String getTitle() {
		return diffRecord != null ? diffRecord.getDiffFile().getID() + ": "
				+ diffRecord.getFilename() : "EMPTY";
	}

	@Override
	public String getToolTipText() {
		if (diffRecord == null)
			return "";
		long predRevision = diffRecord.getPredecessor() != null ? diffRecord
				.getPredecessor().getDiffFile().getRevision() : -1;
		long revision = diffRecord.getDiffFile().getRevision();
		return "ID: " + diffRecord.getDiffFile().getID() + "\nFile:"
				+ diffRecord.getFilename() + "\nRevisions: " + predRevision
				+ " and " + revision;
	}

	@Override
	public Image getTitleImage() {
		if (titleImage == null) {
			titleImage = PlatformUI.getWorkbench().getEditorRegistry()
					.getImageDescriptor(diffRecord.getFilename()).createImage();
			CompareUI.disposeOnShutdown(titleImage);
		}
		return titleImage;
	}

	protected Object prepareInput(IProgressMonitor pm) {
		DiffFileRecordCompareItem left = new DiffFileRecordCompareItem(
				diffRecord.getPredecessor(), getTitleImage());
		DiffFileRecordCompareItem right = new DiffFileRecordCompareItem(
				diffRecord, getTitleImage());

		return new DiffNode(null, Differencer.ADDITION | Differencer.CHANGE
				| Differencer.DELETION, null, left, right);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((diffRecord == null) ? 0 : diffRecord.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiffFileRecordCompareInput other = (DiffFileRecordCompareInput) obj;
		if (diffRecord == null) {
			if (other.diffRecord != null)
				return false;
		} else if (!diffRecord.equals(other.diffRecord))
			return false;
		return true;
	}
}