package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

public interface IData extends Iterable<String> {

	public IBaseDataContainer getBaseDataContainer();

	public String getName();

	public String read();

	public String read(long from, long to);

	public String readFirstLine();

	public String readLastLines(int numLines);

	public long getLength();
}
