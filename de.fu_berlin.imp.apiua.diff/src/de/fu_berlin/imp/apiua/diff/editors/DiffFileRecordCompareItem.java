package de.fu_berlin.imp.apiua.diff.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;

/**
 * One of the items a {@link CompareEditor} is made of. Typically the editor
 * consists of the left and right side thus two elements.
 * <p>
 * Designed for use in conjunction with {@link DiffFileRecordCompareEditorInput}.
 * 
 * @author bkahlert
 * 
 */
@SuppressWarnings("restriction")
public class DiffFileRecordCompareItem implements ITypedElement,
		IModificationDate, IStreamContentAccessor {
	private final IDiffRecord diffRecord;
	private final Image image;

	public DiffFileRecordCompareItem(IDiffRecord diffRecord, Image image) {
		this.diffRecord = diffRecord;
		this.image = image;
	}

	@Override
	public String getName() {
		if (this.diffRecord == null) {
			return "";
		}
		return this.diffRecord.getFilename();
	}

	@Override
	public Image getImage() {
		return this.image;
	}

	@Override
	public String getType() {
		if (this.diffRecord == null) {
			return ITypedElement.UNKNOWN_TYPE;
		}
		return FilenameUtils.getExtension(this.diffRecord.getFilename());
	}

	@Override
	public long getModificationDate() {
		try {
			return this.diffRecord.getDiffFile().getDateRange().getStartDate()
					.getTime();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	@Override
	public InputStream getContents() throws CoreException {
		if (this.diffRecord == null || this.diffRecord.getSource() == null) {
			return null;
		}
		return new ByteArrayInputStream(this.diffRecord.getSource().getBytes());
	}
}