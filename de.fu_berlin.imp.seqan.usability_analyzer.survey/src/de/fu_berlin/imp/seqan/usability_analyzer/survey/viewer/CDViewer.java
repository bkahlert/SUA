package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.utils.PaintUtils;
import com.bkahlert.devel.nebula.utils.StringUtils;
import com.bkahlert.devel.nebula.widgets.browser.extended.BootstrapEnabledBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite.IFocusListener;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.devel.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.nebula.utils.ImageUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer.CodeColors;
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

	private BootstrapEnabledBrowserComposite browser;
	private SurveyContainer surveyContainer;

	private ISelection selection = null;

	public CDViewer(BootstrapEnabledBrowserComposite browser) {
		this.browser = browser;

		/*
		 * Handle add code events
		 */
		this.browser.addAnkerListener(new AnkerAdapter() {
			@Override
			public void ankerClicked(IAnker anker) {
				if (!anker.getHref().startsWith("addCode-")) {
					return;
				}

				final AtomicReference<URI> uri = new AtomicReference<URI>();
				try {
					uri.set(new URI(anker.getHref().substring(
							"addCode-".length())));
				} catch (URISyntaxException e) {
					LOGGER.error("Can't create URI to code from "
							+ anker.getHref());
					return;
				}
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
			for (CDDocument cdDocument : cdDocuments) {
				String caption = cdDocument.getIdentifier().toString();

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
				form.addRaw("<h2 tabindex=\"0\" data-focus-id=\""
						+ cdDocument.getUri()
						+ "\"><a name=\""
						+ cdDocument.getUri()
						+ "\">"
						+ caption
						+ "</a>"
						+ (documentMemoExists ? " <img src=\""
								+ ImageUtils
										.createUriFromImage(ImageManager.MEMO)
								+ "\"> " : " ")
						+ this.createCodeLinks(documentCodes)
						+ " <a href=\"addCode-"
						+ cdDocument.getUri()
						+ "\" class=\"btn btn-primary btn-sm\">Add Code...</a></h2>");

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

					form.addStaticField(
							field.getUri().toString(),
							field.getQuestion()
									+ (fieldMemoExists ? " <img src=\""
											+ ImageUtils
													.createUriFromImage(ImageManager.MEMO)
											+ "\">"
											: "")
									+ " "
									+ this.createCodeLinks(fieldCodes)
									+ " <a href=\"addCode-"
									+ field.getUri()
									+ "\" class=\" btn btn-primary btn-xs\">Add Code...</a>",
							field.getAnswer(), true);
				}
			}

			StringBuilder html = new StringBuilder();
			BootstrapBuilder bootstrapBuilder = new BootstrapBuilder();
			bootstrapBuilder.addHeaderNavigation(navigationElements, 0);
			html.append(bootstrapBuilder.toString());
			html.append("<br><br>");
			html.append("<div class=\"container\">");
			html.append(form.toString());
			html.append("</div>");

			this.browser.setBodyHtml("<style>header{opacity:0.7;}</style>"
					+ html.toString());
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

	private final Map<ICode, Image> codeImages = new HashMap<ICode, Image>();

	private URI getCodeImage(ICode code) {
		if (!this.codeImages.containsKey(code)) {
			Image image = new Image(Display.getCurrent(), 16, 16);
			CodeColors info = new CodeColors(code.getColor());
			GC gc = new GC(image);
			gc.setAlpha(128);
			PaintUtils.drawRoundedRectangle(gc, image.getBounds(),
					info.getBackgroundColor(), info.getBorderColor());
			gc.dispose();
			this.codeImages.put(code, image);
		}

		return ImageUtils.createUriFromImage(this.codeImages.get(code));
	}

	private String createCodeLinks(List<ICode> codes) {
		List<String> html = new ArrayList<String>();
		for (ICode code : codes) {
			html.add("<a href=\"" + code.getUri() + "\"><img src=\""
					+ this.getCodeImage(code) + "\"/>");
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
