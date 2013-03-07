package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;

class CompilationServiceUtils {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CompilationServiceUtils.class);

	static final String SCOPE = "compilation";
	static final String NAME = "states.properties";

	/**
	 * Returns the file that contains the compilation states from the
	 * {@link IBaseDataContainer}.
	 * 
	 * @param baseDataContainer
	 * @return
	 * @throws IOException
	 */
	private static File getCompilationStateFile(
			IBaseDataContainer baseDataContainer) throws IOException {
		return baseDataContainer.getStaticFile(SCOPE, NAME);
	}

	/**
	 * Saves changes made to the compilation states file in the
	 * {@link IBaseDataContainer}.
	 * 
	 * @param baseDataContainer
	 * @param compilatonStateFile
	 * @throws IOException
	 */
	private static void setCompilationStateFile(
			IBaseDataContainer baseDataContainer, File compilatonStateFile)
			throws IOException {
		baseDataContainer.putFile(SCOPE, NAME, compilatonStateFile);
	}

	/**
	 * Returns the file that contains the compiler output from the
	 * {@link IBaseDataContainer}.
	 * 
	 * @param baseDataContainer
	 * @param URI
	 *            uri
	 * @return
	 * @throws IOException
	 */
	private static File getCompilerOutputFile(
			IBaseDataContainer baseDataContainer, URI uri) throws IOException {
		return baseDataContainer.getStaticFile(SCOPE,
				DigestUtils.md5Hex(uri.toString()) + ".compiler_output.html");
	}

	/**
	 * Saves changes made to the compilation output file in the
	 * {@link IBaseDataContainer}.
	 * 
	 * @param baseDataContainer
	 * @param URI
	 *            uri
	 * @param compilerOutputFile
	 * @throws IOException
	 */
	private static void setCompilerOutputFile(
			IBaseDataContainer baseDataContainer, URI uri,
			File compilerOutputFile) throws IOException {
		baseDataContainer.putFile(SCOPE, DigestUtils.md5Hex(uri.toString())
				+ ".compiler_output.html", compilerOutputFile);
	}

	private static File getExecutionOutputFile(
			IBaseDataContainer baseDataContainer, URI uri) throws IOException {
		return baseDataContainer.getStaticFile(SCOPE,
				DigestUtils.md5Hex(uri.toString()) + ".execution_output.html");
	}

	private static void setExecutionOutputFile(
			IBaseDataContainer baseDataContainer, URI uri,
			File executionOutputFile) throws IOException {
		baseDataContainer.putFile(SCOPE, DigestUtils.md5Hex(uri.toString())
				+ ".execution_output.html", executionOutputFile);
	}

	/**
	 * Returns the compilation states from the given {@link IBaseDataContainer}
	 * s.
	 * 
	 * @param baseDataContainers
	 * @return
	 * @throws IOException
	 */
	static Map<URI, Boolean> getCompilationStates(
			IBaseDataContainer[] baseDataContainers) throws IOException {
		Map<URI, Boolean> compilationStates = new HashMap<URI, Boolean>();
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			File compilationStateFile = getCompilationStateFile(baseDataContainer);
			compilationStates.putAll(CompilationStateReaderWriter
					.fromFile(compilationStateFile));
		}
		return compilationStates;
	}

	/**
	 * Sets the compilation states to the given {@link IBaseDataContainer}.
	 * 
	 * @param baseDataContainers
	 * @param compilationStates
	 * @throws IOException
	 */
	static void setCompilationStates(IBaseDataContainer[] baseDataContainers,
			Map<URI, Boolean> compilationStates) throws IOException {
		File compilationStateFile = CompilationStateReaderWriter
				.toFile(compilationStates);
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			setCompilationStateFile(baseDataContainer, compilationStateFile);
		}
	}

	/**
	 * Returns the compiler output for the given {@link URI} from the given
	 * {@link IBaseDataContainer}s.
	 * 
	 * @param baseDataContainers
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	static String getCompilerOutput(IBaseDataContainer[] baseDataContainers,
			URI uri) throws IOException {
		String compilerOutput = null;
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			compilerOutput = OutputReaderWriter
					.fromFile(getCompilerOutputFile(baseDataContainer, uri));
			if (compilerOutput != null && !compilerOutput.trim().isEmpty())
				break;
		}
		return compilerOutput;
	}

	/**
	 * Sets the compiler output for the given {@link URI} to the given
	 * {@link IBaseDataContainer}.
	 * 
	 * @param baseDataContainers
	 * @param uri
	 * @param compilationStates
	 * @throws IOException
	 */
	static void setCompilerOutput(IBaseDataContainer[] baseDataContainers,
			URI uri, String compilerOutput) throws IOException {
		Assert.isNotNull(baseDataContainers);
		Assert.isNotNull(uri);
		File compilerOutputFile = OutputReaderWriter
				.toFile(compilerOutput);
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			setCompilerOutputFile(baseDataContainer, uri, compilerOutputFile);
		}
	}

	/**
	 * Returns the output that is generated when the given {@link URI}'s file
	 * from the given {@link IBaseDataContainer}s is executed.
	 * 
	 * @param baseDataContainers
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	static String getExecutionOutput(IBaseDataContainer[] baseDataContainers,
			URI uri) throws IOException {
		String executionOutput = null;
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			executionOutput = OutputReaderWriter
					.fromFile(getExecutionOutputFile(baseDataContainer, uri));
			if (executionOutput != null && !executionOutput.trim().isEmpty())
				break;
		}
		return executionOutput;
	}

	/**
	 * Sets the output that is generated when the given {@link URI}'s file of
	 * the given {@link IBaseDataContainer} is executed.
	 * 
	 * @param baseDataContainers
	 * @param uri
	 * @param executionOutput
	 * @throws IOException
	 */
	static void setExecutionOutput(IBaseDataContainer[] baseDataContainers,
			URI uri, String executionOutput) throws IOException {
		Assert.isNotNull(baseDataContainers);
		Assert.isNotNull(uri);
		File executionOutputFile = OutputReaderWriter
				.toFile(executionOutput);
		for (IBaseDataContainer baseDataContainer : baseDataContainers) {
			setExecutionOutputFile(baseDataContainer, uri, executionOutputFile);
		}
	}

}
