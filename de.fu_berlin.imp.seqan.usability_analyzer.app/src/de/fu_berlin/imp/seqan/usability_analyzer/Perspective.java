package de.fu_berlin.imp.seqan.usability_analyzer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView;

public class Perspective implements IPerspectiveFactory {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.app.perspective";

	public void createInitialLayout(IPageLayout layout) {
		// IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
		// 0.25f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView", IPageLayout.LEFT, 0.325f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addView("org.eclipse.ui.views.ResourceNavigator",
				IPageLayout.BOTTOM, 0.5f, "de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView");
		layout.addView("de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogExplorerView", IPageLayout.BOTTOM, 0.5f,
				"org.eclipse.ui.views.ResourceNavigator");

		layout.addView("de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView", IPageLayout.BOTTOM, 0.7f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addView("de.fu_berlin.imp.seqan.usability_analyzer.core.views.TimeZoneDateRangeView", IPageLayout.RIGHT, 0.8f,
				EntityView.ID);
		layout.addView("org.eclipse.ui.views.ProgressView", IPageLayout.BOTTOM,
				0.5f, "de.fu_berlin.imp.seqan.usability_analyzer.core.views.TimeZoneDateRangeView");

		layout.addView("de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogTimelineView", IPageLayout.RIGHT, 0.595f,
				IPageLayout.ID_EDITOR_AREA);
	}
}
