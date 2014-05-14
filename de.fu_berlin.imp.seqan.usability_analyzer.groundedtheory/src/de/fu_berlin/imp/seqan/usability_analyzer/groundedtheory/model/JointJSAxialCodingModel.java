package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

import com.bkahlert.nebula.utils.JSONUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

public class JointJSAxialCodingModel implements IAxialCodingModel {

	private static final long serialVersionUID = 1L;
	private final URI uri;
	private String rawJson;
	private HashMap<String, Object> json;

	private List<URI> codes;
	private String title;
	private List<ILink> links;

	public JointJSAxialCodingModel(URI uri, String json) {
		try {
			Assert.isNotNull(uri);
		} catch (AssertionFailedException e) {
			throw new IllegalArgumentException(e);
		}
		this.uri = uri;
		this.update(json);
	}

	@SuppressWarnings("unchecked")
	public void update(String json) {
		try {
			this.json = (HashMap<String, Object>) JSONUtils.parseJson(json);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		this.rawJson = json;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public String getTitle() {
		if (this.title == null) {
			Object title = this.json.get("title");
			this.title = title != null ? title.toString() : null;
		}
		return this.title;
	}

	public void setTitle(String title) {
		try {
			Assert.isNotNull(title);
		} catch (AssertionFailedException e) {
			throw new IllegalArgumentException(e);
		}
		this.title = title;
	}

	@SuppressWarnings("unchecked")
	private List<HashMap<String, Object>> getCells() {
		return (List<HashMap<String, Object>>) this.json.get("cells");
	}

	@Override
	public List<URI> getCodes() {
		if (this.codes == null) {
			this.codes = new ArrayList<URI>();
			for (HashMap<String, Object> cell : this.getCells()) {
				if (!cell.get("type").toString().contains("link")) {
					JointJSCode code = new JointJSCode(cell);
					this.codes.add(code.getUri());
				}
			}
		}
		return this.codes;
	}

	@Override
	public List<ILink> getLinks() {
		if (this.links == null) {
			this.links = new ArrayList<ILink>();
			for (HashMap<String, Object> cell : this.getCells()) {
				if (cell.get("type").toString().contains("link")
						&& (cell.get("permanent") == null || !cell
								.get("permanent").toString().equals("true"))) {
					JointJSLink link = new JointJSLink(cell);
					this.links.add(link);
				}
			}
		}
		return this.links;
	}

	@Override
	public String serialize() {
		return this.rawJson;
	}

}
