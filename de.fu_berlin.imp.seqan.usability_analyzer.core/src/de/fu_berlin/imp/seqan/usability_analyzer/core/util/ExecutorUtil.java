package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

public class ExecutorUtil {

	private static final Logger LOGGER = Logger.getLogger(ExecutorUtil.class);
	private static final ExecutorService asyncExecPool = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

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

	public static <V> V syncExec(final Callable<V> callable) throws Exception {
		final AtomicReference<V> r = new AtomicReference<V>();
		final AtomicReference<Exception> exception = new AtomicReference<Exception>();
		final Semaphore mutex = new Semaphore(0);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					r.set(callable.call());
				} catch (Exception e) {
					exception.set(e);
				}
				mutex.release();
			}
		});
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			LOGGER.error(e);
		}
		if (exception.get() != null)
			throw exception.get();
		return r.get();
	}

	public static void syncExec(Runnable runnable) {
		Display.getDefault().syncExec(runnable);
	}

	public static <V> Future<V> asyncExec(final Callable<V> callable) {
		return asyncExecPool.submit(new Callable<V>() {
			@Override
			public V call() throws Exception {
				final AtomicReference<V> r = new AtomicReference<V>();
				final AtomicReference<Exception> exception = new AtomicReference<Exception>();
				final Semaphore mutex = new Semaphore(0);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							r.set(callable.call());
						} catch (Exception e) {
							exception.set(e);
						}
						mutex.release();
					}
				});
				mutex.acquire();
				if (exception.get() != null)
					throw exception.get();
				return r.get();
			}
		});
	}

	public static void asyncExec(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	public static boolean isUIThread() {
		return Display.getCurrent() == Display.getDefault();
	}

	/**
	 * Runs the runnable in a separate {@link Thread} and returns it.
	 * 
	 * @param
	 * @return
	 */
	public static Thread asyncRun(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.start();
		return thread;
	}

	private ExecutorUtil() {
	}
}
