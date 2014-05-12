package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.LocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class SelectiveCodingContentProvider extends
		URIContentProvider<ICodeService> implements
		com.bkahlert.nebula.viewer.jointjs.JointJSContentProvider {

	private final IImportanceService importanceService = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);

	private ICodeService codeService;
	private Viewer viewer;

	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					false);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					false);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					false);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					false);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					false);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					false);
		}

		@Override
		public void memoAdded(URI uri) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					true);
		}

		@Override
		public void memoModified(URI uri) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					true);
		}

		@Override
		public void memoRemoved(URI uri) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					true);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					true);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					true);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			ViewerUtils.refresh(SelectiveCodingContentProvider.this.viewer,
					true);
		}
	};

	private final IImportanceServiceListener importanceServiceListener = new IImportanceServiceListener() {
		@Override
		public void importanceChanged(Set<URI> uris, Importance importance) {
			ViewerUtils.update(SelectiveCodingContentProvider.this.viewer,
					uris.toArray(new URI[0]), null);
		}
	};

	public SelectiveCodingContentProvider() {
		this.importanceService
				.addImportanceServiceListener(this.importanceServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, ICodeService oldInput,
			ICodeService newInput, Object ignore) throws Exception {
		this.viewer = viewer;

		if (this.codeService != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}
		this.codeService = null;

		if (newInput != null) {
			this.codeService = newInput;
			this.codeService.addCodeServiceListener(this.codeServiceListener);
		} else {
			if (this.codeService != null) {
				this.codeService
						.removeCodeServiceListener(this.codeServiceListener);
				this.codeService = null;
			}
		}
	}

	@Override
	public void dispose() {
		this.importanceService
				.removeImportanceServiceListener(this.importanceServiceListener);
		if (this.codeService != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}
	}

	@Override
	public String getId(Object element) {
		if (element instanceof URI) {
			return element.toString();
		}
		return null;
	}

	@Override
	public URI[] getTopLevelElements(ICodeService input) throws Exception {
		List<ICode> codes = input.getTopLevelCodes();
		URI[] uris = new URI[codes.size()];
		for (int i = 0; i < codes.size(); i++) {
			uris[i] = codes.get(i).getUri();
		}
		return uris;
	}

	@Override
	public URI getParent(URI uri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null).get();
		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;
			ICode parent = this.codeService.getParent(code);
			if (parent != null) {
				return parent.getUri();
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) throws Exception {
		return this.getChildren(uri).length > 0;
	}

	@Override
	public URI[] getChildren(URI parentUri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(parentUri, null)
				.get();
		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;

			// TODO delete
			ICode parent = this.codeService.getParent(code);
			if (parent != null) {
				return new URI[0];
			}

			List<ICode> children = this.codeService.getChildren(code);
			if (children != null) {
				URI[] uris = new URI[children.size()];
				for (int i = 0; i < uris.length; i++) {
					uris[i] = children.get(i).getUri();
				}
				return uris;
			}
		}
		return new URI[0];
	}

	@Override
	public Object[] getLinks(Object element) {
		return new Object[0];
	}
}