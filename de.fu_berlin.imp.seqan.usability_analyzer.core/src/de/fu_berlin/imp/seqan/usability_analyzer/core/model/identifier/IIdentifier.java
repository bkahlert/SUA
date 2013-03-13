package de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier;

/**
 * Instances of this class represent identifier that allow to identifier a
 * resource.
 * <p>
 * Use {@link IIdentifier#toString()} to get the string that makes up the
 * {@link IIdentifier}.
 * 
 * @author bkahlert
 * 
 */
public interface IIdentifier extends Comparable<Object> {

	/**
	 * Returns the string that represents this {@link IIdentifier}.
	 * 
	 * @return
	 */
	public String getIdentifier();

}
