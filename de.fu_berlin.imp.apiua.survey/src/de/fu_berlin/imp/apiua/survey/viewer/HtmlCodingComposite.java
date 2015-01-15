package de.fu_berlin.imp.apiua.survey.viewer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.widgets.browser.extended.extensions.BrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;

public class HtmlCodingComposite extends Composite implements
		ISelectionProvider {

	private static final IUriPresenterService PRESENTER_SERVICE = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	private List<ISelectionChangedListener> selectionChangedListeners = new LinkedList<>();
	private ISelection selection = null;
	private BootstrapBrowser browser;

	public HtmlCodingComposite(Composite parent, int style, String uri) {
		super(parent, style);
		this.setLayout(new FillLayout());

		this.browser = new BootstrapBrowser(this, SWT.NONE,
				new BrowserExtension[] { new BrowserExtension("custom",
						"return window.custominit;", Arrays.asList(BrowserUtils
								.getFile(
										HtmlCodingComposite.class,
										HtmlCodingComposite.class
												.getSimpleName() + ".js")),
						Arrays.asList(BrowserUtils.getFileUrl(
								HtmlCodingComposite.class,
								HtmlCodingComposite.class.getSimpleName()
										+ ".css")), null) {
					@Override
					public String getVerificationScript() {
						return super.getVerificationScript();
					};
				} }) {
		};
		this.browser.deactivateNativeMenu();
		this.browser.addFocusListener(new IFocusListener() {
			@Override
			public void focusLost(IElement element) {
				HtmlCodingComposite.this.selection = null;
			}

			@Override
			public void focusGained(IElement element) {
				if (Arrays.asList(element.getClasses()).contains("codeable")) {
					URI selectedUri = new URI(element.getAttribute("id"));
					System.err.println(selectedUri);
					HtmlCodingComposite.this.selection = new StructuredSelection(
							selectedUri);
					HtmlCodingComposite.this
							.fireSelectionChanged(new SelectionChangedEvent(
									HtmlCodingComposite.this,
									HtmlCodingComposite.this.selection));
				}
			}
		});

		PRESENTER_SERVICE.enable(this.browser,
				new ISubjectInformationProvider<Control, URI>() {
					private URI hovered = null;

					private final IAnkerListener ankerListener = new AnkerAdapter() {
						@Override
						public void ankerHovered(IAnker anker, boolean entered) {
							URI uri = null;
							try {
								uri = new URI(anker.getAttribute("id"));
							} catch (Exception e) {

							}

							if (uri != null && entered) {
								hovered = uri;
							} else {
								hovered = null;
							}
						}
					};

					@Override
					public void register(Control subject) {
						HtmlCodingComposite.this.browser
								.addAnkerListener(this.ankerListener);
					}

					@Override
					public void unregister(Control subject) {
						HtmlCodingComposite.this.browser
								.removeAnkerListener(this.ankerListener);
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

		this.browser
				.addDisposeListener((DisposeListener) e -> PRESENTER_SERVICE
						.disable(this.browser));

		this.browser.open(uri, 60000);
	}

	public void setCodeMarkup(int fragment, String html) {
		this.browser.run("setCodeMarkup(" + fragment + ", "
				+ (html != null ? "\"" + Browser.escape(html) + "\"" : "null")
				+ ");");
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			List<URI> uris = SelectionUtils.getAdaptableObjects(selection,
					URI.class);
			for (URI uri : uris) {
				this.browser.scrollTo(new ISelector.IdSelector(uri.toString()));
			}
		}
	}

	/**
	 * Notifies any selection changed listeners that the viewer's selection has
	 * changed. Only listeners registered at the time this method is called are
	 * notified.
	 *
	 * @param event
	 *            a selection changed event
	 *
	 * @see ISelectionChangedListener#selectionChanged
	 */
	protected void fireSelectionChanged(final SelectionChangedEvent event) {
		for (int i = 0; i < this.selectionChangedListeners.size(); ++i) {
			final ISelectionChangedListener l = this.selectionChangedListeners
					.get(i);
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.selectionChangedListeners.remove(listener);
	}
}
