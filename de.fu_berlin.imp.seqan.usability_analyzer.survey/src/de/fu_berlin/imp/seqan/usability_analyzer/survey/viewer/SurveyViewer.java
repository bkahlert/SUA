package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.csv.CSVSurveyRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.model.SurveyRecordItem;

public class SurveyViewer extends SortableTreeViewer {
	private final LocalResourceManager resources;

	public SurveyViewer(final Composite parent, int style) {
		super(parent, style);

		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				SurveyViewer.this.resources.dispose();
			}
		});

		this.initColumns();
	}

	private void initColumns() {

		this.createColumn("Question", new RelativeWidth(.3, 120))
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof CSVSurveyRecord) {
							CSVSurveyRecord cSVSurveyRecord = (CSVSurveyRecord) element;
							IIdentifier identifier = cSVSurveyRecord.getID();
							Token token = cSVSurveyRecord.getToken();
							return IIdentifier.class.getSimpleName()
									+ ": "
									+ ((identifier != null) ? identifier
											.toString() : "-")
									+ ", Token: "
									+ ((token != null) ? token.toString() : "-");
						}
						if (element instanceof SurveyRecordItem) {
							SurveyRecordItem surveyRecordItem = (SurveyRecordItem) element;
							return surveyRecordItem.getKey();
						}
						return "";
					}
				});

		this.createColumn("Answer", new RelativeWidth(.7, 150))
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof SurveyRecordItem) {
							SurveyRecordItem surveyRecordItem = (SurveyRecordItem) element;
							return surveyRecordItem.getValue();
						}
						return "";
					}
				});
	}
}
