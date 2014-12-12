package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
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
import com.bkahlert.nebula.utils.Stylers;
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
import de.fu_berlin.imp.apiua.core.services.IImportanceServiceListener;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
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

	private final IImportanceServiceListener importanceServiceListener = (uris,
			importance) -> ExecUtils.logException(AxialCodingComposite.this
			.refresh());

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

		this.addDisposeListener(e -> {
			CODE_SERVICE
					.removeCodeServiceListener(AxialCodingComposite.this.codeServiceListener);
			IMPORTANCE_SERVICE
					.removeImportanceServiceListener(AxialCodingComposite.this.importanceServiceListener);
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

			@Override
			public void doubleClicked(JointJSCell cell) {
				if (LocatorService.INSTANCE != null) {
					LocatorService.INSTANCE.showInWorkspace(
							new URI(cell.getId()), false, null);
				} else {
					LOGGER.error("Could not retrieve "
							+ ILocatorService.class.getSimpleName());
				}
			}
		});

		String originColor = new RGB(Stylers.IMPORTANCE_HIGH_COLOR.getRGB())
				.toDecString();

		this.jointjs.injectCss(
		// invalid elements / relations
				".html-element.invalid {"
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
						+ ".link.invalid .connection { stroke: rgba(0,0,0,.2); stroke-dasharray: 27,27; }"

						// highlight origin element / relation
						+ ".html-element.origin { border-color: "
						+ originColor
						+ " !important; border-width: 5px; }"
						+ ".html-element.origin h1 { color: "
						+ originColor
						+ "; text-transform: uppercase; font-weight: 700; }"
						+ ".link.origin .connection { stroke-width: 5px; stroke: "
						+ originColor
						+ "; }"
						+ ".link.origin .labels text { fill: "
						+ originColor
						+ "; text-transform: uppercase; }"

						// num groundings
						+ ".link .labels tspan+tspan { stroke: none; text-transform: none; fill: "
						+ new RGB(Stylers.COUNTER_COLOR.getRGB()).toDecString()
						+ "; }");
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

		this.jointjs.addDisposeListener(e -> PRESENTER_SERVICE
				.disable(AxialCodingComposite.this.jointjs));
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

				ExecUtils.nonUIAsyncExec(() -> {
					try {
						if (element.getAttribute("model-id") != null) {
							// already there
							List<URI> elements = AxialCodingComposite.this
									.getElements().get();
							for (final String uriString1 : data.split("\\|")) {
								URI uri1 = new URI(uriString1);
								if (LocatorService.INSTANCE.resolve(uri1, null)
										.get() != null
										&& !elements.contains(uri1)) {
									AxialCodingComposite.this.replaceElement(
											new URI(element
													.getAttribute("model-id")),
											uri1);
									break; // only replace using the first
									// element
								}
							}
						} else {
							// new element
							Point pan = AxialCodingComposite.this.jointjs
									.getPan().get();
							Double zoom = AxialCodingComposite.this.jointjs
									.getZoom().get();
							int dropX = (int) ((offsetX / zoom) - pan.x);
							int dropY = (int) ((offsetY / zoom) - pan.y);
							for (final String uriString2 : data.split("\\|")) {
								URI uri2 = new URI(uriString2);
								Point size = AxialCodingComposite.this.labelProvider
										.getSize(uri2);
								if (size != null) {
									dropX -= size.x / 2;
									dropY -= size.y / 2;
								}
								if (LocatorService.INSTANCE.resolve(uri2, null)
										.get() != null) {
									AxialCodingComposite.this.createElement(
											uri2, new Point(dropX, dropY));
									AxialCodingComposite.this.jointjs
											.run("$('.jointjs svg .element').attr('droppable', true)");
								}
							}
						}
					} catch (Exception e) {
						LOGGER.error("Error dropping " + data, e);
					}
				});

			}
		});
	}

	public Future<Void> open(final URI uri) {
		Assert.isNotNull(uri);
		return ExecUtils
				.nonUIAsyncExec((Callable<Void>) () -> {
					try {
						IAxialCodingModel axialCodingModel = CODE_SERVICE
								.getAxialCodingModel(uri);
						if (axialCodingModel == null) {
							return AxialCodingComposite.this.open(null).get();
						}
						AxialCodingComposite.this.openedUri = uri;
						new SUAGTPreferenceUtil()
								.setLastOpenedAxialCodingModels(Arrays
										.asList(uri));
						AxialCodingComposite.this.jointjs.load(
								axialCodingModel.serialize()).get();
						AxialCodingComposite.this.refresh().get();
						AxialCodingComposite.this.jointjs
								.run("$('.jointjs svg .element').attr('droppable', true)");
						AxialCodingComposite.this.jointjs.setEnabled(true);
					} catch (CodeStoreReadException e1) {
						throw new IllegalArgumentException(e1);
					} catch (Exception e2) {
						LOGGER.error("Error refreshing the axial coding model "
								+ uri);
					}
					return null;
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

	public Set<URI> getOriginCells() {
		Set<URI> uris = new HashSet<>();
		try {
			IAxialCodingModel acm = CODE_SERVICE
					.getAxialCodingModel(this.openedUri);
			if (acm.getOrigin() != null) {
				uris.add(acm.getOrigin());
				ILocatable locatable = LocatorService.INSTANCE.resolve(
						acm.getOrigin(), null).get();
				if (locatable instanceof IRelation) {
					uris.add(((IRelation) locatable).getFrom());
					uris.add(((IRelation) locatable).getTo());
				} else if (locatable instanceof IRelationInstance) {
					uris.add(((IRelationInstance) locatable).getRelation()
							.getUri());
					uris.add(((IRelationInstance) locatable).getRelation()
							.getFrom());
					uris.add(((IRelationInstance) locatable).getRelation()
							.getTo());
					uris.add(((IRelationInstance) locatable).getPhenomenon());
				}
			}
		} catch (CodeStoreReadException | InterruptedException
				| ExecutionException e) {
			LOGGER.error("Can't retrieve origin cells", e);
		}
		return uris;
	}

	public Future<List<URI>> getElements() {
		return ExecUtils.nonUIAsyncExec((Callable<List<URI>>) () -> {
			List<URI> uris = new LinkedList<URI>();
			for (String id : AxialCodingComposite.this.jointjs.getElements()
					.get()) {
				uris.add(new URI(id));
			}
			return uris;
		});
	}

	public Future<List<URI>> getRelations() {
		return ExecUtils
				.nonUIAsyncExec((Callable<List<URI>>) () -> {
					List<URI> uris = new LinkedList<URI>();
					for (String id : AxialCodingComposite.this.jointjs
							.getLinks().get()) {
						if (id.contains("|")) {
							continue;
						}
						uris.add(new URI(id));
					}
					return uris;
				});
	}

	public Future<List<URI>> getIsARelations() {
		return ExecUtils.nonUIAsyncExec((Callable<List<URI>>) () -> {
			List<URI> uris = new LinkedList<URI>();
			for (String id : AxialCodingComposite.this.jointjs
					.getPermanentLinks().get()) {
				uris.add(new URI(id));
			}
			return uris;
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

	/**
	 *
	 * @param uri
	 * @param position
	 * @throws Exception
	 *
	 * @NonUIThread
	 */
	public void createElement(URI uri, Point position) throws Exception {
		String title = this.labelProvider.getText(uri);
		String content = this.labelProvider.getContent(uri);
		Point size = this.labelProvider.getSize(uri);

		if (position == null) {
			position = new Point(10, 10);
		}

		String id = null;
		try {
			id = this.jointjs.createElement(uri.toString(), title, content,
					position, size).get();
		} catch (Exception e) {
			LOGGER.error("Error creating element " + id, e);
		}

		if (id == null) {
			LOGGER.error("ID missing for created/updated element!");
		}

		this.refresh().get();
	}

	/**
	 *
	 * @param oldUri
	 * @param newUri
	 * @throws Exception
	 *
	 * @NonUIThread
	 */
	private void replaceElement(URI oldUri, URI newUri) throws Exception {
		this.deleteIsARelations(oldUri).get();
		String updatedJson = this.jointjs.save().get()
				.replace(oldUri.toString(), newUri.toString());
		this.jointjs.load(updatedJson).get();
		this.refresh().get();
	}

	/**
	 *
	 * @param uri
	 * @param position
	 * @throws Exception
	 *
	 * @NonUIThread
	 */
	public void createRelation(URI uri) throws Exception {
		IRelation relation = CODE_SERVICE.getRelation(uri);
		if (relation == null) {
			return;
		}

		this.jointjs.createLink(uri.toString(), relation.getFrom().toString(),
				relation.getTo().toString());
		this.refreshRelation(uri);
	}

	/**
	 * Creates in and outgoing "is a" permanent relations for the given
	 * {@link URI} without touching any existing relations.
	 *
	 * @param uri
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	private void createIsARelations(final URI uri) throws InterruptedException,
			ExecutionException {
		List<URI> existingElements = AxialCodingComposite.this.getElements()
				.get();
		ICode code = LocatorService.INSTANCE.resolve(uri, ICode.class, null)
				.get();
		if (code == null) {
			LOGGER.warn(uri + " is no valid code");
			return;
		}
		ICode parent = code;
		while (true) {
			parent = CODE_SERVICE.getParent(parent);
			if (parent == null) {
				break;
			}
			if (existingElements.contains(parent.getUri())) {
				AxialCodingComposite.this.createIsARelation(parent.getUri(),
						code.getUri());
			}
		}
		for (ICode child : CODE_SERVICE.getSubCodes(code)) {
			if (existingElements.contains(child.getUri())) {
				AxialCodingComposite.this.createIsARelation(code.getUri(),
						child.getUri());
			}
		}
	}

	/**
	 * Creates a "is a" permanent relation between the given parent and child
	 * element.
	 *
	 * @param parent
	 * @param child
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	private void createIsARelation(final URI parent, final URI child)
			throws InterruptedException, ExecutionException {
		LOGGER.info("Creating Is-A-Relation from "
				+ LocatorService.INSTANCE.resolve(child, ICode.class, null)
						.get().getCaption()
				+ " to "
				+ LocatorService.INSTANCE.resolve(parent, ICode.class, null)
						.get().getCaption());
		String id = parent.toString() + "|" + child.toString();

		id = AxialCodingComposite.this.jointjs.createPermanentLink(id,
				parent.toString(), child.toString()).get();

		String[] texts = new String[] { "is a" };
		for (int i = 0; texts != null && i < texts.length; i++) {
			AxialCodingComposite.this.jointjs.setText(id, i, texts[i]);
		}
	}

	/**
	 * Removes all incoming and outgoing "is a" permanent relations of the
	 * specified {@link URI}.
	 *
	 * @return
	 *
	 * @NonUIThread
	 */
	private Future<Void> deleteIsARelations(final URI uri) {
		return ExecUtils.nonUIAsyncExec((Callable<Void>) () -> {
			List<String> relationIds = AxialCodingComposite.this.jointjs
					.getConnectedPermanentLinks(uri.toString()).get();
			for (String relationId : relationIds) {
				AxialCodingComposite.this.jointjs.remove(relationId).get();
			}
			return null;
		});
	}

	/**
	 * Removes all outdated "is a" permanent relations from the graph.
	 *
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 *
	 * @NonUIThread
	 */
	private void deleteAllOutdatedIsARelationsBetweenValidElements()
			throws InterruptedException, ExecutionException {
		List<String> relationIds = this.jointjs.getPermanentLinks().get();
		for (String relationId : relationIds) {
			URI parentURI = new URI(relationId.split("\\|")[0]);
			URI subURI = new URI(relationId.split("\\|")[1]);

			ICode parentCode = LocatorService.INSTANCE.resolve(parentURI,
					ICode.class, null).get();
			ICode subCode = LocatorService.INSTANCE.resolve(subURI,
					ICode.class, null).get();
			if (parentCode != null && subCode != null
					&& !CODE_SERVICE.getSubCodes(parentCode).contains(subCode)) {
				this.jointjs.remove(relationId).get();
			}
		}
	}

	public Future<Void> refresh() {
		return ExecUtils.nonUIAsyncExec((Callable<Void>) () -> {
			List<URI> validUris = this.refreshElements();
			AxialCodingComposite.this
					.deleteAllOutdatedIsARelationsBetweenValidElements();
			for (URI uri : validUris) {
				AxialCodingComposite.this.createIsARelations(uri);
			}
			AxialCodingComposite.this.refreshRelations();
			return null;
		});
	}

	/**
	 * Updates non-structural information all existing elements based on the
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
	 * @return
	 * @throws Exception
	 *
	 * @NonUIThread
	 */
	public List<URI> refreshElements() throws Exception {
		return this.getElements().get().stream()
				.filter(e -> this.refreshElement(e))
				.collect(Collectors.toList());
	}

	/**
	 * Updates non-structural information of the given {@link URI} based on the
	 * internal information.
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
	 *
	 * @NonUIThread
	 */
	public boolean refreshElement(final URI uri) {
		try {
			if (!this.getElements().get().contains(uri)) {
				return false;
			}

			if (LocatorService.INSTANCE.resolve(uri, null).get() == null) {
				this.jointjs.addCustomClass(Arrays.asList(uri.toString()),
						"invalid");
				return false;
			}

			this.jointjs.removeCustomClass(Arrays.asList(uri.toString()),
					"invalid");

			this.jointjs.setElementTitle(uri.toString(),
					this.labelProvider.getText(uri));
			this.jointjs.setElementContent(uri.toString(),
					this.labelProvider.getContent(uri));
			this.jointjs.setColor(uri.toString(),
					this.labelProvider.getColor(uri));
			this.jointjs.setBackgroundColor(uri.toString(),
					this.labelProvider.getBackgroundColor(uri));
			this.jointjs.setBorderColor(uri.toString(),
					this.labelProvider.getBorderColor(uri));

			Point size = this.labelProvider.getSize(uri);
			if (size != null) {
				if (this.getOriginCells().contains(uri)) {
					this.jointjs.addCustomClass(Arrays.asList(uri.toString()),
							"origin");
					size.x += 100;
					size.y += 5;
				} else {
					this.jointjs.removeCustomClass(
							Arrays.asList(uri.toString()), "origin");
				}
				this.jointjs.setSize(uri.toString(), size.x, size.y);
			}

			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Update all {@link IRelation}s in the loaded {@link IAxialCodingModel}.
	 *
	 * @param from
	 * @param to
	 * @return the valid {@link IRelation}s
	 *
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	public List<URI> refreshRelations() throws Exception {
		return this.getRelations().get().stream()
				.filter(r -> this.refreshRelation(r))
				.collect(Collectors.toList());
	}

	/**
	 * Update the given {@link IRelation} in the loaded
	 * {@link IAxialCodingModel}.
	 *
	 * @param from
	 * @param to
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	public boolean refreshRelation(URI uri) {
		try {
			if (!this.getRelations().get().contains(uri)) {
				return false;
			}

			IRelation relation = CODE_SERVICE.getRelation(uri);
			if (relation == null) {
				this.jointjs.addCustomClass(Arrays.asList(uri.toString()),
						"invalid");
				return false;
			}

			this.jointjs.removeCustomClass(Arrays.asList(uri.toString()),
					"invalid");
			if (this.getOriginCells().contains(uri)) {
				this.jointjs.addCustomClass(Arrays.asList(uri.toString()),
						"origin");
			} else {
				this.jointjs.removeCustomClass(Arrays.asList(uri.toString()),
						"origin");
			}

			int groundingAll = CODE_SERVICE.getAllRelationInstances(relation)
					.size();
			int groundingImmediate = CODE_SERVICE
					.getRelationInstances(relation).size();

			StringBuffer caption = new StringBuffer();
			caption.append(relation.getName());
			caption.append("\\n");
			caption.append(groundingAll + " (" + groundingImmediate + ")");

			String[] texts = new String[] { caption.toString() };
			for (int i = 0; texts != null && i < texts.length; i++) {
				AxialCodingComposite.this.jointjs.setText(uri.toString(), i,
						texts[i]);
			}
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
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
