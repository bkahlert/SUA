package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.gt.DoclogLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecordList;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class DoclogContentProvider extends URIContentProvider<List<Doclog>> {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogContentProvider.class);

	private Viewer viewer;

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		private boolean isResponsible(List<URI> uris) {
			return URIUtils.filterByResource(uris,
					DoclogLocatorProvider.DOCLOG_NAMESPACE).size() > 0;
		}

		@Override
		public void codesAdded(List<ICode> code) {
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			if (this.isResponsible(uris)) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			if (this.isResponsible(uris)) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
		}

		@Override
		public void codeDeleted(ICode code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}
	};

	public DoclogContentProvider() {
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			Object[] objects = ((Collection<?>) inputElement).toArray();
			/*
			 * If the list contains only one element and this element is a list
			 * return the mentioned child list. This way we save one hierarchy
			 * level (= ID level).
			 */
			if (objects.length == 1 && objects[0] instanceof Doclog) {
				objects = ((Doclog) objects[0]).getDoclogRecords().toArray();
			}

			for (int i = 0; i < objects.length; i++) {
				URI uri = (URI) Platform.getAdapterManager().getAdapter(
						objects[i], URI.class);
				if (uri != null) {
					objects[i] = uri;
				}
			}
			return objects;
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		ILocatable locatable = null;
		try {
			locatable = this.locatorService.resolve((URI) element, null).get();
		} catch (Exception e) {
			LOGGER.error("Error getting parent of " + element);
		}

		if (locatable instanceof DoclogRecord) {
			return ((DoclogRecord) locatable).getDoclog().getUri();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object uri) {
		ILocatable locatable = null;
		try {
			locatable = this.locatorService.resolve((URI) uri, null).get();
		} catch (Exception e) {
			LOGGER.error("Error getting parent of " + locatable);
		}

		if (locatable instanceof Doclog) {
			return ((Doclog) locatable).getDoclogRecords().size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		ILocatable locatable = null;
		try {
			locatable = this.locatorService.resolve((URI) parentElement, null)
					.get();
		} catch (Exception e) {
			LOGGER.error("Error getting parent of " + parentElement);
		}

		if (locatable instanceof Doclog) {
			DoclogRecordList doclogRecords = ((Doclog) locatable)
					.getDoclogRecords();
			if (doclogRecords != null) {
				URI[] uris = new URI[doclogRecords.size()];
				for (int i = 0; i < doclogRecords.size(); i++) {
					uris[i] = doclogRecords.get(i).getUri();
				}
				return uris;
			} else {
				return new Object[0];
			}
		}
		return new Object[0];
	}
}
