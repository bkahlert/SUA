package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data;

import java.io.File;
import java.io.IOException;

public interface IBaseDataContainer extends IDataContainer {

	public IDataSetInfo getInfo();

	/**
	 * Returns a static {@link File} from the {@link IBaseDataContainer} that is
	 * only locally created once.
	 * <p>
	 * The {@link File} is read-only, must not be edited and lives outside the
	 * {@link IBaseDataContainer}'s life cycle.
	 * 
	 * @param scope
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public File getStaticFile(String scope, String name) throws IOException;

	/**
	 * Resets a static {@link File}.
	 * <p>
	 * This method is intended to be called if the underlying resource has
	 * changed (e.g. by {@link #putFile(String, String, File)}).
	 */
	public void resetStaticFile(String scope, String name) throws IOException;

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

	/**
	 * Clears all temporary resources the {@link IBaseDataContainer} needed.
	 * <p>
	 * <strong>Warning:</strong> This method should only be called if you don't
	 * want to work with this {@link IBaseDataContainer} anymore.
	 */
	public void dispose();

}
