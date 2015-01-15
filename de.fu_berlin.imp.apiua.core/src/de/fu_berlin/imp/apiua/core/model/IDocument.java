package de.fu_berlin.imp.apiua.core.model;

import java.util.Map;

import de.fu_berlin.imp.apiua.core.model.data.IData;

/**
 * TODO Use as the interface of all documents like survey, cognitive dimension,
 * etc.
 *
 * @author bkahlert
 *
 */
public interface IDocument extends IData, ILocatable {

	public Map<URI, String> getFields();

}
