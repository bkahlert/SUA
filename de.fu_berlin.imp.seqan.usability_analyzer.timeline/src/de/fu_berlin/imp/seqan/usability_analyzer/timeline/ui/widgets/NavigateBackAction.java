package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.information.InformationControl;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public class NavigateBackAction extends NavigateAction {
	private final InformationPresentingTimeline informationPresentingTimeline;
	private final Object element;

	public NavigateBackAction(
			InformationPresentingTimeline informationPresentingTimeline,
			InformationControl<?> informationControl, Object element) {
		super(informationPresentingTimeline, informationControl);
		this.informationPresentingTimeline = informationPresentingTimeline;
		this.element = element;
	}

	@Override
	public String getText() {
		return "Back";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(PlatformUI.getWorkbench()
				.getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
	}

	@Override
	public void run() {
		Object predecessor = this.informationPresentingTimeline
				.getPredecessor(this.element);
		if (predecessor instanceof ILocatable) {
			this.navigateTo((ILocatable) predecessor);
		}
	}
}