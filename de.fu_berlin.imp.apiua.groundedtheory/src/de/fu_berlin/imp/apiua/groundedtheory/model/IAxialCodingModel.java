package de.fu_berlin.imp.apiua.groundedtheory.model;

import java.util.List;
import java.util.Map;

import com.bkahlert.nebula.widgets.jointjs.JointJSLink.ICoordinateEndpoint;
import com.bkahlert.nebula.widgets.jointjs.JointJSLink.IEndpoint;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel.IURIEndpoint;

/**
 * Instances of this describe a graph describing an axial coding model.
 *
 * @author bkahlert
 *
 */
public interface IAxialCodingModel extends ILocatable {

	public String getTitle();

	public List<URI> getCodes();

	public List<URI> getRelations();

	/**
	 * Returns the title of the cell identified by the given {@link URI}. The
	 * title is not necessarily the attribute with the name &quot;title&quot;.
	 * Therefore it is recommended to use this function instead of
	 * {@link #getAttribute(URI, String)}.
	 *
	 * @param uri
	 * @return
	 */
	public String getTitle(URI uri);

	public Object getAttribute(URI uri, String key);

	/**
	 * Returns the source {@link IEndpoint} of the {@link IRelation} identified
	 * by the given {@link URI}.
	 *
	 * @param uri
	 * @return is of type {@link ICoordinateEndpoint} or {@link IURIEndpoint}
	 */
	public IEndpoint getSource(URI uri);

	/**
	 * Returns the target {@link IEndpoint} of the {@link IRelation} identified
	 * by the given {@link URI}.
	 *
	 * @param uri
	 * @return is of type {@link ICoordinateEndpoint} or {@link IURIEndpoint}
	 */
	public IEndpoint getTarget(URI uri);

	/**
	 * Create a new {@link IAxialCodingModel} based on this
	 * {@link IAxialCodingModel} but overrides the given fields.
	 *
	 * @param copy
	 * @param map
	 */
	public IAxialCodingModel createCopy(Map<String, Object> map);

	public String serialize();

}
