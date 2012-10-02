package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import java.io.File;
import java.io.IOException;

public interface IData extends Iterable<String> {

	public IBaseDataContainer getBaseDataContainer();

	public IDataContainer getParentDataContainer();

	public String getName();

	public String read();

	public String read(long from, long to);

	public String readFirstLine();

	public String readLastLines(int numLines);

	public long getLength();

	public File getStaticFile() throws IOException;

}
