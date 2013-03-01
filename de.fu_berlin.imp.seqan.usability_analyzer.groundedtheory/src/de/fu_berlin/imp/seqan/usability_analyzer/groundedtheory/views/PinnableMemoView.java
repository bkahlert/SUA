package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class PinnableMemoView extends MemoView {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.PinnableMemoView";

	private ISelection lastSelection;
	private boolean pin = false;

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			lastSelection = selection;

			if (!pin) {
				load(selection);
			}
		}
	};

	public PinnableMemoView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		SelectionUtils.getSelectionService(getSite().getWorkbenchWindow())
				.addPostSelectionListener(selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService(getSite().getWorkbenchWindow())
				.removePostSelectionListener(selectionListener);
		super.dispose();
	}

	public void load(ISelection selection) {
		final List<ICode> codes = SelectionUtils.getAdaptableObjects(selection,
				ICode.class);
		final List<ICodeInstance> codeInstances = SelectionUtils
				.getAdaptableObjects(selection, ICodeInstance.class);
		final List<ICodeable> codeables = SelectionUtils.getAdaptableObjects(
				selection, ICodeable.class);
		final List<Object> objects = SelectionUtils.getAdaptableObjects(
				selection, Object.class);

		if (codes.size() > 0)
			PinnableMemoView.this.load(codes.get(0));
		else if (codeInstances.size() > 0)
			PinnableMemoView.this.load(codeInstances.get(0));
		else if (codeables.size() > 0)
			PinnableMemoView.this.load(codeables.get(0));
		else if (objects.size() > 0)
			PinnableMemoView.this.lock();
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
		if (!pin)
			load(this.lastSelection);
	}

}
