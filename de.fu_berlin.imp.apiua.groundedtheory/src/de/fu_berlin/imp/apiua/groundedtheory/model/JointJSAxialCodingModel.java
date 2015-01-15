package de.fu_berlin.imp.apiua.groundedtheory.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

import com.bkahlert.nebula.widgets.jointjs.JointJSCell;
import com.bkahlert.nebula.widgets.jointjs.JointJSElement;
import com.bkahlert.nebula.widgets.jointjs.JointJSLink;
import com.bkahlert.nebula.widgets.jointjs.JointJSLink.CoordinateEndpoint;
import com.bkahlert.nebula.widgets.jointjs.JointJSLink.IElementEndpoint;
import com.bkahlert.nebula.widgets.jointjs.JointJSLink.IEndpoint;
import com.bkahlert.nebula.widgets.jointjs.JointJSModel;

import de.fu_berlin.imp.apiua.core.model.URI;

public class JointJSAxialCodingModel implements IAxialCodingModel {

	public static interface IURIEndpoint extends IEndpoint {
		public URI getURI();
	}

	public static class URIEndpoint implements IURIEndpoint {
		private URI uri;

		public URIEndpoint(IElementEndpoint elementEndpoint) {
			this.uri = new URI(elementEndpoint.getElement());
		}

		@Override
		public URI getURI() {
			return this.uri;
		}
	}

	private static final long serialVersionUID = 1L;
	private final URI uri;
	private JointJSModel jointJsModel;

	private List<URI> codes;
	private final List<URI> codesNotInJson = new ArrayList<URI>();
	private List<URI> relations;

	public JointJSAxialCodingModel(URI uri, JointJSModel jointJsModel) {
		try {
			Assert.isNotNull(uri);
		} catch (AssertionFailedException e) {
			throw new IllegalArgumentException(e);
		}
		this.uri = uri;
		this.jointJsModel = jointJsModel;
	}

	public JointJSAxialCodingModel(URI uri, String json) {
		this(uri, new JointJSModel(json));
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public String getTitle() {
		return this.jointJsModel.getTitle();
	}

	public void setTitle(String title) {
		this.jointJsModel.setTitle(title);
	}

	@Override
	public URI getOrigin() {
		String uri = this.jointJsModel.getData("origin");
		if (uri != null) {
			return new URI(uri);
		}
		return null;
	}

	public void setOrigin(URI origin) {
		this.jointJsModel.setData("origin", origin != null ? origin.toString()
				: null);
	}

	@Override
	public String getTitle(URI uri) {
		String id = uri != null ? uri.toString() : null;
		JointJSCell cell = this.jointJsModel.getCell(id);
		if (cell instanceof JointJSElement) {
			return ((JointJSElement) cell).getTitle();
		} else if (cell instanceof JointJSLink) {
			return ((JointJSLink) cell).getTitle();
		}
		return null;
	}

	@Override
	public Object getAttribute(URI uri, String key) {
		String id = uri != null ? uri.toString() : null;
		JointJSCell cell = this.jointJsModel.getCell(id);
		return cell != null ? cell.getAttribute(key) : null;
	}

	public IEndpoint getEndpoint(URI uri, String endpointName) {
		Object endpointAttribute = this.getAttribute(uri, endpointName);
		@SuppressWarnings("unchecked")
		IEndpoint endpoint = JointJSLink
				.createEndpoint((HashMap<String, Object>) endpointAttribute);
		return endpoint instanceof CoordinateEndpoint ? (CoordinateEndpoint) endpoint
				: new URIEndpoint((IElementEndpoint) endpoint);
	}

	@Override
	public IEndpoint getSource(URI uri) {
		return this.getEndpoint(uri, "source");
	}

	@Override
	public IEndpoint getTarget(URI uri) {
		return this.getEndpoint(uri, "target");
	}

	@Override
	public List<URI> getCodes() {
		if (this.codes == null) {
			this.codes = new ArrayList<URI>();
			for (JointJSElement element : this.jointJsModel.getElements()) {
				String id = element.getId();
				URI uri = id != null ? new URI(id) : null;
				if (uri != null) {
					this.codes.add(uri);
				}
			}
		}
		List<URI> codes = new ArrayList<URI>();
		codes.addAll(this.codes);
		codes.addAll(this.codesNotInJson);
		return codes;
	}

	@Override
	public List<URI> getRelations() {
		if (this.relations == null) {
			this.relations = new ArrayList<URI>();
			for (JointJSLink link : this.jointJsModel.getLinks()) {
				String id = link.getId();
				URI uri = id != null ? new URI(id) : null;
				if (uri != null) {
					this.relations.add(uri);
				}
			}
		}
		return this.relations;
	}

	@Override
	public IAxialCodingModel createCopy(Map<String, Object> customize) {
		return new JointJSAxialCodingModel(this.uri,
				this.jointJsModel.createCopy(customize));
	}

	@Override
	public String serialize() {
		return this.jointJsModel.serialize();
	}

	@Override
	public String toString() {
		return JointJSAxialCodingModel.class.getSimpleName() + " \""
				+ this.getTitle() + "\": " + this.codes.size() + " codes, "
				+ this.relations.size() + " links";
	}

}
