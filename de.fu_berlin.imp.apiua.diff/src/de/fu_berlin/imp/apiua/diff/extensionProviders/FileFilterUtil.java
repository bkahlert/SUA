package de.fu_berlin.imp.apiua.diff.extensionProviders;

import java.io.FileFilter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class FileFilterUtil {

	private static interface FileFilterRunnable {
		public void run(IFileFilterListener fileFilterListener);
	}

	private static void notify(final FileFilterRunnable fileFilterRunnable) {
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.apiua.diff.filefilter");
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof IFileFilterListener) {
					fileFilterRunnable.run((IFileFilterListener) o);
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void notifyFileFilterAdded(final FileFilter fileFilter) {
		notify(new FileFilterRunnable() {
			@Override
			public void run(IFileFilterListener fileFilterListener) {
				fileFilterListener.fileFilterAdded(fileFilter);
			}
		});
	}

	public static void notifyFileFilterRemoved(final FileFilter fileFilter) {
		notify(new FileFilterRunnable() {
			@Override
			public void run(IFileFilterListener fileFilterListener) {
				fileFilterListener.fileFilterRemoved(fileFilter);
			}
		});
	}
}
