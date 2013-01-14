package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl;

import org.apache.commons.io.FileUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;

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
