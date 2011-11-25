package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffFileRecordCompareInput extends CompareEditorInput {
	private DiffFileRecord diffFileRecord;
	private Image titleImage;

	public DiffFileRecordCompareInput(DiffFileRecord diffFileRecord) {
		super(new CompareConfiguration());
		this.diffFileRecord = diffFileRecord;
	}

	public DiffFileRecord getDiffFileRecord() {
		return this.diffFileRecord;
	}

	@Override
	public String getName() {
		return diffFileRecord != null ? diffFileRecord.getFilename() : "EMPTY";
	}

	@Override
	public String getTitle() {
		return diffFileRecord != null ? diffFileRecord.getDiffFile().getId()
				+ ": " + diffFileRecord.getFilename() : "EMPTY";
	}

	@Override
	public String getToolTipText() {
		if (diffFileRecord == null)
			return "";
		int predRevision = diffFileRecord.getPredecessor() != null ? Integer
				.parseInt(diffFileRecord.getPredecessor().getDiffFile()
						.getRevision()) : -1;
		int revision = Integer.parseInt(diffFileRecord.getDiffFile()
				.getRevision());
		return "ID: " + diffFileRecord.getDiffFile().getId() + "\nFile:"
				+ diffFileRecord.getFilename() + "\nRevisions: " + predRevision
				+ " and " + revision;
	}

	@Override
	public Image getTitleImage() {
		if (titleImage == null) {
			titleImage = PlatformUI.getWorkbench().getEditorRegistry()
					.getImageDescriptor(diffFileRecord.getFilename())
					.createImage();
			CompareUI.disposeOnShutdown(titleImage);
		}
		return titleImage;
	}

	protected Object prepareInput(IProgressMonitor pm) {
		DiffFileRecordCompareItem left = new DiffFileRecordCompareItem(
				diffFileRecord.getPredecessor(), getTitleImage());
		DiffFileRecordCompareItem right = new DiffFileRecordCompareItem(
				diffFileRecord, getTitleImage());

		return new DiffNode(null, Differencer.ADDITION | Differencer.CHANGE
				| Differencer.DELETION, null, left, right);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((diffFileRecord == null) ? 0 : diffFileRecord.hashCode());
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
		if (diffFileRecord == null) {
			if (other.diffFileRecord != null)
				return false;
		} else if (!diffFileRecord.equals(other.diffFileRecord))
			return false;
		return true;
	}
}