package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import org.apache.commons.io.FileUtils;

/**
 * A {@link IBaseDataContainer} that uses the system temporary directory.
 * 
 * @author bkahlert
 * 
 */
public class TempBaseDataContainer extends FileBaseDataContainer implements
		IBaseDataContainer {

	public TempBaseDataContainer() {
		super(FileUtils.getTempDirectory());
	}

}
