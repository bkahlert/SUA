package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.naming.OperationNotSupportedException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.itemlist.ItemList;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;

public class AxialCodingView extends ViewPart {

	static final Logger LOGGER = Logger.getLogger(AxialCodingView.class);

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView";

	private SashForm axialCodingCompositesContainer;
	private final List<AxialCodingComposite> axialCodingComposites = new ArrayList<AxialCodingComposite>();

	public AxialCodingView() {

	}

	@Override
	public void dispose() {
		for (AxialCodingComposite axialCodingComposite : this.axialCodingComposites) {
			System.err.println(axialCodingComposite.isDisposed() + ", "
					+ axialCodingComposite.getJointjs().isDisposed() + ", "
					+ axialCodingComposite.getJointjs().getBrowser());
		}
		this.saveAll();
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());
		ItemList modelList = new AxialCodingViewModelList(parent, SWT.NONE);
		modelList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.axialCodingCompositesContainer = new SashForm(parent, SWT.VERTICAL);
		this.axialCodingCompositesContainer.setLayoutData(new GridData(
				SWT.FILL, SWT.FILL, true, true));

		List<URI> lastOpenedModels = new SUAGTPreferenceUtil()
				.getLastOpenedAxialCodingModels();
		if (lastOpenedModels.size() > 0) {
			final URI uri = lastOpenedModels.get(0);
			final Future<Void> success = this.open(uri);
			ExecUtils.nonUIAsyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						success.get();
					} catch (Exception e) {
						LOGGER.error("Error opening "
								+ IAxialCodingModel.class.getSimpleName() + " "
								+ uri);
					}
				}
			});
		}
	}

	private void activateMenu() {
		// this.getSite().setSelectionProvider(this.axialCodingComposite);
		//
		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuManager.createContextMenu(this.axialCodingComposites
				.get(0));
		this.getSite().registerContextMenu(menuManager,
				this.axialCodingComposites.get(0));
		this.axialCodingComposites.get(0).setMenu(menu);
	}

	public Future<Void> open(final URI... uris) {
		this.saveAll();
		this.disposeAll();

		for (int i = 0; i < uris.length; i++) {
			final AxialCodingComposite axialCodingComposite = new AxialCodingComposite(
					this.axialCodingCompositesContainer, SWT.BORDER);
			axialCodingComposite.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			axialCodingComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					true, true));
			this.axialCodingComposites.add(axialCodingComposite);
		}
		this.axialCodingCompositesContainer.layout(true);
		this.activateMenu();
		// ExecUtils.asyncExec(new Runnable() {
		// @Override
		// public void run() {
		// axialCodingComposite.getJointjs().setBodyHtml(
		// "<p>Hello World</p>");
		// axialCodingComposite.getJointjs().setBackground(
		// Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		// System.err.println("set");
		// }
		//
		// }, 1000);

		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (int i = 0; i < uris.length; i++) {
					AxialCodingView.this.axialCodingComposites.get(i)
							.open(uris[i]).get();
				}
				return null;
			}
		});
	}

	/**
	 * Saves the currently opened {@link URI}s. The {@link Control}s's content
	 * is immediately saved, so it is save to call this method while disposition
	 * takes place. The axial coding models are written on the disk not until
	 * {@link Future#isDone()} returns true.
	 * 
	 * @return
	 * 
	 * @return
	 */
	public Future<Void> saveAll() {
		final Map<URI, Future<Void>> success = new HashMap<URI, Future<Void>>();
		for (final AxialCodingComposite axialCodingComposite : this.axialCodingComposites) {
			success.put(axialCodingComposite.getOpenedURI(),
					axialCodingComposite.save());
		}
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Exception ex = null;
				for (URI uri : success.keySet()) {
					try {
						success.get(uri).get();
						LOGGER.info("Successfully saved "
								+ IAxialCodingModel.class.getSimpleName() + " "
								+ uri);
					} catch (Exception e) {
						if (ex == null) {
							ex = e;
						}
						LOGGER.error(
								"Error saving "
										+ IAxialCodingModel.class
												.getSimpleName() + " " + uri, e);
					}
				}
				if (ex != null) {
					throw ex;
				}
				return null;
			}
		});
	}

	/**
	 * Disposes the all opened {@link AxialCodingComposite}s without saving.
	 */
	private void disposeAll() {
		for (Iterator<AxialCodingComposite> iterator = this.axialCodingComposites
				.iterator(); iterator.hasNext();) {
			AxialCodingComposite axialCodingComposite = iterator.next();
			axialCodingComposite.dispose();
			iterator.remove();
		}
		Assert.isTrue(this.axialCodingCompositesContainer.getChildren().length == 0);
	}

	public List<URI> getOpenedURIs() {
		List<URI> uris = new ArrayList<URI>(this.axialCodingComposites.size());
		for (AxialCodingComposite axialCodingComposite : this.axialCodingComposites) {
			uris.add(axialCodingComposite.getOpenedURI());
		}
		return uris;
	}

	@Override
	public void setFocus() {
		if (this.axialCodingComposites != null
				&& this.axialCodingComposites.size() > 0) {
			this.axialCodingComposites.get(0).setFocus();
		}
	}

	/**
	 * Removes the given {@link ICode}s from the currently loaded
	 * {@link IAxialCodingModel}s.
	 * 
	 * @param codes
	 */
	public void remove(List<ICode> codes) {
		for (AxialCodingComposite axialCodingComposite : this.axialCodingComposites) {
			axialCodingComposite.remove(codes);
		}
	}

	public void autoLayoutFocussedACM() {
		throw new RuntimeException(new OperationNotSupportedException());
	}

	public void zoomOutFocussedACM() {
		throw new RuntimeException(new OperationNotSupportedException());
	}

	public void zoomInFocussedACM() {
		throw new RuntimeException(new OperationNotSupportedException());
	}

}
