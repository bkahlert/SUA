package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.loader.Loader;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.GTLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.WizardUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocument;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocumentField;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.BootstrapBuilder.NavigationElement;

public class CDViewer extends Viewer {

	private static final Logger LOGGER = Logger.getLogger(CDViewer.class);

	private static final ILocatorService LOCATOR_SERVICE = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private static final IImportanceService IMPORTANCE_SERVICE = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);
	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private static final IUriPresenterService PRESENTER_SERVICE = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	private BootstrapBrowser browser;
	private Loader loader;

	private SurveyContainer surveyContainer;

	private ISelection selection = null;

	public CDViewer(final BootstrapBrowser browser) {
		this.loader = new Loader(browser);
		this.browser = browser;
		this.browser
				.injectCss("header{opacity:0.7;} .form-horizontal td .form-group { margin: 0; padding-bottom: 10px; }");

		PRESENTER_SERVICE.enable(this.browser,
				new ISubjectInformationProvider<Control, URI>() {
					private URI hovered = null;

					private final IAnkerListener ankerListener = new AnkerAdapter() {
						@Override
						public void ankerHovered(IAnker anker, boolean entered) {
							URI uri = new URI(anker.getHref());

							if (uri.getScheme() != null
									&& !uri.getScheme().contains("-")
									&& entered) {
								try {
									hovered = uri;
								} catch (Exception e) {
									LOGGER.error(e);
								}
							} else {
								hovered = null;
							}
						}
					};

					@Override
					public void register(Control subject) {
						browser.addAnkerListener(this.ankerListener);
					}

					@Override
					public void unregister(Control subject) {
						browser.removeAnkerListener(this.ankerListener);
					}

					@Override
					public Point getHoverArea() {
						return new Point(20, 10);
					}

					@Override
					public URI getInformation() {
						return this.hovered;
					}
				});

		browser.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				PRESENTER_SERVICE.disable(browser);
			}
		});

		/*
		 * Handle add code events
		 */
		this.browser.addAnkerListener(new AnkerAdapter() {
			@Override
			public void ankerClicked(IAnker anker) {
				if (!anker.getHref().startsWith("addcode-")) {
					return;
				}

				final AtomicReference<URI> uri = new AtomicReference<URI>();
				uri.set(new URI(anker.getHref().substring("addcode-".length())));
				ExecUtils.nonUISyncExec(CDViewer.class,
						"Opening Add Code Wizard", new Callable<Void>() {
							@Override
							public Void call() throws Exception {
								WizardUtils.openAddCodeWizard(uri.get(),
										Utils.getFancyCodeColor());
								return null;
							}
						});
			}
		});

		this.browser.addFocusListener(new IFocusListener() {
			@Override
			public void focusLost(IElement element) {
			}

			@Override
			public void focusGained(final IElement element) {
				ExecUtils.nonUISyncExec(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						URI uri = new URI(element.getAttribute("data-focus-id"));
						final ILocatable locatable = LOCATOR_SERVICE.resolve(
								uri, null).get();
						ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
								CDViewer.this
										.setSelection(new StructuredSelection(
												locatable));
							}
						});
						return null;
					}
				});
			}
		});

		this.browser.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				try {
					Point scrollPosition = browser.getScrollPosition().get();
					new SUACorePreferenceUtil().setLastScrollPosition(
							CDViewer.class, scrollPosition);
				} catch (Exception e1) {
					LOGGER.error("Error saving scroll position", e1);
				}
			}
		});
	}

	@Override
	public Control getControl() {
		return this.browser;
	}

	@Override
	public void setInput(final Object input) {
		if (input instanceof SurveyContainer) {
			this.surveyContainer = (SurveyContainer) input;

			// sort
			List<CDDocument> cdDocuments = new ArrayList<CDDocument>();
			cdDocuments.addAll(this.surveyContainer.getCDDocuments());
			Collections.sort(cdDocuments, new Comparator<CDDocument>() {
				@Override
				public int compare(CDDocument o1, CDDocument o2) {
					return o1.getCompleted().compareTo(o2.getCompleted());
				}
			});

			List<NavigationElement> navigationElements = new ArrayList<NavigationElement>();

			FormBuilder form = new FormBuilder();
			form.addRaw("<table>");
			for (CDDocument cdDocument : cdDocuments) {
				String caption = cdDocument.getIdentifierHash();

				navigationElements.add(new NavigationElement(caption, "#"
						+ cdDocument.getUri()));

				List<ICode> documentCodes = new ArrayList<ICode>();
				try {
					documentCodes.addAll(CODE_SERVICE.getCodes(cdDocument
							.getUri()));
				} catch (CodeServiceException e) {
					LOGGER.warn(e);
				}

				boolean documentMemoExists = CODE_SERVICE.isMemo(cdDocument
						.getUri());
				form.addRaw("<tr><td>");
				form.addRaw("<h2 tabindex=\"" + 0 + "\" data-focus-id=\""
						+ cdDocument.getUri() + "\"><a name=\""
						+ cdDocument.getUri() + "\">" + caption + "</a></h2>");
				form.addRaw("</td><td>");
				form.addRaw("<a href=\"addcode-" + cdDocument.getUri()
						+ "\" class=\"btn btn-primary btn-sm\">Add Code...</a>");
				form.addRaw("</td><td>");
				if (documentMemoExists) {
					form.addRaw(this.createMemoIcon() + " ");
				}
				form.addRaw(this.createCodeLinks(documentCodes));
				form.addRaw("</td></tr>");

				for (CDDocumentField field : cdDocument) {
					List<ICode> fieldCodes = new ArrayList<ICode>();
					try {
						fieldCodes
								.addAll(CODE_SERVICE.getCodes(field.getUri()));
					} catch (CodeServiceException e) {
						LOGGER.warn(e);
					}

					boolean fieldMemoExists = CODE_SERVICE.isMemo(field
							.getUri());

					form.addRaw("<tr><td>");
					switch (IMPORTANCE_SERVICE.getImportance(field.getUri())) {
					case HIGH:
						form.addRaw("<span style='font-size: 1.2em; color: "
								+ RGB.IMPORTANCE_HIGH.toHexString() + "'>");
						break;
					case LOW:
						form.addRaw("<span style='font-weight: 300; font-size: 0.75em; color: "
								+ RGB.IMPORTANCE_LOW.toHexString() + "''>");
						break;
					default:
						break;
					}
					form.addStaticField(field.getUri().toString(),
							field.getQuestion(), field.getAnswer(), 0);
					switch (IMPORTANCE_SERVICE.getImportance(field.getUri())) {
					case HIGH:
						form.addRaw("</span>");
						break;
					case LOW:
						form.addRaw("</span>");
						break;
					default:
						break;
					}
					form.addRaw("</td><td>");
					form.addRaw("<a href=\"addcode-"
							+ field.getUri()
							+ "\" class=\" btn btn-primary btn-xs\">Add Code...</a>");
					form.addRaw("</td><td>");
					if (fieldMemoExists) {
						form.addRaw(this.createMemoIcon() + " ");
					}
					form.addRaw(this.createCodeLinks(fieldCodes));
					form.addRaw("</td></tr>");
				}
			}
			form.addRaw("</table>");

			final StringBuilder html = new StringBuilder();
			BootstrapBuilder bootstrapBuilder = new BootstrapBuilder();
			bootstrapBuilder.addHeaderNavigation(navigationElements, 0);
			html.append(bootstrapBuilder.toString());
			html.append("<br><br>");
			html.append("<div class=\"container\">");
			html.append(form.toString());
			html.append("</div>");

			try {
				ExecUtils.syncExec(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						CDViewer.this.browser.setBodyHtml(html.toString());
						return null;
					}
				});
			} catch (Exception e) {
				LOGGER.error("Error loading " + this.surveyContainer, e);
			}

		}
	}

	@Override
	public Object getInput() {
		return this.surveyContainer;
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		this.selection = selection;
		this.fireSelectionChanged(new SelectionChangedEvent(this, selection));
	}

	private String createMemoIcon() {
		return "<img src=\""
				+ ImageUtils.createUriFromImage(ImageManager.MEMO)
				+ "\" style=\"display: inline-block; margin: 3px; width:16px;\">";
	}

	private String createCodeLinks(List<ICode> codes) {
		List<String> html = new ArrayList<String>();
		for (ICode code : codes) {
			html.add("<a href=\""
					+ code.getUri()
					+ "\" data-focus-id=\""
					+ code.getUri().toString()
					+ "\" tabindex=\"-1\" style=\"display: inline-block; margin: 3px;\"><img src=\""
					+ GTLabelProvider.getCodeImageURI(code) + "\"/></a>");
		}
		return StringUtils.join(html, " ");
	}

	@Override
	public void refresh() {
		this.loader.run(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Point pos = CDViewer.this.browser.getScrollPosition().get();
				CDViewer.this.setInput(CDViewer.this.surveyContainer);
				CDViewer.this.browser.scrollTo(pos);
				return null;
			}
		}, false);
	}

}
