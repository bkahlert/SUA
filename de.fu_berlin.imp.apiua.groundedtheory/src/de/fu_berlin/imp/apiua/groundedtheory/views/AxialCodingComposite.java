package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IModifiable;
import com.bkahlert.nebula.utils.IReflexiveConverter;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IDNDListener;
import com.bkahlert.nebula.widgets.browser.listener.IMouseListener;
import com.bkahlert.nebula.widgets.browser.listener.MouseAdapter;
import com.bkahlert.nebula.widgets.jointjs.JointJS;
import com.bkahlert.nebula.widgets.jointjs.JointJS.JointJSListener;
import com.bkahlert.nebula.widgets.jointjs.JointJSCell;
import com.bkahlert.nebula.widgets.jointjs.JointJSModel;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.apiua.core.services.IImportanceServiceListener;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.AxialCodingLabelProvider;

/**
 * Can load, edit and save a {@link IAxialCodingModel}.
 *
 * @author bkahlert
 *
 */
public class AxialCodingComposite extends Composite implements
		ISelectionProvider, IModifiable {

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingComposite.class);

	private static final IImportanceService IMPORTANCE_SERVICE = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);
	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private static final IUriPresenterService PRESENTER_SERVICE = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	private final IImportanceServiceListener importanceServiceListener = new IImportanceServiceListener() {
		@Override
		public void importanceChanged(Set<URI> uris, Importance importance) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}
	};

	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void codeMoved(final ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void codeDeleted(ICode code) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void relationsAdded(java.util.Set<IRelation> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void relationsRenamed(java.util.Set<IRelation> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void relationsDeleted(java.util.Set<IRelation> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};
	};

	private final List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

	private JointJS jointjs = null;
	private final AxialCodingLabelProvider labelProvider = new AxialCodingLabelProvider();

	private URI openedUri = null;

	public AxialCodingComposite(Composite parent, int style) {
		super(parent, style);
		IMPORTANCE_SERVICE
				.addImportanceServiceListener(this.importanceServiceListener);
		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
		this.setLayout(new FillLayout());

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				CODE_SERVICE
						.removeCodeServiceListener(AxialCodingComposite.this.codeServiceListener);
				IMPORTANCE_SERVICE
						.removeImportanceServiceListener(AxialCodingComposite.this.importanceServiceListener);
			}
		});

		this.jointjs = new JointJS(this, SWT.BORDER, "apiua://code/",
				"apiua://relation/", new IReflexiveConverter<String, Object>() {
					@Override
					public URI convert(String returnValue) {
						// default selection
						if (returnValue == null) {
							return AxialCodingComposite.this.openedUri;
						}
						if (returnValue.contains("|")) {
							return null;
						}
						if (returnValue.startsWith("apiua://")) {
							return new URI(returnValue);
						}
						return null;
					}

					@Override
					public String convertBack(Object object) {
						return object.toString();
					}
				});
		this.jointjs.addJointJSListener(new JointJSListener() {

			@Override
			public void modified(JointJSModel model) {
				Event event = new Event();
				event.display = Display.getCurrent();
				event.widget = AxialCodingComposite.this;
				event.text = model.serialize();
				event.data = model;
				ModifyEvent modifyEvent = new ModifyEvent(event);
				for (ModifyListener modifyListener : AxialCodingComposite.this.modifyListeners) {
					modifyListener.modifyText(modifyEvent);
				}
			}
		});

		this.jointjs
				.injectCss(".html-element.invalid {"
						+ "	background-image: linear-gradient(-45deg,"
						+ "		rgba(255, 255, 255, .2) 25%,"
						+ "		rgba(255, 255, 255, .85) 25%,"
						+ "		rgba(255, 255, 255, .85) 50%,"
						+ "		rgba(255, 255, 255, .2) 50%,"
						+ "		rgba(255, 255, 255, .2) 75%,"
						+ "		rgba(255, 255, 255, .85) 75%,"
						+ "		rgba(255, 255, 255, .85));"
						+ "	background-size: 55px 55px;"
						+ "}"
						+ ".link.invalid { .connection { stroke: rgba(0,0,0,.2); stroke-dasharray: 27,27; } }");
		// this.jointjs
		// .injectCss("[droppable].over rect { stroke:black; stroke-width: 4px; stroke-dasharray:5,5;");
		this.jointjs.setEnabled(false);

		this.activateDropSupport();
		this.activateInformationSupport();
	}

	private void activateInformationSupport() {
		PRESENTER_SERVICE.enable(this.jointjs,
				new ISubjectInformationProvider<Control, URI>() {
					private URI hovered = null;
					private boolean isMouseDown = false;

					private final JointJS.IJointJSListener jointJsListener = new JointJS.JointJSListener() {

						@Override
						public void hovered(JointJSCell cell, boolean hoveredIn) {
							if (hoveredIn && cell != null
									&& cell.getId() != null
									&& !cell.getId().contains("|")
									&& cell.getId().startsWith("apiua://")) {
								hovered = new URI(cell.getId());
							} else {
								hovered = null;
							}
						}
					};

					private final IMouseListener mouseListener = new MouseAdapter() {
						@Override
						public void mouseDown(double x, double y,
								IElement element) {
							isMouseDown = true;
						}

						@Override
						public void mouseUp(double x, double y, IElement element) {
							isMouseDown = false;
						}
					};

					@Override
					public void register(Control subject) {
						AxialCodingComposite.this.jointjs
								.addJointJSListener(this.jointJsListener);
						AxialCodingComposite.this.jointjs
								.addMouseListener(this.mouseListener);
						this.hovered = null;
						this.isMouseDown = false;
					}

					@Override
					public void unregister(Control subject) {
						AxialCodingComposite.this.jointjs
								.removeMouseListener(this.mouseListener);
						AxialCodingComposite.this.jointjs
								.removeJointJSListener(this.jointJsListener);
						this.hovered = null;
						this.isMouseDown = false;
					}

					@Override
					public Point getHoverArea() {
						return new Point(10, 10);
					}

					@Override
					public URI getInformation() {
						return !this.isMouseDown ? this.hovered : null;
					}
				});

		this.jointjs.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				PRESENTER_SERVICE.disable(AxialCodingComposite.this.jointjs);
			}
		});
	}

	private void activateDropSupport() {
		this.jointjs.run("$('.jointjs svg').attr('droppable', true)");
		this.jointjs.addDNDListener(new IDNDListener() {
			@Override
			public void dragStart(long offsetX, long offsetY, IElement element,
					String mimeType, String data) {
			}

			@Override
			public void drop(final long offsetX, final long offsetY,
					final IElement element, String mimeType, final String data) {
				if (data == null || data.isEmpty()
						|| AxialCodingComposite.this.openedUri == null) {
					return;
				}

				ExecUtils.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							if (element.getAttribute("model-id") != null) {
								// already there
								List<URI> nodes = AxialCodingComposite.this
										.getNodes().get();
								for (final String uriString : data.split("\\|")) {
									URI uri = new URI(uriString);
									if (LocatorService.INSTANCE.resolve(uri,
											null).get() != null
											&& !nodes.contains(uri)) {
										AxialCodingComposite.this.replaceNode(
												new URI(
														element.getAttribute("model-id")),
												uri);
										break; // only replace using the first
										// element
									}
								}
							} else {
								// new node
								Point pan = AxialCodingComposite.this.jointjs
										.getPan().get();
								Double zoom = AxialCodingComposite.this.jointjs
										.getZoom().get();
								int dropX = (int) ((offsetX / zoom) - pan.x);
								int dropY = (int) ((offsetY / zoom) - pan.y);
								for (final String uriString : data.split("\\|")) {
									URI uri = new URI(uriString);
									Point size = AxialCodingComposite.this.labelProvider
											.getSize(uri);
									if (size != null) {
										dropX -= size.x / 2;
										dropY -= size.y / 2;
									}
									if (LocatorService.INSTANCE.resolve(uri,
											null).get() != null) {
										AxialCodingComposite.this.createNode(
												uri, new Point(dropX, dropY));
										AxialCodingComposite.this.jointjs
												.run("$('.jointjs svg .element').attr('droppable', true)");
									}
								}
							}
						} catch (Exception e) {
							LOGGER.error("Error dropping " + data, e);
						}
					}
				});

			}
		});
	}

	public Future<Void> open(final URI uri) {
		Assert.isNotNull(uri);
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					IAxialCodingModel axialCodingModel = CODE_SERVICE
							.getAxialCodingModel(uri);
					if (axialCodingModel == null) {
						return AxialCodingComposite.this.open(null).get();
					}
					AxialCodingComposite.this.openedUri = uri;
					new SUAGTPreferenceUtil()
							.setLastOpenedAxialCodingModels(Arrays.asList(uri));
					AxialCodingComposite.this.jointjs.load(
							axialCodingModel.serialize()).get();
					AxialCodingComposite.this.refresh().get();
					AxialCodingComposite.this.jointjs
							.run("$('.jointjs svg .element').attr('droppable', true)");
					AxialCodingComposite.this.jointjs.setEnabled(true);
				} catch (CodeStoreReadException e) {
					throw new IllegalArgumentException(e);
				} catch (Exception e) {
					LOGGER.error("Error refreshing the axial coding model "
							+ uri);
				}
				return null;
			}
		});
	}

	/**
	 * Saves the currently opened {@link URI}.
	 *
	 * @return
	 */
	public void save() {
		if (this.openedUri == null) {
			return;
		}

		final URI uri = this.openedUri;
		final IAxialCodingModel axialCodingModel = new JointJSAxialCodingModel(
				uri, this.jointjs.getModel());
		try {
			CODE_SERVICE.addAxialCodingModel(axialCodingModel);
		} catch (CodeStoreWriteException e) {
			LOGGER.error("Error saving "
					+ IAxialCodingModel.class.getSimpleName() + " " + uri);
		}
	}

	public URI getOpenedURI() {
		return this.openedUri;
	}

	public Future<List<URI>> getNodes() {
		return ExecUtils.nonUIAsyncExec(new Callable<List<URI>>() {
			@Override
			public List<URI> call() throws Exception {
				List<URI> uris = new LinkedList<URI>();
				for (String id : AxialCodingComposite.this.jointjs.getNodes()
						.get()) {
					uris.add(new URI(id));
				}
				return uris;
			}
		});
	}

	public void setTitle(String title) {
		ExecUtils.logException(this.jointjs.setTitle(title));
	}

	public Future<Void> highlight(List<URI> uris) {
		List<String> ids = new LinkedList<String>();
		if (ids != null) {
			for (URI uri : uris) {
				ids.add(uri.toString());
			}
		}
		return this.jointjs.highlight(ids);
	}

	public void createNode(URI uri, Point position) throws Exception {
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

		this.refresh();
	}

	private void replaceNode(URI oldUri, URI newUri) throws Exception {
		this.deleteIsALinks(oldUri).get();
		String updatedJson = this.jointjs.save().get()
				.replace(oldUri.toString(), newUri.toString());
		this.jointjs.load(updatedJson).get();
		this.refresh().get();
	}

	/**
	 * Creates in and outgoing "is a" permanent links for the given {@link URI}
	 * without touching any existing links.
	 *
	 * @param uri
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Future<Void> createIsALinks(final URI uri)
			throws InterruptedException, ExecutionException {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<URI> existingNodes = AxialCodingComposite.this.getNodes()
						.get();
				ICode code = LocatorService.INSTANCE.resolve(uri, ICode.class,
						null).get();
				if (code == null) {
					LOGGER.warn(uri + " is no valid code");
					return null;
				}
				ICode parent = code;
				while (true) {
					parent = CODE_SERVICE.getParent(parent);
					if (parent == null) {
						break;
					}
					if (existingNodes.contains(parent.getUri())) {
						AxialCodingComposite.this.createIsALink(
								parent.getUri(), code.getUri()).get();
					}
				}
				for (ICode child : CODE_SERVICE.getSubCodes(code)) {
					if (existingNodes.contains(child.getUri())) {
						AxialCodingComposite.this.createIsALink(code.getUri(),
								child.getUri()).get();
					}
				}
				return null;
			}
		});
	}

	/**
	 * Creates a "is a" permanent link between the given parent and child node.
	 *
	 * @param parent
	 * @param child
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Future<Void> createIsALink(final URI parent, final URI child)
			throws InterruptedException, ExecutionException {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				LOGGER.info("Creating Is-A-Link from "
						+ LocatorService.INSTANCE
								.resolve(child, ICode.class, null).get()
								.getCaption()
						+ " to "
						+ LocatorService.INSTANCE
								.resolve(parent, ICode.class, null).get()
								.getCaption());
				String id = parent.toString() + "|" + child.toString();

				id = AxialCodingComposite.this.jointjs.createPermanentLink(id,
						parent.toString(), child.toString()).get();

				String[] texts = new String[] { "is a" };
				for (int i = 0; texts != null && i < texts.length; i++) {
					AxialCodingComposite.this.jointjs.setText(id, i, texts[i]);
				}
				return null;
			}
		});
	}

	/**
	 * Creates a custom {@link IRelation}.
	 *
	 * @param from
	 * @param to
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public Future<Void> createLink(IRelation relation)
			throws InterruptedException, ExecutionException {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				LOGGER.info("Creating relation " + relation);
				String id = AxialCodingComposite.this.jointjs.createLink(
						relation.getUri().toString(),
						relation.getFrom().toString(),
						relation.getTo().toString()).get();

				String[] texts = new String[] { relation.getName() };
				for (int i = 0; texts != null && i < texts.length; i++) {
					AxialCodingComposite.this.jointjs.setText(id, i, texts[i]);
				}
				return null;
			}
		});
	}

	/**
	 * Removes all incoming and outgoing "is a" permanent links of the specified
	 * {@link URI}.
	 *
	 * @return
	 */
	private Future<Void> deleteIsALinks(final URI uri) {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<String> linkIds = AxialCodingComposite.this.jointjs
						.getConnectedPermanentLinks(uri.toString()).get();
				for (String linkId : linkIds) {
					AxialCodingComposite.this.jointjs.remove(linkId).get();
				}
				return null;
			}
		});
	}

	public Future<Void> refresh() {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<URI> validUris = AxialCodingComposite.this.syncModel();
				AxialCodingComposite.this.updateLabels(validUris);
				AxialCodingComposite.this.refreshLinks();
				return null;
			}
		});
	}

	/**
	 * Synchronized structural information with the internally saved
	 * information.
	 * <p>
	 * <ul>
	 * <li>No more existing {@link ICode} get removed.</li>
	 * <li>Permanent "is a" links are recreated.
	 *
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private List<URI> syncModel() throws InterruptedException,
			ExecutionException {
		List<URI> validUris = this.markAllInvalidNodes();
		this.deleteAllOutdatedIsALinksBetweenValidNodes();
		for (URI uri : validUris) {
			this.createIsALinks(uri);
		}
		return validUris;
	}

	/**
	 * Removes all outdated "is a" permanent links from the graph.
	 *
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private void deleteAllOutdatedIsALinksBetweenValidNodes()
			throws InterruptedException, ExecutionException {
		List<String> linkIds = this.jointjs.getPermanentLinks().get();
		for (String linkId : linkIds) {
			URI parentURI = new URI(linkId.split("\\|")[0]);
			URI subURI = new URI(linkId.split("\\|")[1]);
			ICode parentCode = LocatorService.INSTANCE.resolve(parentURI,
					ICode.class, null).get();
			ICode subCode = LocatorService.INSTANCE.resolve(subURI,
					ICode.class, null).get();
			if (parentCode != null && subCode != null
					&& !CODE_SERVICE.getSubCodes(parentCode).contains(subCode)) {
				this.jointjs.remove(linkId).get();
			}
		}
	}

	/**
	 * Removes all nodes that symbolize a no more existing {@link ICode}.
	 *
	 * @return the nodes that are kept
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private List<URI> markAllInvalidNodes() throws InterruptedException,
			ExecutionException {
		List<URI> uris = this.getNodes().get();
		List<String> validIds = new ArrayList<String>();
		List<String> invalidIds = new ArrayList<String>();
		for (Iterator<URI> iterator = uris.iterator(); iterator.hasNext();) {
			URI uri = iterator.next();
			ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null)
					.get();
			if (locatable == null) {
				invalidIds.add(uri.toString());
				iterator.remove();
			} else {
				validIds.add(uri.toString());
			}
		}
		this.jointjs.addCustomClass(invalidIds, "invalid");
		this.jointjs.removeCustomClass(validIds, "invalid");
		return uris;
	}

	/**
	 * Updates non-structural information all existing nodes based on the
	 * internal information and returns the new size.
	 * <p>
	 * Updated information are:
	 * <ul>
	 * <li>title</li>
	 * <li>content</li>
	 * <li>colors</li>
	 * <li>size</li>
	 * </ul>
	 *
	 * @param uris
	 * @return
	 * @throws Exception
	 */
	public void updateLabels(List<URI> uris) throws Exception {
		for (URI uri : uris) {
			this.refreshNode(uri);
		}
	}

	/**
	 * Updates non-structural information of the given {@link URI} based on the
	 * internal information and returns the new size.
	 * <p>
	 * Updated information are:
	 * <ul>
	 * <li>title</li>
	 * <li>content</li>
	 * <li>colors</li>
	 * <li>size</li>
	 * </ul>
	 *
	 * @param uri
	 */
	private void refreshNode(final URI uri) throws Exception {
		if (!this.getNodes().get().contains(uri)) {
			return;
		}
		this.jointjs.setElementTitle(uri.toString(),
				this.labelProvider.getText(uri));
		this.jointjs.setElementContent(uri.toString(),
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
	}

	private void refreshLinks() throws Exception {
		List<String> linkIds = this.jointjs.getLinks().get();
		for (String linkId : linkIds) {
			if (linkId.contains("|")) {
				continue;
			}
			IRelation relation = CODE_SERVICE.getRelation(new URI(linkId));
			if (relation == null) {
				this.jointjs.addCustomClass(Arrays.asList(linkId), "invalid");
				// this.jointjs.remove(linkId);
			} else {
				this.jointjs.setLinkTitle(linkId, relation.getName());
			}
		}

		List<String> elementIds = this.jointjs.getNodes().get();
		for (IRelation relation : CODE_SERVICE.getRelations()) {
			if (elementIds.contains(relation.getFrom().toString())
					&& elementIds.contains(relation.getTo().toString())) {
				this.createLink(relation);
			}
		}
	}

	@Override
	public boolean setFocus() {
		return this.jointjs.setFocus();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			this.jointjs.addSelectionChangedListener(listener);
		}
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			this.jointjs.removeSelectionChangedListener(listener);
		}
	}

	@Override
	public ISelection getSelection() {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			return this.jointjs.getSelection();
		}
		return new StructuredSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			this.jointjs.setSelection(selection);
		}
	}

	public JointJS getJointjs() {
		return this.jointjs;
	}

	/**
	 * Removes the given {@link ICode}s from the currently loaded
	 * {@link IAxialCodingModel}.
	 *
	 * @param uris
	 */
	public void remove(List<URI> uris) {
		for (URI uri : uris) {
			this.jointjs.remove(uri.toString());
		}
	}

	public void autoLayout() {
		this.jointjs.autoLayout();
	}

	public void zoomOut() {
		this.jointjs.zoomOut();
	}

	public void zoomIn() {
		this.jointjs.zoomIn();
	}

	public void fitOnScreen() {
		this.jointjs.fitOnScreen();
	}

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.add(modifyListener);
	}

	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.remove(modifyListener);
	}

}
