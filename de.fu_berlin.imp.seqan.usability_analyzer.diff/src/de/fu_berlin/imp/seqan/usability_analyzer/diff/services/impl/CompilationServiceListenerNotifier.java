package de.fu_berlin.imp.seqan.usability_analyzer.diff.services.impl;

import java.util.ArrayList;
import java.util.List;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.browser.listener.SchemeAnkerListener;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;

public class CompilationServiceListenerNotifier {
	private final List<ICompilationServiceListener> compilationServiceListeners = new ArrayList<ICompilationServiceListener>();

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
		ExecUtils
				.nonUIAsyncExec(
						CompilationServiceListenerNotifier.class,
						"Compilation State Changed Notification",
						this.compilationServiceListeners,
						new ExecUtils.ParametrizedCallable<ICompilationServiceListener, Void>() {
							@Override
							public Void call(
									ICompilationServiceListener compilationServiceListener)
									throws Exception {
								compilationServiceListener
										.compilationStateChanged(compilables,
												state);
								return null;
							}
						});
	}

	public void compilerOutputChanged(final ICompilable compilable,
			final String html) {
		ExecUtils
				.nonUIAsyncExec(
						CompilationServiceListenerNotifier.class,
						"Compilation Output Changed Notification",
						this.compilationServiceListeners,
						new ExecUtils.ParametrizedCallable<ICompilationServiceListener, Void>() {
							@Override
							public Void call(
									ICompilationServiceListener compilationServiceListener)
									throws Exception {
								compilationServiceListener
										.compilerOutputChanged(compilable, html);
								return null;
							}
						});
	}

	public void executionOutputChanged(final ICompilable compilable,
			final String html) {
		ExecUtils
				.nonUIAsyncExec(
						SchemeAnkerListener.class,
						"Execution Output Changed Notification",
						this.compilationServiceListeners,
						new ExecUtils.ParametrizedCallable<ICompilationServiceListener, Void>() {
							@Override
							public Void call(
									ICompilationServiceListener compilationServiceListener)
									throws Exception {
								compilationServiceListener
										.executionOutputChanged(compilable,
												html);
								return null;
							}
						});
	}
}
