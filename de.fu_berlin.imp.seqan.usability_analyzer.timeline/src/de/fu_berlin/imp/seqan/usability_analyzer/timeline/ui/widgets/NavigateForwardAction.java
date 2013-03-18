package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.information.InformationControl;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public class NavigateForwardAction extends NavigateAction {
	private final InformationPresentingTimeline informationPresentingTimeline;
	private final Object element;

	public NavigateForwardAction(
			InformationPresentingTimeline informationPresentingTimeline,
			InformationControl<?> informationControl, Object element) {
		super(informationPresentingTimeline, informationControl);
		this.informationPresentingTimeline = informationPresentingTimeline;
		this.element = element;
	}

	@Override
	public String getText() {
		return "Forward";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(PlatformUI.getWorkbench()
				.getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
	}

	@Override
	public void run() {
		Object successor = this.informationPresentingTimeline
				.getSuccessor(this.element);
		if (successor instanceof ILocatable) {
			this.navigateTo((ILocatable) successor);
		}
	}
}