package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.viewer.jointjs.JointJSContentProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSLabelProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSViewer;
import com.bkahlert.nebula.widgets.browser.listener.IDropListener;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.AxialCodingContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.AxialCodingLabelProvider;

public class AxialCodingView extends ViewPart {

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingView.class);

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AxialCodingView";

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private JointJS jointjs = null;
	private JointJSViewer jointjsViewer = null;

	private URI openedUri = null;

	public AxialCodingView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		this.jointjs = new JointJS(parent, SWT.NONE, "sua://code/",
				"sua://code-link");
		JointJSContentProvider contentProvider = new AxialCodingContentProvider();
		JointJSLabelProvider labelProvider = new AxialCodingLabelProvider();
		this.jointjsViewer = new JointJSViewer(this.jointjs, contentProvider,
				labelProvider);

		this.jointjsViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
		this.jointjsViewer.refresh();

		this.activateDropSupport();
	}

	private void activateDropSupport() {
		this.jointjs.addDropListener(new IDropListener() {
			@Override
			public void drop(long offsetX, long offsetY, String data) {
				if (data == null || data.isEmpty()) {
					return;
				}

				List<URI> uris = new LinkedList<URI>();
				for (String uri : data.split("\\|")) {
					try {
						uris.add(new URI(uri));
					} catch (Exception e) {
						LOGGER.error("Error dropping " + uri);
					}
				}
				System.err.println(uris);
			}
		});
	}

	public JointJS getJointjs() {
		return this.jointjs;
	}

	public void open(URI uri) {
		try {
			IAxialCodingModel axialCodingModel = CODE_SERVICE
					.getAxialCodingModel(uri);
			this.jointjsViewer.setInput(axialCodingModel);
			this.openedUri = uri;
		} catch (CodeStoreReadException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Object getOpenedURI() {
		return this.openedUri;
	}

	@Override
	public void setFocus() {
		this.jointjs.setFocus();
	}

}
