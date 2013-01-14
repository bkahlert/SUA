package de.fu_berlin.imp.seqan.usability_analyzer.survey.views;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
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
			final List<ID> ids = ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), ID.class);
			final List<Token> tokens = ArrayUtils.getAdaptableObjects(
					workSession.getEntities().toArray(), Token.class);
			HashSet<Object> idsOrTokens = new HashSet<Object>();
			idsOrTokens.addAll(ids);
			idsOrTokens.addAll(tokens);
			open(idsOrTokens, null);
		}
	};

	private SurveyViewer surveyViewer;
	private HashMap<Object, SurveyRecord> openedSurveyRecords = new HashMap<Object, SurveyRecord>();

	public SurveyView() {
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		if (this.workSessionService == null)
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());

		if (this.workSessionService != null)
			this.workSessionService.addWorkSessionListener(workSessionListener);
	}

	@Override
	public void dispose() {
		if (this.workSessionService != null)
			this.workSessionService
					.removeWorkSessionListener(workSessionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		this.surveyViewer = new SurveyViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		final Tree tree = surveyViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.surveyViewer.setContentProvider(new SurveyContentProvider());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * Opens the given {@link ID}s and {@link Token}s. If the corresponding
	 * {@link SurveyRecord}s could be successfully opened a caller defined
	 * {@link Runnable} gets executed.
	 * <p>
	 * Note: The {@link Runnable} is executed in the UI thread.
	 * 
	 * @param <T>
	 * 
	 * @param idsOrTokens
	 * @param success
	 */
	public <T> Future<T> open(final HashSet<Object> idsOrTokens,
			final Callable<T> success) {
		final HashMap<Object, SurveyRecord> newOpenedSurveyRecords = new HashMap<Object, SurveyRecord>();

		// do not load already opened survey records
		for (Object idOrToken : openedSurveyRecords.keySet()) {
			if (idsOrTokens.contains(idOrToken)) {
				newOpenedSurveyRecords.put(idOrToken,
						openedSurveyRecords.get(idOrToken));
				idsOrTokens.remove(idOrToken);
			}
		}

		// Case 1: no IDs
		if (idsOrTokens.size() == 0) {
			if (success != null) {
				return ExecutorUtil.asyncExec(success);
			} else
				return null;
		}

		// Case 2: multiple IDs
		for (Object idOrToken : idsOrTokens) {
			SurveyRecord surveyRecord = null;
			if (idOrToken instanceof ID) {
				surveyRecord = Activator.getDefault().getSurveyContainer()
						.getSurveyRecord((ID) idOrToken);
			} else if (idOrToken instanceof Token) {
				surveyRecord = Activator.getDefault().getSurveyContainer()
						.getSurveyRecord((Token) idOrToken);
			} else {
				throw new InvalidParameterException();
			}
			if (surveyRecord != null
					&& !openedSurveyRecords.containsValue(surveyRecord)
					&& !newOpenedSurveyRecords.containsValue(surveyRecord)) {
				newOpenedSurveyRecords.put(idOrToken, surveyRecord);
			}
		}

		return ExecutorUtil.nonUIAsyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				if (surveyViewer != null && surveyViewer.getTree() != null
						&& !surveyViewer.getTree().isDisposed()) {
					openedSurveyRecords = newOpenedSurveyRecords;
					final String partName = "Survey - "
							+ StringUtils.join(newOpenedSurveyRecords.keySet(),
									", ");
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							setPartName(partName);
							surveyViewer.setInput(newOpenedSurveyRecords
									.values());
							surveyViewer.expandAll();
						}
					});
				}

				if (success != null)
					return success.call();
				else
					return null;
			}
		});
	}

}
