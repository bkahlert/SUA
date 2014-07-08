package de.fu_berlin.imp.apiua.core.model;

import java.io.StringReader;

import de.fu_berlin.imp.apiua.core.model.data.IData;

public class DataReader extends StringReader {

	public DataReader(IData data) {
		super(data.read());
	}

}
