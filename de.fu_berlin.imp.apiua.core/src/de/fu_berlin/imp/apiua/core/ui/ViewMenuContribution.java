package de.fu_berlin.imp.apiua.core.ui;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.osgi.framework.Bundle;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.apiua.core.util.WorkbenchUtils;

public class ViewMenuContribution extends ContributionItem {

	private static final Logger LOGGER = Logger
			.getLogger(ViewMenuContribution.class);

	public ViewMenuContribution() {
		this.init();
	}

	public ViewMenuContribution(String id) {
		super(id);
		this.init();
	}

	private void init() {

	}

	@Override
	public void fill(Menu menu, int index) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry
				.getExtensionPoint("org.eclipse.ui.views");
		if (point == null) {
			return;
		}

		for (IConfigurationElement configurationElement : point
				.getConfigurationElements()) {
			// filter only views
			if (!configurationElement.getName().toLowerCase().contains("view")) {
				continue;
			}

			final String clazz = configurationElement.getAttribute("class");
			final String id = configurationElement.getAttribute("id");
			final String name = configurationElement.getAttribute("name");
			final String icon = configurationElement.getAttribute("icon");

			if (clazz == null || id == null || name == null) {
				continue;
			}

			if (!id.startsWith("de.fu_berlin.imp.apiua")
					|| name.toLowerCase().contains("editor only")) {
				continue;
			}

			MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index);
			menuItem.setText(name);
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ExecUtils.nonUIAsyncExec(new Runnable() {
						@Override
						public void run() {
							WorkbenchUtils.getView(id);
						}
					});
				}
			});

			if (icon != null) {
				try {
					String bundleName = configurationElement
							.getDeclaringExtension().getContributor().getName();
					Bundle bundle = Platform.getBundle(bundleName);
					URL url = bundle.getEntry(icon);
					File file = new File(FileLocator.resolve(url).toURI());
					final Image image = new Image(Display.getCurrent(),
							new ImageData(file.getAbsolutePath()));
					menuItem.setImage(image);
					menuItem.addDisposeListener(new DisposeListener() {
						@Override
						public void widgetDisposed(DisposeEvent e) {
							image.dispose();
						}
					});
				} catch (Exception e) {
					LOGGER.warn("Error loading icon for " + id);
				}
			}
		}
	}
}
