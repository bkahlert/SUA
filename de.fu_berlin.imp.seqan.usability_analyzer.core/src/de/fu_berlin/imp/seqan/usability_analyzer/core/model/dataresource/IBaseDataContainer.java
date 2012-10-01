package de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource;

import java.io.File;
import java.io.IOException;

public interface IBaseDataContainer extends IDataContainer {

	public IDataSetInfo getInfo();

	/**
	 * Returns a {@link File} the caller can work with.
	 * <p>
	 * In order to save the changes made to the file use
	 * {@link #putFile(String, String, File)}.
	 * 
	 * @param scope
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public File getFile(String scope, String name) throws IOException;

	/**
	 * Puts a {@link File} into the {@link IBaseDataContainer}.
	 * <p>
	 * In order to get a previously put {@link File} use
	 * {@link #getFile(String, String)}.
	 * 
	 * @param scope
	 * @param name
	 * @param file
	 *            if null the {@link File} will be deleted from the
	 *            {@link IBaseDataContainer}
	 * @return
	 * @throws IOException
	 */
	public void putFile(String scope, String name, File file)
			throws IOException;

	/**
	 * Deleted all {@link File}s in the given scope.
	 * 
	 * @param scope
	 */
	public void deleteScope(String scope);

}
