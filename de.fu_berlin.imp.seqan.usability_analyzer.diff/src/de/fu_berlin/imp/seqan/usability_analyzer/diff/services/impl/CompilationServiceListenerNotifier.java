package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import java.util.ArrayList;
import java.util.List;

import com.bkahlert.devel.nebula.utils.ExecutorService;
import com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;

public class CompilationServiceListenerNotifier {
	private final List<ICompilationServiceListener> compilationServiceListeners = new ArrayList<ICompilationServiceListener>();
	private static final ExecutorService EXECUTOR_SERVICE = new ExecutorService(
			CompilationServiceListenerNotifier.class, 1);

	public void addCompilationServiceListener(
			ICompilationServiceListener compilationServiceListener) {
		this.compilationServiceListeners.add(compilationServiceListener);
	}

	public void removeCompilationServiceListener(
			ICompilationServiceListener compilationServiceListener) {
		this.compilationServiceListeners.remove(compilationServiceListener);
	}

	public void compilationStateChanged(final ICompilable[] compilables,
			final Boolean state) {
		EXECUTOR_SERVICE.nonUIAsyncExec(this.compilationServiceListeners,
				new ParametrizedCallable<ICompilationServiceListener, Void>() {
					@Override
					public Void call(
							ICompilationServiceListener compilationServiceListener)
							throws Exception {
						compilationServiceListener.compilationStateChanged(
								compilables, state);
						return null;
					}
				});
	}

	public void compilerOutputChanged(final ICompilable compilable,
			final String html) {
		EXECUTOR_SERVICE.nonUIAsyncExec(this.compilationServiceListeners,
				new ParametrizedCallable<ICompilationServiceListener, Void>() {
					@Override
					public Void call(
							ICompilationServiceListener compilationServiceListener)
							throws Exception {
						compilationServiceListener.compilerOutputChanged(
								compilable, html);
						return null;
					}
				});
	}

	public void executionOutputChanged(final ICompilable compilable,
			final String html) {
		EXECUTOR_SERVICE.nonUIAsyncExec(this.compilationServiceListeners,
				new ParametrizedCallable<ICompilationServiceListener, Void>() {
					@Override
					public Void call(
							ICompilationServiceListener compilationServiceListener)
							throws Exception {
						compilationServiceListener.executionOutputChanged(
								compilable, html);
						return null;
					}
				});
	}
}
