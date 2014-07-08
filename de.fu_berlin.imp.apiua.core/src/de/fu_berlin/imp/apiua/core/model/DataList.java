package de.fu_berlin.imp.apiua.core.model;

import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.imp.apiua.core.model.data.IData;

/**
 * A typed {@link List} that only contains {@link IData}s
 * 
 * @author bkahlert
 * 
 */
public class DataList extends LinkedList<IData> {
	private static final long serialVersionUID = 951309780161782511L;
}
