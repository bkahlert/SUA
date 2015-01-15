package de.fu_berlin.imp.apiua.survey.views;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.NamedJob;

import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.services.DataServiceAdapter;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.core.services.IDataServiceListener;
import de.fu_berlin.imp.apiua.survey.Activator;
import de.fu_berlin.imp.apiua.survey.model.SurveyContainer;
import de.fu_berlin.imp.apiua.survey.model.groupdiscussion.GroupDiscussionDocument;
import de.fu_berlin.imp.apiua.survey.viewer.HtmlCodingComposite;
import de.ralfebert.rcputils.menus.ContextMenu;

public class GroupDiscussionCodingView extends ViewPart {

	private static final Logger LOGGER = Logger
			.getLogger(HtmlCodingComposite.class);

	public static final String ID = "de.fu_berlin.imp.apiua.survey.views.GroupDiscussionCodingView";

	private final IDataService dataService = (IDataService) PlatformUI
			.getWorkbench().getService(IDataService.class);
	private final IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		@Override
		public void dataDirectoriesLoaded(
				List<? extends IBaseDataContainer> dataContainers) {
			GroupDiscussionCodingView.this.load();
		}
	};

	private Composite composite;
	private HtmlCodingComposite htmlCodingComposite;

	public GroupDiscussionCodingView() {
		this.dataService.addDataServiceListener(this.dataServiceListener);
	}

	@Override
	public void createPartControl(Composite parent) {
		this.composite = parent;
		parent.setLayout(new FillLayout());
	}

	@Override
	public void dispose() {
		this.dataService.removeDataServiceListener(this.dataServiceListener);
		super.dispose();
	}

	public Job load() {
		Job job = new NamedJob(CDView.class,
				"Loading Cognitive Dimensions Questionnaire") {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				final SurveyContainer surveyContainer = Activator.getDefault()
						.getSurveyContainer();

				ExecUtils
						.logException(ExecUtils
								.asyncExec(() -> {
									if (GroupDiscussionCodingView.this.htmlCodingComposite != null
											&& !GroupDiscussionCodingView.this.htmlCodingComposite
													.isDisposed()) {
										GroupDiscussionCodingView.this.htmlCodingComposite
												.dispose();
									}

									Collection<GroupDiscussionDocument> groupDiscussions = surveyContainer
											.getGroupDiscussionDocuments();
									if (groupDiscussions.size() > 1) {
										LOGGER.warn("More than one group discussion is currently not supported!");
									}
									if (groupDiscussions.size() == 1) {
										GroupDiscussionDocument gd = groupDiscussions
												.iterator().next();
										GroupDiscussionCodingView.this.htmlCodingComposite = new HtmlCodingComposite(
												GroupDiscussionCodingView.this.composite,
												SWT.NONE, gd.getMarkedUpFile()
														.toURI().toString());
										new ContextMenu(
												GroupDiscussionCodingView.this.htmlCodingComposite,
												GroupDiscussionCodingView.this
														.getSite()) {
											@Override
											protected String getDefaultCommandID() {
												return null;
											}
										};
									}
									GroupDiscussionCodingView.this.composite
											.layout(true, true);
								}));

				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return job;
	}

	public ISelectionProvider getSelectionProvider() {
		return this.htmlCodingComposite != null
				&& !this.htmlCodingComposite.isDisposed() ? this.htmlCodingComposite
				: null;
	}

	@Override
	public void setFocus() {
		if (this.htmlCodingComposite != null
				&& !this.htmlCodingComposite.isDisposed()) {
			this.htmlCodingComposite.setFocus();
		}
	}

}
