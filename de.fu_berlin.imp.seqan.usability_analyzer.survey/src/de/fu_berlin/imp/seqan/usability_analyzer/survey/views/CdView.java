package de.fu_berlin.imp.seqan.usability_analyzer.survey.views;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.extended.BootstrapEnabledBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.utils.CompletedFuture;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.DataServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.WizardUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.SurveyContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocument;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.model.cd.CDDocumentField;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.BootstrapBuilder;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.BootstrapBuilder.NavigationElement;
import de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer.FormBuilder;

public class CDView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.survey.views.CDView";

	public static final Logger LOGGER = Logger.getLogger(CDView.class);

	private IDataService dataService = (IDataService) PlatformUI.getWorkbench()
			.getService(IDataService.class);
	private IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		@Override
		public void dataDirectoriesLoaded(
				List<? extends IBaseDataContainer> dataContainers) {
			LOGGER.info("Refreshing " + CDView.class.getSimpleName());
			ExecutorUtil.asyncExec(new Runnable() {
				@Override
				public void run() {
					CDView.this.load(Activator.getDefault()
							.getSurveyContainer());
				}
			});
		}
	};

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	private BootstrapEnabledBrowserComposite view = null;

	public CDView() {
		this.dataService.addDataServiceListener(this.dataServiceListener);
	}

	@Override
	public void dispose() {
		this.dataService.removeDataServiceListener(this.dataServiceListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.view = new BootstrapEnabledBrowserComposite(parent, SWT.NONE);
		this.view.deactivateNativeMenu();
		this.view.setAllowLocationChange(true);
		this.view.openAboutBlank();

		this.view.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerHovered(IAnker anker, boolean entered) {
				System.err.println(anker);
			}

			@SuppressWarnings("serial")
			@Override
			public void ankerClicked(IAnker anker) {
				if (anker.getHref().startsWith("about:blank")) {
					return;
				}

				final AtomicReference<URI> uri = new AtomicReference<URI>();
				try {
					uri.set(new URI(anker.getHref()));
				} catch (URISyntaxException e) {
					LOGGER.error("Can't create URI to code from "
							+ anker.getHref());
					return;
				}
				WizardUtils.openAddCodeWizard(new ILocatable() {
					@Override
					public URI getUri() {
						return uri.get();
					}
				}, Utils.getFancyCodeColor());
			}
		});
	}

	protected void load(SurveyContainer surveyContainer) {

		List<NavigationElement> navigationElements = new ArrayList<NavigationElement>();

		FormBuilder form = new FormBuilder();
		for (CDDocument cdDocument : surveyContainer.getCDDocuments()) {
			String caption = cdDocument.getIdentifier().toString();

			navigationElements.add(new NavigationElement(caption, "#"
					+ cdDocument.getUri()));

			form.addRaw("<h2><a name=\""
					+ cdDocument.getUri()
					+ "\">"
					+ caption
					+ "</a> <a href=\""
					+ cdDocument.getUri()
					+ "\" class=\"btn btn-primary btn-sm\">Add Code...</a></h2>");
			for (CDDocumentField field : cdDocument) {
				form.addStaticField(
						field.getUri().toString(),
						field.getQuestion()
								+ " <a href=\""
								+ field.getUri()
								+ "\" class=\" btn btn-primary btn-xs\">Add Code...</a>",
						field.getAnswer());
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

		this.view.setBodyHtml(html.toString());
	}

	@Override
	public void setFocus() {
		this.view.setFocus();
	}

	public Future<ILocatable[]> open(ILocatable[] locatables,
			Callable<ILocatable[]> callable) {
		if (locatables.length > 0) {
			ILocatable locatable = locatables[0];
			this.view.openAnker(locatable.getUri().toString());
		}
		return new CompletedFuture<ILocatable[]>(new ILocatable[0], null);
	}

}
