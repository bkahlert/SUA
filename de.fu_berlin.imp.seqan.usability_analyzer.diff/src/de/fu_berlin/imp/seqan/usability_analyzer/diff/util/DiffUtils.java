package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffUtils {
	private File logDirectory;

	public DiffUtils(File logDirectory) {
		this.logDirectory = logDirectory;
	}

	public File getSourceRoot() {
		String path = this.logDirectory + "/sources";
		return new File(path.replace("//", "/"));
	}

	public File getSourceFile(DiffFileRecord diffFileRecord) {
		DiffFile diffFile = diffFileRecord.getDiffFile();
		ID id = diffFile.getId();
		long revision = Long.parseLong(diffFile.getRevision());
		String filename = diffFileRecord.getFilename();

		String path = this.getSourceRoot().getAbsolutePath() + "/" + id + "/"
				+ revision + "/" + filename;
		return new File(path.replace("//", "/"));
	}
}
