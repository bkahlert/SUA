package de.fu_berlin.imp.seqan.usability_analyzer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TimelinePerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	@SuppressWarnings("unused")
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		String editorArea = layout.getEditorArea();
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView",
				IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView",
				IPageLayout.BOTTOM, 0.67f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeInstancesView",
				IPageLayout.RIGHT, 0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.survey.views.SurveyView",
				IPageLayout.TOP, 0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.MemoView",
				IPageLayout.TOP,
				0.66f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeInstancesView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView",
				IPageLayout.TOP, 0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.MemoView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.core.views.TimeZoneDateRangeView",
				IPageLayout.RIGHT, 0.6f,
				"de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView");
		layout.addView("org.eclipse.ui.views.ProgressView", IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.core.views.TimeZoneDateRangeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView",
				IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA);
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut("de.fu_berlin.imp.seqan.usability_analyzer.OpenCodingPerspective");
		layout.addPerspectiveShortcut("de.fu_berlin.imp.seqan.usability_analyzer.OpenCodingPerspective");
	}

}
