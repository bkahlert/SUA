package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

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
	 * Returns a list of allowed namespaces.
	 * 
	 * @return
	 */
	public abstract List<String> getAllowedNamespaces();

	@Override
	public final FutureTask<ICodeable> getCodedObject(final URI codeInstanceID) {
		List<String> allowedNamespaces = getAllowedNamespaces();
		if (allowedNamespaces.contains(codeInstanceID.getHost())) {
			final AtomicReference<IProgressMonitor> monitorReference = new AtomicReference<IProgressMonitor>();
			final FutureTask<ICodeable> futureTask = new FutureTask<ICodeable>(
					getCodedObjectCallable(monitorReference, codeInstanceID));
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
				if (codeable != null)
					codeables.add(codeable);
			} catch (Exception e) {
				logger.error("Could not find the coded object", e);
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
			if (futureCodeable != null)
				futureCodeables.add(futureCodeable);
		}
		return futureCodeables;
	}

	@Override
	public final void showCodedObjectsInWorkspace(
			final List<URI> codeInstanceIDs) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<Future<ICodeable>> futureCodeables = getCodedObjectFutures(codeInstanceIDs);
				final List<ICodeable> codedObjects = getCodedObjects(futureCodeables);
				if (codedObjects.size() > 0)
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							showCodedObjectsInWorkspace2(codedObjects);
						}
					});
			}
		}).start();
	}

	/**
	 * Shows the coded objects in the workspace.
	 * 
	 * @param codedObjects
	 *            are the objects to be shown. Only objects determined by
	 *            {@link URI} matching to the allowed namespaces returned by
	 *            {@link #getAllowedNamespaces()} are passed.
	 *            <p>
	 *            e.g. given the URIs sua://abc/... and sua://xyz/... and the
	 *            namespace xyz only the object described by sua://xyz/... would
	 *            be in the list.
	 */
	public abstract void showCodedObjectsInWorkspace2(
			List<ICodeable> codedObjects);
}
