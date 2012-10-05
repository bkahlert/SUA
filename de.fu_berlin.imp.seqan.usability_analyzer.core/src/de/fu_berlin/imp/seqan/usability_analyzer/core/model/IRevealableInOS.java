package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.File;
import java.io.IOException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.handlers.RevealInOSHandler;

/**
 * This interface is used to denote objects that can be revealed in the OS's
 * file system.
 * <p>
 * It is used by the {@link RevealInOSHandler} and its corresponding command.
 * 
 * @author bkahlert
 * 
 */
public interface IRevealableInOS {
	public File getFile() throws IOException;
}
