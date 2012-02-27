package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsUtil {

	/**
	 * Creates a thread pool that reuses a fixed number of threads operating off
	 * a shared unbounded queue. At any point, at most a multiple times of the
	 * available processors count threads will be active processing tasks.
	 * 
	 * @param multiple
	 * @return
	 * 
	 * @see Executors#newFixedThreadPool(int)
	 */
	public static ExecutorService newFixedMultipleOfProcessorsThreadPool(
			int multiple) {
		int numProcessors = Runtime.getRuntime().availableProcessors();
		return Executors.newFixedThreadPool(multiple * numProcessors);
	}

	private ExecutorsUtil() {
	}
}
