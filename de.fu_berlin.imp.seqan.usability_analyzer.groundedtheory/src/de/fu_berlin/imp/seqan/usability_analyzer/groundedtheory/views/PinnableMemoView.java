package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

public class PinnableMemoView extends AbstractMemoView {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.PinnableMemoView";

	private ISelection lastSelection;
	private boolean pin = false;

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == PinnableMemoView.this) {
				return;
			}
			PinnableMemoView.this.lastSelection = selection;
			if (!PinnableMemoView.this.pin) {
				PinnableMemoView.this.load(selection);
			}
		}
	};

	public PinnableMemoView() {

	}

	@Override
	public void postInit() {
		super.postInit();
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.addPostSelectionListener(this.selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.removePostSelectionListener(this.selectionListener);
		super.dispose();
	}

	public void load(ISelection selection) {
		final List<URI> uris = SelectionUtils.getAdaptableObjects(selection,
				URI.class);

		PinnableMemoView.this.loadAndClearHistory(uris.toArray(new URI[0]));
	}

	/**
	 * Defines if the currently loaded memo should be pinned.
	 * <p>
	 * A pinned memo stays open although another object with memo support is
	 * selected.
	 * 
	 * @param pin
	 */
	public void setPin(boolean pin) {
		this.pin = pin;
		if (!pin) {
			this.load(this.lastSelection);
		}
	}

}
