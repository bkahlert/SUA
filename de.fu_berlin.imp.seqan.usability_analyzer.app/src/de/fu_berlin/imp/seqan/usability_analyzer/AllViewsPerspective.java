package de.fu_berlin.imp.seqan.usability_analyzer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class AllViewsPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		@SuppressWarnings("unused")
		String editorArea = layout.getEditorArea();
		this.addFastViews(layout);
		this.addViewShortcuts(layout);
		this.addPerspectiveShortcuts(layout);

		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffView",
				IPageLayout.LEFT, 0.32f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogView",
				IPageLayout.BOTTOM, 0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView",
				IPageLayout.BOTTOM, 0.69f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.core.views.TimeZoneDateRangeView",
				IPageLayout.RIGHT, 0.8f,
				"de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView");
		layout.addView("org.eclipse.ui.views.ProgressView", IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.core.views.TimeZoneDateRangeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView",
				IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView",
				IPageLayout.RIGHT, 0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.survey.views.SurveyView",
				IPageLayout.TOP, 0.43f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.uri.views.UriView",
				IPageLayout.BOTTOM, 0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.PinnableMemoView",
				IPageLayout.BOTTOM, 0.38f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeInstancesView",
				IPageLayout.BOTTOM,
				0.43f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.PinnableMemoView");
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
	}

}
