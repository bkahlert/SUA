package de.fu_berlin.imp.seqan.usability_analyzer.survey.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.SurveyContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.SurveyViewer;

public class SurveyView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.survey.views.SurveyView";
	public static final Logger LOGGER = Logger.getLogger(SurveyView.class);

	private IWorkSessionService workSessionService;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void workSessionStarted(IWorkSession workSession) {
			HashSet<IIdentifier> idsOrTokens = new HashSet<IIdentifier>();
			idsOrTokens.addAll(ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), IIdentifier.class));
			SurveyView.this.open(idsOrTokens, null);
		}
	};

	private SurveyViewer surveyViewer;
	private HashMap<IIdentifier, SurveyRecord> openedSurveyRecords = new HashMap<IIdentifier, SurveyRecord>();

	public SurveyView() {
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		if (this.workSessionService == null) {
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());
		}

		if (this.workSessionService != null) {
			this.workSessionService
					.addWorkSessionListener(this.workSessionListener);
		}
	}

	@Override
	public void dispose() {
		if (this.workSessionService != null) {
			this.workSessionService
					.removeWorkSessionListener(this.workSessionListener);
		}
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		this.surveyViewer = new SurveyViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		final Tree tree = this.surveyViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.surveyViewer.setContentProvider(new SurveyContentProvider());
	}

	@Override
	public void setFocus() {
		this.surveyViewer.getControl().setFocus();
	}

	/**
	 * Opens the given {@link IIdentifier}s. If the corresponding
	 * {@link SurveyRecord}s could be successfully opened a caller defined
	 * {@link Runnable} gets executed.
	 * <p>
	 * Note: The {@link Runnable} is executed in the UI thread.
	 * 
	 * @param <T>
	 * 
	 * @param identifiers
	 * @param success
	 */
	public <T> Future<T> open(final HashSet<IIdentifier> identifiers,
			final Callable<T> success) {
		final HashMap<IIdentifier, SurveyRecord> newOpenedSurveyRecords = new HashMap<IIdentifier, SurveyRecord>();

		// do not load already opened survey records
		for (IIdentifier identifier : this.openedSurveyRecords.keySet()) {
			if (identifiers.contains(identifier)) {
				newOpenedSurveyRecords.put(identifier,
						this.openedSurveyRecords.get(identifier));
				identifiers.remove(identifier);
			}
		}

		// Case 1: no IDs
		if (identifiers.size() == 0) {
			if (success != null) {
				return ExecutorUtil.asyncExec(success);
			} else {
				return null;
			}
		}

		// Case 2: multiple IDs
		for (IIdentifier identifier : identifiers) {
			SurveyRecord surveyRecord = Activator.getDefault()
					.getSurveyContainer().getSurveyRecord(identifier);
			if (surveyRecord != null
					&& !this.openedSurveyRecords.containsValue(surveyRecord)
					&& !newOpenedSurveyRecords.containsValue(surveyRecord)) {
				newOpenedSurveyRecords.put(identifier, surveyRecord);
			}
		}

		return ExecutorUtil.nonUIAsyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				if (SurveyView.this.surveyViewer != null
						&& SurveyView.this.surveyViewer.getTree() != null
						&& !SurveyView.this.surveyViewer.getTree().isDisposed()) {
					SurveyView.this.openedSurveyRecords = newOpenedSurveyRecords;
					final String partName = "Survey - "
							+ StringUtils.join(newOpenedSurveyRecords.keySet(),
									", ");
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							SurveyView.this.setPartName(partName);
							SurveyView.this.surveyViewer
									.setInput(newOpenedSurveyRecords.values());
							SurveyView.this.surveyViewer.expandAll();
						}
					});
				}

				if (success != null) {
					return success.call();
				} else {
					return null;
				}
			}
		});
	}

}
