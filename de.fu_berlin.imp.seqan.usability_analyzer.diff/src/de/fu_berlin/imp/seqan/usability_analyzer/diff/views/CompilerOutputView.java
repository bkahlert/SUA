package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class CompilerOutputView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.diff.views.CompilerOutputView";

	private static final Logger LOGGER = Logger
			.getLogger(CompilerOutputView.class);

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<ICompilable> compilables = SelectionUtils.getAdaptableObjects(
					selection, ICompilable.class);
			if (compilables.size() > 0)
				compilable = compilables.get(0);
			load();
		}
	};

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {

		@Override
		public void memoRemoved(ICodeable codeable) {
			if (compilable != null && compilable.equals(codeable))
				refreshHeader();
		}

		@Override
		public void memoRemoved(ICode code) {
			if (compilable != null && compilable.equals(code))
				refreshHeader();
		}

		@Override
		public void memoAdded(ICodeable codeable) {
			if (compilable != null && compilable.equals(codeable))
				refreshHeader();
		}

		@Override
		public void memoAdded(ICode code) {
			if (compilable != null && compilable.equals(code))
				refreshHeader();
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes,
				List<ICodeable> codeables) {
			for (ICodeable codeable : codeables) {
				if (compilable != null && compilable.equals(codeable)) {
					refreshHeader();
					break;
				}
			}
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			for (ICodeable codeable : codeables) {
				if (compilable != null && compilable.equals(codeable)) {
					refreshHeader();
					break;
				}
			}
		}

		@Override
		public void codeDeleted(ICode code) {
			refreshHeader();
		}
	};
	private ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
		@Override
		public void compilationStateChanged(ICompilable[] compilables,
				Boolean state) {
			for (ICompilable compilable : compilables) {
				if (CompilerOutputView.this.compilable != null
						&& CompilerOutputView.this.compilable
								.equals(compilable)) {
					refreshHeader();
					break;
				}
			}
		}
	};

	private Editor editor;
	private ICompilable compilable = null;
	private Job compilerOutputLoader = null;
	private Job compilerOutputSaver = null;

	public CompilerOutputView() {

	}

	protected void refreshHeader() {
		// TODO dirty, compilationservice should provide this label
		// provider
		final String text;
		final Image image;
		if (compilable != null) {
			ILabelProvider labelProvider = codeService
					.getLabelProvider(compilable.getUri());
			if (labelProvider != null) {
				text = "Compiler Output - " + labelProvider.getText(compilable);
				image = labelProvider.getImage(compilable);
			} else {
				LOGGER.warn("No label provider found for " + compilable);
				text = null;
				image = null;
			}
		} else {
			text = null;
			image = null;
		}

		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				setPartName(text != null ? text : "Compiler Output");
				setTitleImage(image != null ? image
						: ImageManager.COMPILEROUTPUT_MISC);
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.editor = new Editor(parent, SWT.NONE, 200);
		this.editor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				save((String) e.data);
			}
		});
		this.editor.setEnabled(false);

		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});

		SelectionUtils.getSelectionService(getSite().getWorkbenchWindow())
				.addPostSelectionListener(selectionListener);
		this.codeService.addCodeServiceListener(codeServiceListener);
		this.compilationService
				.addCompilationServiceListener(compilationServiceListener);
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(compilationServiceListener);
		this.codeService.removeCodeServiceListener(codeServiceListener);
		SelectionUtils.getSelectionService(getSite().getWorkbenchWindow())
				.removePostSelectionListener(selectionListener);
		super.dispose();
	}

	synchronized public void load() {
		if (compilerOutputLoader != null)
			compilerOutputLoader.cancel();

		if (compilable == null) {
			refreshHeader();
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					editor.setSource("");
					editor.setEnabled(false);
				}
			});
		} else {
			compilerOutputLoader = new Job("Loading Compiler Memo") {
				@Override
				protected IStatus run(IProgressMonitor progressMonitor) {
					if (progressMonitor.isCanceled())
						return Status.CANCEL_STATUS;
					SubMonitor monitor = SubMonitor.convert(progressMonitor, 3);
					final String compilerOutput = compilationService
							.compilerOutput(compilable);
					monitor.worked(1);
					refreshHeader();
					monitor.worked(1);
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							editor.setSource(compilerOutput);
							editor.setEnabled(true);
						}
					});
					monitor.worked(1);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			compilerOutputLoader.schedule();
		}
	}

	synchronized public void save(String html) {
		if (compilerOutputSaver != null)
			compilerOutputSaver.cancel();

		if (compilable != null) {
			compilerOutputSaver = new Job("Saving Compiler Output") {
				@Override
				protected IStatus run(IProgressMonitor progressMonitor) {
					if (progressMonitor.isCanceled())
						return Status.CANCEL_STATUS;
					SubMonitor monitor = SubMonitor.convert(progressMonitor, 2);

					String compilerOutput;
					try {
						compilerOutput = ExecutorUtil
								.syncExec(new Callable<String>() {
									@Override
									public String call() {
										return editor.getSource();
									}
								});
					} catch (Exception e) {
						LOGGER.error("Error writing compiler output", e);
						return Status.CANCEL_STATUS;
					}
					monitor.worked(1);
					compilationService.compilerOutput(compilable,
							compilerOutput);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			compilerOutputSaver.schedule();
		}
	}

	@Override
	public void setFocus() {
		if (this.editor != null && !this.editor.isDisposed())
			this.editor.setFocus();
	}

}
