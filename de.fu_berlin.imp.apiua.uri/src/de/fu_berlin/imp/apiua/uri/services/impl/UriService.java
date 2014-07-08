package de.fu_berlin.imp.apiua.uri.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.uri.model.IUri;
import de.fu_berlin.imp.apiua.uri.services.IUriService;
import de.fu_berlin.imp.apiua.uri.services.IUriServiceListener;
import de.fu_berlin.imp.apiua.uri.services.UriDoesNotExistException;
import de.fu_berlin.imp.apiua.uri.services.UriServiceException;

public class UriService implements IUriService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(UriService.class);

	private final File file;
	private final File backupFile;
	private HashSet<IUri> uris;

	private final List<IUriServiceListener> uriServiceListeners = new ArrayList<IUriServiceListener>();

	public UriService(File file) throws IOException {
		this.file = file;
		this.backupFile = new File(file.getAbsolutePath() + ".bak");
		this.load();
	}

	@SuppressWarnings("unchecked")
	private void load() throws IOException {
		if (this.file.length() == 0) {
			this.uris = new HashSet<IUri>();
		} else {
			FileInputStream fileIn = new FileInputStream(this.file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			try {
				this.uris = (HashSet<IUri>) in.readObject();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} finally {
				in.close();
				fileIn.close();
			}
		}
	}

	private void save() throws IOException {
		File file = File.createTempFile(SUACorePreferences.URI_SCHEME, ".uris");

		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this.uris);
		out.close();
		fileOut.close();

		if (this.backupFile.exists()) {
			this.backupFile.delete();
		}
		if (this.file.exists()) {
			FileUtils.moveFile(this.file, this.backupFile);
		}
		FileUtils.moveFile(file, this.file);
	}

	@Override
	public Set<IUri> getUris() {
		return Collections.unmodifiableSet(this.uris);
	}

	@Override
	public void addUri(IUri uri) throws UriServiceException {
		this.uris.add(uri);
		for (IUriServiceListener listener : this.uriServiceListeners) {
			listener.urisAdded(new HashSet<IUri>(Arrays.asList(uri)));
		}
		try {
			this.save();
		} catch (IOException e) {
			throw new UriServiceException(e);
		}
	}

	@Override
	public void replaceUri(IUri oldUri, IUri newUri) throws UriServiceException {
		if (oldUri == null || newUri == null) {
			throw new UriServiceException(new IllegalArgumentException(
					"Arguments must not be null"));
		}

		ILocatorService locatorService;
		ICodeService codeService;
		try {
			locatorService = (ILocatorService) PlatformUI.getWorkbench()
					.getService(ILocatorService.class);
			codeService = (ICodeService) PlatformUI.getWorkbench().getService(
					ICodeService.class);
		} catch (Exception e) {
			locatorService = null;
			codeService = null;
		}

		if (this.uris.contains(oldUri)) {
			if (locatorService != null) {
				locatorService.uncache(oldUri.getUri());
			}
			this.uris.remove(oldUri);
			this.uris.add(newUri);

			if (codeService != null) {
				try {
					codeService.reattachAndSave(oldUri.getUri(),
							newUri.getUri());
				} catch (CodeServiceException e) {
					throw new UriServiceException(e);
				}
			}

			for (IUriServiceListener listener : this.uriServiceListeners) {
				listener.uriReplaced(oldUri, newUri);
			}
		} else {
			throw new UriDoesNotExistException(oldUri);
		}

		try {
			this.save();
		} catch (IOException e) {
			throw new UriServiceException(e);
		}
	}

	@Override
	public void removeUri(IUri uri) throws UriServiceException {
		this.removeUris(Arrays.asList(uri));
	}

	@Override
	public void removeUris(Collection<IUri> uris) throws UriServiceException {
		this.uris.removeAll(uris);
		for (IUriServiceListener listener : this.uriServiceListeners) {
			listener.urisRemoved(new HashSet<IUri>(uris));
		}
		// TODO also remove codes and memos
		try {
			this.save();
		} catch (IOException e) {
			throw new UriServiceException(e);
		}
	}

	@Override
	public void addUriServiceListener(IUriServiceListener uriServiceListener) {
		this.uriServiceListeners.add(uriServiceListener);
	}

	@Override
	public void removeUriServiceListener(IUriServiceListener uriServiceListener) {
		this.uriServiceListeners.remove(uriServiceListener);
	}

}
