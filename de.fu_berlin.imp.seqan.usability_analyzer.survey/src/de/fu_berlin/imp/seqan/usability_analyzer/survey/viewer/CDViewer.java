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
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
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

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final IUriPresenterService presenterService = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	private BootstrapBrowser browser;

	private SurveyContainer surveyContainer;

	private ISelection selection = null;

	public CDViewer(final BootstrapBrowser browser) {
		this.browser = browser;

		this.presenterService.enable(this.browser,
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
				CDViewer.this.presenterService.disable(browser);
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
						final ILocatable locatable = CDViewer.this.locatorService
								.resolve(uri, null).get();
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
	public void setInput(Object input) {
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
					documentCodes.addAll(this.codeService.getCodes(cdDocument
							.getUri()));
				} catch (CodeServiceException e) {
					LOGGER.warn(e);
				}

				boolean documentMemoExists = this.codeService.isMemo(cdDocument
						.getUri());
				form.addRaw("<tr><td>");
				form.addRaw("<h2 tabindex=\""
						+ 0
						+ "\" data-focus-id=\""
						+ cdDocument.getUri()
						+ "\"><a name=\""
						+ cdDocument.getUri()
						+ "\">"
						+ caption
						+ "</a>"
						+ (documentMemoExists ? " <img src=\""
								+ ImageUtils
										.createUriFromImage(ImageManager.MEMO)
								+ "\"> " : " ") + " </h2>");
				form.addRaw("</td><td>");
				form.addRaw("<a href=\"addcode-" + cdDocument.getUri()
						+ "\" class=\"btn btn-primary btn-sm\">Add Code...</a>");
				form.addRaw("</td><td>");
				form.addRaw(this.createCodeLinks(documentCodes));
				form.addRaw("</td></tr>");

				for (CDDocumentField field : cdDocument) {
					List<ICode> fieldCodes = new ArrayList<ICode>();
					try {
						fieldCodes.addAll(this.codeService.getCodes(field
								.getUri()));
					} catch (CodeServiceException e) {
						LOGGER.warn(e);
					}

					boolean fieldMemoExists = this.codeService.isMemo(field
							.getUri());

					form.addRaw("<tr><td>");
					form.addStaticField(
							field.getUri().toString(),
							field.getQuestion()
									+ (fieldMemoExists ? " <img src=\""
											+ ImageUtils
													.createUriFromImage(ImageManager.MEMO)
											+ "\">"
											: ""), field.getAnswer(), 0);
					form.addRaw("</td><td>");
					form.addRaw("<a href=\"addcode-"
							+ field.getUri()
							+ "\" class=\" btn btn-primary btn-xs\">Add Code...</a>");
					form.addRaw("</td><td>");
					form.addRaw(this.createCodeLinks(fieldCodes));
					form.addRaw("</td></tr>");
				}
			}
			form.addRaw("</table>");

			StringBuilder html = new StringBuilder();
			BootstrapBuilder bootstrapBuilder = new BootstrapBuilder();
			bootstrapBuilder.addHeaderNavigation(navigationElements, 0);
			html.append(bootstrapBuilder.toString());
			html.append("<br><br>");
			html.append("<div class=\"container\">");
			html.append(form.toString());
			html.append("</div>");

			this.browser
					.setBodyHtml("<style>header{opacity:0.7;} .form-horizontal td .form-group { margin: 0; padding-bottom: 10px; }</style>"
							+ html.toString());

			Point pos = new SUACorePreferenceUtil()
					.getLastScrollPosition(CDViewer.class);
			try {
				CDViewer.this.browser.scrollTo(pos).get();
			} catch (Exception e) {
				LOGGER.error("Error scrolling to last scroll position");
			}

			// debug
			// ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			// @Override
			// public Void call() throws Exception {
			// String html = CDViewer.this.browser.getHtml().get();
			// System.err.println(html);
			// return null;
			// }
			// });
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
		ExecUtils.nonUIAsyncExec(CDViewer.class, "Refresh",
				new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						Point pos = CDViewer.this.browser.getScrollPosition()
								.get();
						CDViewer.this.setInput(CDViewer.this.surveyContainer);
						CDViewer.this.browser.scrollTo(pos);
						return null;
					}
				});
	}

}
