package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffFileRecordCompareItem implements ITypedElement,
		IModificationDate, IStreamContentAccessor {
	private DiffFileRecord diffFileRecord;
	private Image image;

	public DiffFileRecordCompareItem(DiffFileRecord diffFileRecord, Image image) {
		this.diffFileRecord = diffFileRecord;
		this.image = image;
	}

	public String getName() {
		if (diffFileRecord == null)
			return "";
		return diffFileRecord.getFilename();
	}

	public Image getImage() {
		return this.image;
	}

	public String getType() {
		if (diffFileRecord == null)
			return ITypedElement.UNKNOWN_TYPE;
		return FilenameUtils.getExtension(diffFileRecord.getFilename());
	}

	public long getModificationDate() {
		try {
			return this.diffFileRecord.getDiffFile().getDateRange()
					.getStartDate().getTime();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public InputStream getContents() throws CoreException {
		if (this.diffFileRecord == null
				|| this.diffFileRecord.getSource() == null)
			return null;
		return new ByteArrayInputStream(this.diffFileRecord.getSource()
				.getBytes());
	}
}