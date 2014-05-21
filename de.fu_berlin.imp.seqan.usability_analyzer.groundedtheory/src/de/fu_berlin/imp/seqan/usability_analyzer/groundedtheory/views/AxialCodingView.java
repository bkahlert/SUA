package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.browser.listener.IDropListener;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.LocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.AxialCodingLabelProvider;

public class AxialCodingView extends ViewPart {

	static final Logger LOGGER = Logger.getLogger(AxialCodingView.class);

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AxialCodingView";

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private JointJS jointjs = null;
	private final AxialCodingLabelProvider labelProvider = new AxialCodingLabelProvider();

	private URI openedUri = null;

	public AxialCodingView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		ItemList modelList = new AxialCodingViewModelList(parent, SWT.BORDER);
		modelList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.jointjs = new JointJS(parent, SWT.NONE, "sua://code/",
				"sua://code-link");
		this.jointjs
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.jointjs.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				AxialCodingView.this.save();
			}
		});
		this.activateDropSupport();
	}

	private void activateDropSupport() {
		this.jointjs.addDropListener(new IDropListener() {
			@Override
			public void drop(long offsetX, long offsetY, final String data) {
				if (data == null || data.isEmpty()
						|| AxialCodingView.this.openedUri == null) {
					return;
				}

				for (final String uriString : data.split("\\|")) {
					ExecUtils.nonUIAsyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								System.err.println(AxialCodingView.this.jointjs
										.getPan().get());
								xx TODO pan korrekt messen
								// TODO: letzte Woche abarbeiten
								// TODO: testen
								URI uri = new URI(uriString);
								AxialCodingView.this.createNode(uri, new Point(
										10, 10));
							} catch (Exception e) {
								LOGGER.error("Error dropping " + data, e);
							}
						}
					});

				}
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
			this.openedUri = uri;
			this.jointjs.load(axialCodingModel.serialize());
		} catch (CodeStoreReadException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Saves the currently opened {@link URI}
	 */
	public void save() {
		if (this.openedUri == null) {
			return;
		}

		final URI uri = this.openedUri;
		final Future<String> json = this.jointjs.save();
		ExecUtils.nonUIAsyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IAxialCodingModel axialCodingModel = new JointJSAxialCodingModel(
							uri, json.get());
					CODE_SERVICE.addAxialCodingModel(axialCodingModel);
				} catch (Exception e) {
					LOGGER.error("Error saving " + uri);
				}
			}
		});
	}

	public Object getOpenedURI() {
		return this.openedUri;
	}

	public Future<List<URI>> getModelCodes() {
		return ExecUtils.nonUIAsyncExec(new Callable<List<URI>>() {
			@Override
			public List<URI> call() throws Exception {
				List<URI> uris = new LinkedList<URI>();
				for (String id : AxialCodingView.this.jointjs.getNodes().get()) {
					uris.add(new URI(id));
				}
				return uris;
			}
		});
	}

	public void refresh() throws Exception {
		for (URI uri : this.getModelCodes().get()) {
			this.refresh(uri);
		}
	}

	/**
	 * Refreshed the given Node based on the internal information and returns
	 * the new size.
	 * 
	 * @param uri
	 * @return
	 */
	public Point refresh(URI uri) throws Exception {
		this.jointjs.setNodeTitle(uri.toString(),
				this.labelProvider.getText(uri));
		this.jointjs.setNodeContent(uri.toString(),
				this.labelProvider.getContent(uri));
		this.jointjs.setColor(uri.toString(), this.labelProvider.getColor(uri));
		this.jointjs.setBackgroundColor(uri.toString(),
				this.labelProvider.getBackgroundColor(uri));
		this.jointjs.setBorderColor(uri.toString(),
				this.labelProvider.getBorderColor(uri));

		Point size = this.labelProvider.getSize(uri);
		if (size != null) {
			this.jointjs.setSize(uri.toString(), size.x, size.y);
		}
		return size;
	}

	private void createNode(URI uri, Point position) throws Exception {
		String title = this.labelProvider.getText(uri);
		String content = this.labelProvider.getContent(uri);
		Point size = this.labelProvider.getSize(uri);

		if (position == null) {
			position = new Point(10, 10);
		}

		String id = null;
		try {
			id = this.jointjs.createNode(uri.toString(), title, content,
					position, size).get();
		} catch (Exception e) {
			LOGGER.error("Error creating node " + id, e);
		}

		if (id == null) {
			LOGGER.error("ID missing for created/updated node!");
		}

		List<URI> existingCodes = this.getModelCodes().get();
		ICode code = LocatorService.INSTANCE.resolve(uri, ICode.class, null)
				.get();
		ICode parent = CODE_SERVICE.getParent(code);
		if (parent != null && existingCodes.contains(parent.getUri())) {
			this.createLink(parent.getUri(), code.getUri());
		}
		for (ICode child : CODE_SERVICE.getChildren(code)) {
			if (existingCodes.contains(child.getUri())) {
				this.createLink(code.getUri(), child.getUri());
			}
		}

		this.refresh(uri);
	}

	private void createLink(URI parent, URI child) throws InterruptedException,
			ExecutionException {
		String id = parent.toString() + "|" + parent.toString();

		id = this.jointjs.createPermanentLink(id, parent.toString(),
				child.toString()).get();

		String[] texts = new String[] { "is a" };
		for (int i = 0; texts != null && i < texts.length; i++) {
			this.jointjs.setText(id, i, texts[i]);
		}
	}

	@Override
	public void setFocus() {
		this.jointjs.setFocus();
	}

}
