package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;

/**
 * One of the items a {@link CompareEditor} is made of. Typically the editor
 * consists of the left and right side thus two elements.
 * <p>
 * Designed for use in conjunction with {@link DiffFileRecordCompareEditorInput}.
 * 
 * @author bkahlert
 * 
 */
public class DiffFileRecordCompareItem implements ITypedElement,
		IModificationDate, IStreamContentAccessor {
	private IDiffRecord diffRecord;
	private Image image;

	public DiffFileRecordCompareItem(IDiffRecord diffRecord, Image image) {
		this.diffRecord = diffRecord;
		this.image = image;
	}

	public String getName() {
		if (diffRecord == null)
			return "";
		return diffRecord.getFilename();
	}

	public Image getImage() {
		return this.image;
	}

	public String getType() {
		if (diffRecord == null)
			return ITypedElement.UNKNOWN_TYPE;
		return FilenameUtils.getExtension(diffRecord.getFilename());
	}

	public long getModificationDate() {
		try {
			return this.diffRecord.getDiffFile().getDateRange()
					.getStartDate().getTime();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public InputStream getContents() throws CoreException {
		if (this.diffRecord == null
				|| this.diffRecord.getSource() == null)
			return null;
		return new ByteArrayInputStream(this.diffRecord.getSource()
				.getBytes());
	}
}