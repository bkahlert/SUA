package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.views.UriPresentingEditorView;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EditorOnlyMemoView;

/**
 * TODO document
 * 
 * @author bkahlert
 * 
 */
public abstract class AbstractOutputView extends UriPresentingEditorView {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractOutputView.class);

	final private boolean selectionSensitive;
	final private boolean editorSensitive;

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<ICompilable> compilables = SelectionUtils.getAdaptableObjects(
					selection, ICompilable.class);
			if (compilables.size() > 0) {
				AbstractOutputView.this.load(compilables.get(0).getUri());
			}
		}
	};

	private final IPartListener partListener = new IPartListener() {

		private ICompilable getCompilable(IWorkbenchPart part) {
			ISelection selection = SelectionUtils.getSelection(part.getSite()
					.getWorkbenchWindow());
			if (selection == null) {
				return null;
			}
			return (ICompilable) Platform.getAdapterManager().getAdapter(
					selection, ICompilable.class);
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part.getClass() == EditorOnlyMemoView.class) {
				return;
			}
			ICompilable compilable = this.getCompilable(part);
			if (compilable != null) {
				AbstractOutputView.this.load(compilable.getUri());
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
		}
	};

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void memoRemoved(URI uri) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void memoAdded(URI uri) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes, List<URI> uris) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codeDeleted(ICode code) {
			AbstractOutputView.this.refreshHeader();
		}
	};
	private final ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private final ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
		@Override
		public void compilationStateChanged(ICompilable[] compilables,
				Boolean state) {
			AbstractOutputView.this.refreshHeader();
		}
	};

	public AbstractOutputView(boolean selectionSensitive,
			boolean editorSensitive) {
		super(2000, ToolbarSet.TERMINAL, true);
		this.selectionSensitive = selectionSensitive;
		this.editorSensitive = editorSensitive;
	}

	public ILabelProviderService getLabelProviderService() {
		return this.labelProviderService;
	}

	public ICodeService getCodeService() {
		return this.codeService;
	}

	public ICompilationService getCompilationService() {
		return this.compilationService;
	}

	@Override
	public void postInit() {
		super.postInit();
		if (this.selectionSensitive) {
			SelectionUtils.getSelectionService(
					this.getSite().getWorkbenchWindow())
					.addPostSelectionListener(this.selectionListener);
		}
		if (this.editorSensitive) {
			this.getSite().getPage().addPartListener(this.partListener);
		}
		this.codeService.addCodeServiceListener(this.codeServiceListener);
		this.compilationService
				.addCompilationServiceListener(this.compilationServiceListener);
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(this.compilationServiceListener);
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
		if (this.editorSensitive) {
			this.getSite().getPage().removePartListener(this.partListener);
		}
		if (this.selectionSensitive) {
			SelectionUtils.getSelectionService(
					this.getSite().getWorkbenchWindow())
					.removePostSelectionListener(this.selectionListener);
		}
		super.dispose();
	}

	@Override
	public PartInfo getPartInfo(List<URI> uris) throws Exception {
		if (uris == null || uris.size() == 0) {
			return this.getDefaultPartInfo();
		}

		String[] captions = new String[uris.size()];
		Image[] images = new Image[uris.size()];
		for (int i = 0; i < uris.size(); i++) {
			ILabelProvider lp = this.labelProviderService.getLabelProvider(uris
					.get(i));
			if (lp == null) {
				captions[i] = "UNKNOWN";
				images[i] = null;
			} else {
				captions[i] = lp.getText(uris.get(i));
				images[i] = lp.getImage(uris.get(i));
			}
		}
		String caption = StringUtils.join(captions, ", ");
		Image image = images[0];
		for (int i = 1; i < images.length && image != null; i++) {
			if (image != images[i]) {
				image = null;
			}
		}
		return new PartInfo(this.getPartInfoPrefix() + caption, image);
	}

	protected abstract String getPartInfoPrefix();

}
