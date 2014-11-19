package de.fu_berlin.imp.apiua.survey.views;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.NamedJob;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.MouseAdapter;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.apiua.core.services.DataServiceAdapter;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.core.services.IDataServiceListener;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.apiua.core.services.IImportanceServiceListener;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.survey.Activator;
import de.fu_berlin.imp.apiua.survey.model.SurveyContainer;
import de.fu_berlin.imp.apiua.survey.viewer.CDViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class CDView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.apiua.survey.views.CDView";

	public static final Logger LOGGER = Logger.getLogger(CDView.class);

	private final IDataService dataService = (IDataService) PlatformUI
			.getWorkbench().getService(IDataService.class);
	private final IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		@Override
		public void dataDirectoriesLoaded(
				List<? extends IBaseDataContainer> dataContainers) {
			CDView.this.load();
		}
	};

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private final IImportanceService importanceService = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);
	private final IImportanceServiceListener importanceServiceListener = new IImportanceServiceListener() {
		@Override
		public void importanceChanged(Set<URI> uris, Importance importance) {
			CDView.this.viewer.refresh();
		}
	};

	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void memoRemoved(URI uri) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void memoModified(URI uri) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void memoAdded(URI uri) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void episodesDeleted(Set<IEpisode> deletedEpisodes) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes, List<URI> uris) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void codesAdded(List<ICode> codes) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void codeDeleted(ICode code) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void relationsAdded(Set<IRelation> relations) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			CDView.this.viewer.refresh();
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			CDView.this.viewer.refresh();
		};

		@Override
		public void axialCodingModelAdded(URI uri) {
		}

		@Override
		public void axialCodingModelUpdated(URI uri) {
		}

		@Override
		public void axialCodingModelRemoved(URI uri) {
		}
	};

	private CDViewer viewer = null;
	private BootstrapBrowser browser = null;

	public CDView() {
		this.dataService.addDataServiceListener(this.dataServiceListener);
		this.importanceService
				.addImportanceServiceListener(this.importanceServiceListener);
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void dispose() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
		this.importanceService
				.removeImportanceServiceListener(this.importanceServiceListener);
		this.dataService.removeDataServiceListener(this.dataServiceListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.browser = new BootstrapBrowser(parent, SWT.NONE);
		this.browser.deactivateNativeMenu();
		this.browser.setAllowLocationChange(true);
		this.browser.openBlank();
		this.browser.addMouseListener(new MouseAdapter() {
			@Override
			public void clicked(double x, double y, IElement element) {
				String uri = element != null ? element
						.getAttribute("data-workspace") : null;
				if (uri != null) {
					CDView.this.locatorService.showInWorkspace(new URI(uri),
							false, null);
				}
			}
		});

		this.viewer = new CDViewer(this.browser);
		new ContextMenu(this.viewer, this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return null;
			}
		};
	}

	@Override
	public void setFocus() {
		this.load();
	}

	public Job load() {
		Job job = new NamedJob(CDView.class,
				"Loading Cognitive Dimensions Questionnaire") {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				final SurveyContainer surveyContainer = Activator.getDefault()
						.getSurveyContainer();
				if (CDView.this.viewer.getInput() != surveyContainer) {
					CDView.this.viewer.setInput(surveyContainer);
					Point lastScrollPosition = new SUACorePreferenceUtil()
							.getLastScrollPosition(CDViewer.class);
					try {
						CDView.this.browser.scrollTo(lastScrollPosition).get();
					} catch (Exception e) {
						LOGGER.error("Error " + surveyContainer + " in "
								+ CDViewer.class, e);
					}
				}

				// ExecUtils.asyncExec(new Runnable() {
				// @Override
				// public void run() {
				// CDView.this.browser.setFocus();
				// }
				// });

				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return job;
	}

	/**
	 * Scrolls to the first {@link ILocatable} given.
	 *
	 * @param URIS
	 * @param callable
	 * @return
	 */
	// TODO auf Viewer setSelection aufrufen
	public Future<ILocatable[]> open(ILocatable[] locatables,
			Callable<ILocatable[]> callable) {
		if (locatables.length > 0) {
			final ILocatable locatable = locatables[0];
			final Future<Boolean> pos = this.browser
					.scrollTo(new ISelector.NameSelector(locatable.getUri()
							.toString()));
			return ExecUtils.nonUISyncExec(new Callable<ILocatable[]>() {
				@Override
				public ILocatable[] call() throws Exception {
					pos.get();
					return new ILocatable[] { locatable };
				}
			});
		}
		return new CompletedFuture<ILocatable[]>(new ILocatable[0], null);
	}

}
