package de.fu_berlin.imp.apiua.groundedtheory.ui;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.SWTUtils;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.utils.colors.ColorSpaceConverter;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.HLS;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.viewer.SortableTreeViewer;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdaptingListener;
import com.bkahlert.nebula.widgets.composer.IAnkerLabelProvider;
import com.bkahlert.nebula.widgets.scale.IScale;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.util.NoNullSet;
import de.fu_berlin.imp.apiua.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.CodeLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.RelationInstanceLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.RelationLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.EditingSupport;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.RelationViewer;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.ViewerURI;

public class Utils {

	public static final class AnkerLabelProvider implements IAnkerLabelProvider {

		private static final Logger LOGGER = Logger
				.getLogger(AnkerAdaptingListener.class);

		public static final AnkerLabelProvider INSTANCE = new AnkerLabelProvider();

		private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);

		private AnkerLabelProvider() {
		}

		@Override
		public boolean isResponsible(IAnker anker) {
			if (anker.getHref() != null) {
				try {
					URI uri = new URI(anker.getHref());
					Future<ILocatable> locatable = LocatorService.INSTANCE
							.resolve(uri, null);
					if (locatable.get() != null) {
						return true;
					}
				} catch (Exception e) {
					if (!URISyntaxException.class.isInstance(e.getCause())) {
						LOGGER.error("Error handling " + anker.getHref(), e);
					}
				}
			}
			return false;
		}

		@Override
		public String getHref(IAnker anker) {
			return anker.getHref();
		}

		@Override
		public String[] getClasses(IAnker anker) {
			return new String[] { "special" };
		}

		@Override
		public String getContent(IAnker anker) {
			if (anker.getHref() != null) {
				try {
					URI uri = new URI(anker.getHref());
					ILabelProvider labelProvider = this.labelProviderService
							.getLabelProvider(uri);
					if (labelProvider != null) {
						return labelProvider.getText(uri);
					}
				} catch (URISyntaxException e) {

				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
			return "!!! " + anker.getHref() + " !!!";
		}
	}

	public static final Logger LOGGER = Logger.getLogger(Utils.class);

	public static String chooseGTFileLocation() {
		DirectoryDialog directoryDialog = new DirectoryDialog(new Shell()); // TODO
		directoryDialog.setText("Grounded Theory Directory");
		directoryDialog
				.setMessage("Please choose where you want to store your grounded theory progress.");
		String filename = directoryDialog.open();
		return filename;
	}

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private static final ILabelProviderService LABELPROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	/**
	 * Returns a color that - given the colors of all existing codes - is as
	 * different as possible.
	 *
	 * @return
	 */
	public static RGB getFancyCodeColor() {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		Set<RGB> rgbs = new NoNullSet<RGB>();
		for (ICode code : codeService.getCodeStore().getCodes()) {
			RGB rgb = code.getColor();
			HLS hls = ColorSpaceConverter.RGBtoHLS(rgb);

			double lightness = hls.getLightness();
			double saturation = hls.getSaturation();
			if (lightness > 0.4 && lightness < 0.6 && saturation > 0.4
					&& saturation < 0.6) {
				rgbs.add(code.getColor());
			}
		}
		return ColorUtils.getBestComplementColor(rgbs);
	}

	public static void addCodeColorRenderSupport(Tree tree,
			final int columnNumber) {
		tree.addListener(
				SWT.PaintItem,
				event -> {
					if (!(event.item instanceof TreeItem)
							|| !(event.item.getData() instanceof URI)) {
						return;
					}
					TreeItem item = (TreeItem) event.item;
					Rectangle bounds = item.getImageBounds(columnNumber);
					bounds.width = 14;
					bounds.height = 14;
					bounds.y += 2;
					bounds.x -= 2;

					ICode code = null;
					try {
						code = LocatorService.INSTANCE.resolve(
								(URI) item.getData(), ICode.class, null).get();
					} catch (Exception e) {
						LOGGER.error("Error painting color of "
								+ item.getData());
					}
					if (code != null) {
						GTLabelProvider.drawCodeImage(code, event.gc, bounds);
					}
				});
		tree.addListener(SWT.MouseMove, new Listener() {
			private final Cursor hand = new Cursor(Display.getCurrent(),
					SWT.CURSOR_HAND);

			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Tree)) {
					return;
				}
				Tree tree = ((Tree) event.widget);
				TreeItem item = tree.getItem(new Point(event.getBounds().x,
						event.getBounds().y));
				if (item != null && item.getData() instanceof URI) {
					if (ICode.class.equals(LocatorService.INSTANCE
							.getType((URI) item.getData()))) {
						Rectangle bounds = item.getImageBounds(columnNumber);
						bounds.width = 14;
						bounds.height = 14;
						bounds.y += 2;
						bounds.x -= 2;

						if (event.getBounds().x >= bounds.x
								&& event.getBounds().x <= bounds.x
										+ bounds.width) {
							tree.setCursor(this.hand);
						} else {
							tree.setCursor(null);
						}
					}
				}
			}
		});
		tree.addListener(
				SWT.MouseUp,
				event -> {
					if (!(event.widget instanceof Tree)) {
						return;
					}
					Tree tree1 = ((Tree) event.widget);
					if (tree1.getCursor() != null) {
						ICommandService cmdService = (ICommandService) PlatformUI
								.getWorkbench().getService(
										ICommandService.class);
						Command cmd = cmdService
								.getCommand("de.fu_berlin.imp.apiua.groundedtheory.commands.recolorCode");
						try {
							cmd.executeWithChecks(new ExecutionEvent());
						} catch (NotHandledException e1) {
						} catch (Exception e2) {
							LOGGER.error("Error recoloring "
									+ event.item.getData());
						}
					}
				});
	}

	/**
	 * Creates a column that not only shows the pure object but also interesting
	 * meta data (like assigned dimension values).
	 *
	 * @param treeViewer
	 */
	public static void createPimpedColumn(SortableTreeViewer treeViewer) {
		TreeViewerColumn codeColumn = treeViewer.createColumn("Code",
				new RelativeWidth(1.0, 150));

		final GTLabelProvider labelProvider = new GTLabelProvider();
		CodeViewer codeViewer = SWTUtils.getParent(CodeViewer.class,
				treeViewer.getTree());
		RelationViewer relationViewer = SWTUtils.getParent(
				RelationViewer.class, treeViewer.getTree());

		codeColumn
				.setLabelProvider(new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						if (uri == ViewerURI.NO_CODES_URI) {
							return new StyledString("no codes",
									Stylers.MINOR_STYLER);
						}
						if (uri == ViewerURI.NO_RELATIONS_URI) {
							return new StyledString("no relations",
									Stylers.MINOR_STYLER);
						}
						if (uri == ViewerURI.NO_PHENOMENONS_URI) {
							return new StyledString("no phenomenons",
									Stylers.MINOR_STYLER);
						}
						StyledString text = labelProvider.getStyledText(uri);

						if (CodeLocatorProvider.CODE_NAMESPACE.equals(URIUtils
								.getResource(uri))) {
							Pair<StyledString, StyledString> dimensionValues = GTLabelProvider
									.getDimensionValues(CODE_SERVICE
											.getAllInstances(uri).toArray(
													new ICodeInstance[0]));
							if (dimensionValues.getFirst() != null) {
								text.append(" = ");
								text.append(dimensionValues.getFirst());
							}
							if (dimensionValues.getSecond() != null) {
								text.append(" ")
										.append("(", Stylers.MINOR_STYLER)
										.append(Stylers.rebase(
												dimensionValues.getSecond(),
												Stylers.MINOR_STYLER))
										.append(")", Stylers.MINOR_STYLER);
							}
						}

						if (CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE
								.equals(URIUtils.getResource(uri))) {
							ICodeInstance codeInstance = LocatorService.INSTANCE
									.resolve(uri, ICodeInstance.class, null)
									.get();
							Stylers.rebase(text, Stylers.SMALL_STYLER);

							if (codeViewer != null) {
								text = removeGroundingInformation(text);
							}

							if (CodeLocatorProvider.CODE_NAMESPACE
									.equals(URIUtils.getResource(codeInstance
											.getId()))) {
								text.append("  phenomenon",
										Stylers.MINOR_STYLER);
							}
						}

						if (relationViewer != null) {
							if (CodeLocatorProvider.CODE_NAMESPACE
									.equals(URIUtils.getResource(uri))) {
								if (!relationViewer
										.getShowRelationInstancesToFirst()) {
									text = labelProvider.getStyledText(uri);
									text = Stylers.append(text,
											new StyledString(" ...",
													Stylers.BOLD_STYLER));

									Pair<Set<String>, Set<String>> dimensionValues = CODE_SERVICE.getDimensionValues(CODE_SERVICE
											.getRelationInstancesStartingFrom(uri));
									text = highlightUsedValues(text,
											dimensionValues.getFirst());
								} else {
									text = labelProvider.getStyledText(uri);
									text = new StyledString("... ",
											Stylers.BOLD_STYLER).append(text);

									Pair<Set<String>, Set<String>> dimensionValues = CODE_SERVICE.getDimensionValues(CODE_SERVICE
											.getRelationInstancesEndingAt(uri));
									text = highlightUsedValues(text,
											dimensionValues.getSecond());
								}
							} else if (RelationLocatorProvider.RELATION_NAMESPACE
									.equals(URIUtils.getResource(uri))) {
								// remove from field

								IRelation relation = LocatorService.INSTANCE
										.resolve(uri, IRelation.class, null)
										.get();

								if (!relationViewer
										.getShowRelationInstancesToFirst()) {
									int pos = text.getString().indexOf(
											GTLabelProvider.RELATION_ARROW);
									if (pos >= 0) {
										text = Stylers
												.substring(
														text,
														pos
																+ GTLabelProvider.RELATION_ARROW
																		.length()
																- 1, text
																.length());
									}
								} else {
									int pos = text.getString().lastIndexOf(
											GTLabelProvider.RELATION_ARROW);
									if (pos >= 0) {
										text = Stylers.substring(text, 0, pos)
												.append("   ⤣",
														Stylers.BOLD_STYLER);
									}
								}

								Pair<Set<String>, Set<String>> dimensionValues = CODE_SERVICE.getDimensionValues(CODE_SERVICE
										.getExplicitRelationInstances(relation));
								text = highlightUsedValues(
										text,
										relationViewer
												.getShowRelationInstancesToFirst() ? dimensionValues
												.getFirst() : dimensionValues
												.getSecond());
							} else if (RelationInstanceLocatorProvider.RELATION_INSTANCE_NAMESPACE
									.equals(URIUtils.getResource(uri))) {
								text = Stylers.rebase(text,
										Stylers.SMALL_STYLER);

								if (relationViewer != null) {
									text = removeGroundingInformation(text);
								}

								IRelationInstance relationInstance = LocatorService.INSTANCE
										.resolve(uri, IRelationInstance.class,
												null).get();
								Pair<Pair<IDimension, String>, Pair<IDimension, String>> dimensionValue = CODE_SERVICE
										.getDimensionValue(relationInstance);
								String fromDimensionValue = dimensionValue
										.getFirst().getFirst() != null ? dimensionValue
										.getFirst().getSecond() != null ? dimensionValue
										.getFirst().getSecond()
										: IScale.UNSET_LABEL
										: "—";
								String toDimensionValue = dimensionValue
										.getSecond().getFirst() != null ? dimensionValue
										.getSecond().getSecond() != null ? dimensionValue
										.getSecond().getSecond()
										: IScale.UNSET_LABEL
										: "—";
								Stylers.setDisableColorMix(true);
								text = text
										.append(" (", Stylers.MINOR_STYLER)
										.append(fromDimensionValue,
												Stylers.combine(
														Stylers.MINOR_STYLER,
														GTLabelProvider.VALID_VALUE_STYLER))
										.append("; ", Stylers.MINOR_STYLER)
										.append(toDimensionValue,
												Stylers.combine(
														Stylers.MINOR_STYLER,
														GTLabelProvider.VALID_VALUE_STYLER))
										.append(")", Stylers.MINOR_STYLER);
								Stylers.setDisableColorMix(false);

								if (relationInstance instanceof ImplicitRelationInstance) {
									ImplicitRelationInstance implicitRelationInstance = (ImplicitRelationInstance) relationInstance;
									text = Stylers.rebase(text,
											Stylers.IMPORTANCE_LOW_STYLER);
									text = Stylers.append(
											text,
											new StyledString(
													" grounding "
															+ labelProvider
																	.getText(implicitRelationInstance
																			.getRelation()
																			.getExplicitRelation()
																			.getUri())
															+ "",
													Stylers.ITALIC_STYLER));
								}
							}
						}

						return text;
					}

					@Override
					public Image getImage(URI uri) throws Exception {
						if (uri == ViewerURI.NO_CODES_URI) {
							return null;
						}
						if (uri == ViewerURI.NO_RELATIONS_URI) {
							return null;
						}
						if (uri == ViewerURI.NO_PHENOMENONS_URI) {
							return null;
						}

						Image image = labelProvider.getImage(uri);

						// codeInstances pointing to relationInstances
						// if (codeViewer != null
						// &&
						// CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE
						// .equals(URIUtils.getResource(uri))) {
						// ICodeInstance codeInstance = LocatorService.INSTANCE
						// .resolve(uri, ICodeInstance.class, null)
						// .get();
						// if
						// (RelationInstanceLocatorProvider.RELATION_INSTANCE_NAMESPACE
						// .equals(URIUtils.getResource(codeInstance
						// .getId()))) {
						// image = ImageManager.RELATION_INSTANCE;
						// }
						// }

						if (relationViewer != null
								&& RelationLocatorProvider.RELATION_NAMESPACE
										.equals(URIUtils.getResource(uri))) {
							image = null;
						}

						return image;
					}
				});

		codeColumn.setEditingSupport(new EditingSupport(treeViewer));
		TreeViewerEditor.create(treeViewer,
				new ColumnViewerEditorActivationStrategy(treeViewer) {
					@Override
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				}, ColumnViewerEditor.DEFAULT);
	}

	public static void createNumPhaenomenonsColumn(
			SortableTreeViewer treeViewer, final ICodeService codeService) {

		RelationViewer relationViewer = SWTUtils.getParent(
				RelationViewer.class, treeViewer.getTree());

		TreeViewerColumn countColumn = treeViewer.createColumn("# ph",
				new AbsoluteWidth(60));
		countColumn.getColumn().setAlignment(SWT.RIGHT);
		countColumn
				.setLabelProvider(new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						ILocatable element = LocatorService.INSTANCE.resolve(
								uri, null).get();
						StyledString text = new StyledString();

						if (ICode.class.isInstance(element)) {
							ICode code = (ICode) element;
							int all = codeService.getAllInstances(code).size()
									- codeService.getDescendents(code).size();
							int here = codeService.getExplicitInstances(code)
									.size();
							text = new StyledString(all + "",
									Stylers.DEFAULT_STYLER);
							text.append("   " + here, Stylers.COUNTER_STYLER);
						}

						if (relationViewer != null
								&& ICode.class.isInstance(element)) {
							int all = !relationViewer
									.getShowRelationInstancesToFirst() ? codeService
									.getAllRelationInstancesStartingFrom(uri)
									.size() : codeService
									.getAllRelationInstancesEndingAt(uri)
									.size();
							// int here = codeService.getInstances(code).size();
							text = new StyledString(all + "",
									Stylers.DEFAULT_STYLER);
							// text.append("   " + here,
							// Stylers.COUNTER_STYLER);
						}

						if (IRelation.class.isInstance(element)) {
							IRelation relation = (IRelation) element;
							int all = codeService.getAllRelationInstances(
									relation).size();
							int here = codeService
									.getExplicitRelationInstances(relation)
									.size();
							text = new StyledString(all + "",
									Stylers.DEFAULT_STYLER);
							text.append("   " + here, Stylers.COUNTER_STYLER);
						}

						return text;
					}
				});
	}

	/**
	 * Highlights the {@link IDimension} values in the given text. The values
	 * are retrieved from the {@link IRelationInstance}s.
	 * <p>
	 * e.g. &quot;ABC - value1, value2&quot; and the retrieved value
	 * &quot;value2&quot; will highlight return the string with
	 * &quot;value2&quot; highlighted.
	 *
	 * @param string
	 * @param relationViewer
	 * @param relationInstances
	 * @return
	 */
	private static StyledString highlightUsedValues(StyledString string,
			Collection<String> words) {
		Stylers.setDisableColorMix(true);
		string = Stylers.apply(string, GTLabelProvider.VALID_VALUE_STYLER,
				words);
		Stylers.setDisableColorMix(false);
		return string;
	}

/**
	 * Removed the (grounding ...) portion from a {@link StyledString).
	 * @param text
	 * @return
	 */
	public static StyledString removeGroundingInformation(StyledString text) {
		int pos = text.getString().indexOf(" (grounding ");
		if (pos < 0) {
			pos = text.getString().indexOf(" (indirectly grounding ");
		}
		if (pos >= 0) {
			int end = StringUtils.findClosingParenthese(text.getString(),
					pos + 1);
			if (end > pos) {
				return Stylers.substring(text, 0, pos).append(
						Stylers.substring(text, end + 1, text.length()));
			}
		}
		return text;
	}

/**
	 * Removed the (grounding ...) portion from a {@link String).
	 * @param text
	 * @return
	 */
	public static String removeGroundingInformation(String text) {
		int pos = text.indexOf(" (grounding ");
		if (pos >= 0) {
			int end = StringUtils.findClosingParenthese(text, pos + 1);
			if (end > pos) {
				return text.substring(0, pos)
						+ text.substring(end + 1, text.length());
			}
		}
		return text;
	}

	/**
	 * Generates an unordered html list (<code>&lt;ul&gt;</code>) containing the
	 * codes and memos applied to the given element.
	 *
	 * @param locatable
	 * @return
	 */
	public static String createAnnotations(ILocatable locatable) {
		StringBuilder html = new StringBuilder("<ul class='instances'>");
		boolean empty = true;

		if (CODE_SERVICE.isMemo(locatable.getUri())) {
			html.append("<li style=\"list-style-image: url('"
					+ ImageUtils.createUriFromImage(ImageManager.MEMO)
					+ "');\">");
			html.append(StringUtils.plainToHtml(StringUtils.shorten(
					CODE_SERVICE.loadMemoPlain(locatable.getUri()), 100)));
			html.append("</li>");
			empty = false;
		}

		for (ICodeInstance codeInstance : CODE_SERVICE
				.getExplicitInstances(locatable.getUri())) {
			String immediateDimensionValue = CODE_SERVICE.getDimensionValue(
					codeInstance.getUri(), codeInstance.getCode());
			html.append("<li style=\"list-style-image: url('"
					+ GTLabelProvider.getCodeImageURI(codeInstance.getCode())
					+ "');\"><a href=\""
					+ codeInstance.getCode().getUri()
					+ "\" data-focus-id=\""
					+ codeInstance.getCode().getUri().toString()
					+ "\" data-workspace=\""
					+ codeInstance.getUri().toString()
					+ "\" tabindex=\"-1\" draggable=\"true\" data-dnd-mime=\"text/plain\" data-dnd-data=\""
					+ codeInstance.getCode().getUri() + "\">"
					+ codeInstance.getCode().getCaption());
			if (immediateDimensionValue != null) {
				html.append("<strong> = ");
				html.append(immediateDimensionValue);
				html.append("</strong>");
			}
			html.append("</a><ul>");
			if (CODE_SERVICE.isMemo(codeInstance.getUri())) {
				html.append("<li style=\"list-style-image: url('"
						+ ImageUtils.createUriFromImage(ImageManager.MEMO)
						+ "');\">");
				html.append(StringUtils.plainToHtml(StringUtils.shorten(
						CODE_SERVICE.loadMemoPlain(codeInstance.getUri()), 100)));
				html.append("</li>");
			}
			// for (Triple<URI, IDimension, String> dimensionValue :
			// dimensionValues) {
			// html.append("<li style=\"list-style-image: none;\">");
			// try {
			// html.append(LocatorService.INSTANCE.resolve(
			// dimensionValue.getFirst(), ICode.class, null).get());
			// } catch (Exception e) {
			// LOGGER.error(e);
			// html.append(dimensionValue.getFirst());
			// }
			// html.append(" = ");
			// html.append(dimensionValue.getThird());
			// html.append("</li>");
			// }
			html.append("</ul></li>");
			empty = false;
		}

		for (IRelationInstance relationInstance : CODE_SERVICE
				.getExplicitRelationInstances(locatable.getUri())) {
			String immediateDimensionValue = null;

			html.append("<li style=\"list-style-image: url('"
					+ BrowserUtils.createDataUri(ImageManager.RELATION)
					+ "');\"><a href=\""
					+ relationInstance.getRelation().getUri()
					+ "\" data-focus-id=\""
					+ relationInstance.getRelation().getUri().toString()
					+ "\" data-workspace=\""
					+ relationInstance.getUri().toString()
					+ "\" tabindex=\"-1\" draggable=\"true\" data-dnd-mime=\"text/plain\" data-dnd-data=\""
					+ relationInstance.getUri()
					+ "\">"
					+ LABELPROVIDER_SERVICE.getText(relationInstance
							.getRelation().getUri()));
			if (immediateDimensionValue != null) {
				// html.append("<strong> = ");
				// html.append(immediateDimensionValue);
				// html.append("</strong>");
			}
			html.append("</a><ul>");
			if (CODE_SERVICE.isMemo(relationInstance.getUri())) {
				html.append("<li style=\"list-style-image: url('"
						+ ImageUtils.createUriFromImage(ImageManager.MEMO)
						+ "');\">");
				html.append(StringUtils.plainToHtml(StringUtils.shorten(
						CODE_SERVICE.loadMemoPlain(relationInstance.getUri()),
						100)));
				html.append("</li>");
			}
			html.append("</ul></li>");
			empty = false;
		}

		html.append("</ul>");
		return !empty ? html.toString() : "";
	}
}
