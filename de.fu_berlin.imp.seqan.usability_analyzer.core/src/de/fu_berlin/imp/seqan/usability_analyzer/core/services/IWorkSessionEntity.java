package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

/**
 * Instances of this class can be part of an {@link IWorkSession}.
 * 
 * @author bkahlert
 * 
 */
public interface IWorkSessionEntity {
	/**
	 * Returns the ID that identifies the entity in focus.
	 * 
	 * @return
	 */
	public String getWorkSessionEntityID();
}
