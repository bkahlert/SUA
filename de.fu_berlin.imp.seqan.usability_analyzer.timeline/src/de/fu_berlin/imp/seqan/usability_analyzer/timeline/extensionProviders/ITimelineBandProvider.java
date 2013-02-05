package de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

/**
 * Instances of this class provide the data needed to display custom data in the
 * timeline.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineBandProvider {
	/**
	 * Returns true if this {@link ITimelineBandProvider} considers the key
	 * valid.
	 * <p>
	 * Typically this means that it has access to a resource identified by this
	 * key.
	 * 
	 * @param key
	 * @return
	 */
	public boolean isValid(Object key);

	/**
	 * Returns the {@link ITimelineBand}s this {@link ITimelineBandProvider}
	 * wants to add to the timeline if the specified key is worked on.
	 * 
	 * @param key
	 *            The key the timeline wants to limit its display to.<br/>
	 *            e.g. if the key is an {@link ID} , this
	 *            {@link ITimelineBandProvider} should only return
	 *            {@link ITimelineBand} with corresponding content.
	 * @param monitor
	 * @return never returns null
	 */
	public List<ITimelineBand> getTimelineBands(Object key,
			IProgressMonitor monitor);
}
