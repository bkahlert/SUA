package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IOpenable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

/**
 * This class provides a standard implementation of {@link ICodeableProvider}.
 * It automatically retrieves the coded objects from given {@link URI}s asynchronously and calls {@link #showCodedObjectsInWorkspace2(List).
 * @author bkahlert
 *
 */
public abstract class CodeableProvider implements ICodeableProvider {

	private final Logger logger = Logger.getLogger(CodeableProvider.class);

	/**
	 * Used to call one {@link ICodeableProvider} per {@link Thread}
	 */
	private static final ExecutorService pool = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

	/**
	 * Returns a list of allowed namespaces.
	 * 
	 * @return
	 */
	public abstract List<String> getAllowedNamespaces();

	@Override
	public final FutureTask<ICodeable> getCodedObject(final URI codeInstanceID) {
		List<String> allowedNamespaces = this.getAllowedNamespaces();
		if (allowedNamespaces.contains(codeInstanceID.getHost())) {
			final AtomicReference<IProgressMonitor> monitorReference = new AtomicReference<IProgressMonitor>();
			final FutureTask<ICodeable> futureTask = new FutureTask<ICodeable>(
					this.getCodedObjectCallable(monitorReference,
							codeInstanceID));
			new Job("Coded object retrieval") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitorReference.set(monitor);
					futureTask.run();
					return Status.OK_STATUS;
				}
			}.schedule();
			return futureTask;
		}
		return null;
	}

	/**
	 * Returns a list of coded objects the given {@link Future}s are supposed to
	 * find.
	 * 
	 * @blocking
	 * @param futureCodeables
	 * @return
	 */
	public final List<ICodeable> getCodedObjects(
			List<Future<ICodeable>> futureCodeables) {
		List<ICodeable> codeables = new ArrayList<ICodeable>();
		for (Future<ICodeable> futureCodeable : futureCodeables) {
			ICodeable codeable;
			try {
				codeable = futureCodeable.get();
				if (codeable != null) {
					codeables.add(codeable);
				}
			} catch (Exception e) {
				this.logger.error("Could not find the coded object", e);
			}
		}
		return codeables;
	}

	/**
	 * Returns a {@link Callable} which itself return the coded object described
	 * by the {@link URI}.
	 * 
	 * @param monitorReference
	 * @param codeInstanceID
	 * @return
	 */
	public abstract Callable<ICodeable> getCodedObjectCallable(
			AtomicReference<IProgressMonitor> monitorReference,
			URI codeInstanceID);

	/**
	 * Returns a list of {@link Future}s which find the coded objects described
	 * by the {@link URI}s.
	 * 
	 * @param codeInstanceIDs
	 *            that describe the coded objects to be returned
	 * @param namespace
	 *            for which to filter the {@link URI}s
	 * @return
	 * @see {@link #getAllowedNamespaces()}
	 */
	public final List<Future<ICodeable>> getCodedObjectFutures(
			List<URI> codeInstanceIDs) {
		List<Future<ICodeable>> futureCodeables = new ArrayList<Future<ICodeable>>();
		for (URI codeInstanceID : codeInstanceIDs) {
			Future<ICodeable> futureCodeable = this
					.getCodedObject(codeInstanceID);
			if (futureCodeable != null) {
				futureCodeables.add(futureCodeable);
			}
		}
		return futureCodeables;
	}

	@Override
	public final Future<Boolean> showCodedObjectsInWorkspace(
			final List<URI> codeInstanceIDs, final boolean show) {
		return pool.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				final List<Future<ICodeable>> futureCodeables = CodeableProvider.this
						.getCodedObjectFutures(codeInstanceIDs);
				final List<ICodeable> codedObjects = CodeableProvider.this
						.getCodedObjects(futureCodeables);
				boolean convertedAll = codedObjects.size() == futureCodeables
						.size();
				if (codedObjects.size() > 0) {
					ICodeable[] showedCodeables = CodeableProvider.this
							.showCodedObjectsInWorkspace2(codedObjects);
					boolean showedAll = showedCodeables != null
							&& codeInstanceIDs.size() == showedCodeables.length;
					if (show) {
						if (showedCodeables == null) {
							return false;
						}
						for (final IOpenable openable : ArrayUtils
								.getAdaptableObjects(showedCodeables,
										IOpenable.class)) {
							ExecutorUtil.syncExec(new Runnable() {
								@Override
								public void run() {
									openable.open();
								}
							});
						}
						// TODO check if could be opened
						return convertedAll && showedAll;
					} else {
						return convertedAll && showedAll;
					}
				} else {
					return convertedAll;
				}
			}
		});
	}

	/**
	 * Shows the coded objects in the workspace.
	 * <p>
	 * Note: This method is called in a separate thread and is allowed to be
	 * time consuming.
	 * 
	 * @param codedObjects
	 *            are the objects to be shown. Only objects determined by
	 *            {@link URI} matching to the allowed namespaces returned by
	 *            {@link #getAllowedNamespaces()} are passed.
	 *            <p>
	 *            e.g. given the URIs sua://abc/... and sua://xyz/... and the
	 *            namespace xyz only the object described by sua://xyz/... would
	 *            be in the list.
	 * @return all objects that could be displayed
	 */
	public abstract ICodeable[] showCodedObjectsInWorkspace2(
			List<ICodeable> codedObjects);

}
