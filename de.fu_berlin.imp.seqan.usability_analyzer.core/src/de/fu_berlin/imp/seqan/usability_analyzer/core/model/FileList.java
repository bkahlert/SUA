package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A typed {@link List} that only contains {@link File}
 * 
 * @author bkahlert
 * 
 */
public class FileList extends LinkedList<File> {
	private static final long serialVersionUID = 951309780161782611L;

	/**
	 * Returns a thread-safe {@link FileList}
	 * 
	 * @return
	 */
	public static FileList create() {
		FileList fileList = (FileList) Collections
				.synchronizedList(new FileList());
		return fileList;
	}
}
