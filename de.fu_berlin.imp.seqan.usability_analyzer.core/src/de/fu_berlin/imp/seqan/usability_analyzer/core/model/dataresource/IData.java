package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import java.io.File;
import java.io.IOException;

public interface IData extends Iterable<String> {

	/**
	 * Returns the {@link IBaseDataContainer} this {@link IData} belongs to.
	 * 
	 * @return
	 */
	public IBaseDataContainer getBaseDataContainer();

	/**
	 * Returns the {@link IDataContainer} of what this {@link IData} is a direct
	 * child.
	 * 
	 * @return
	 */
	public IDataContainer getParentDataContainer();

	/**
	 * Returns the {@link IData}'s name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Returns the complete content of this {@link IData}.
	 * 
	 * @return
	 */
	public String read();

	/**
	 * Returns the content denoted by its first and last character.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public String read(long from, long to);

	/**
	 * Returns the very first line of the {@link IData}'s content.
	 * 
	 * @return
	 */
	public String readFirstLine();

	/**
	 * Returns the last n lines of this {@link IData}'s content.
	 * 
	 * @param numLines
	 * @return
	 */
	public String readLastLines(int numLines);

	/**
	 * Returns the length/size of this {@link IData}.
	 * 
	 * @return
	 */
	public long getLength();

	/**
	 * Returns a {@link File} that is a static copy of the {@link IData}.
	 * 
	 * @return
	 * @throws IOException
	 * @see {@link IBaseDataContainer#getStaticFile(String, String)}
	 */
	public File getStaticFile() throws IOException;

}
