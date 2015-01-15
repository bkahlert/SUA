package de.fu_berlin.imp.apiua.core.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.impl.FileData;
import de.fu_berlin.imp.apiua.core.model.data.impl.WrappingData;

public class HtmlDocument extends WrappingData implements IHtmlDocument {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(HtmlDocument.class);

	private URI uri;
	private String cssQuery;
	private Map<URI, String> fields = new HashMap<>();
	private File modifiedFile;

	public HtmlDocument(File file, String cssQuery, String baseUri) {
		this(new FileData(file), cssQuery, baseUri);
	}

	public HtmlDocument(IData data, String cssQuery, String baseUri) {
		super(data);

		this.cssQuery = cssQuery;
		try {
			URLCodec urlCodec = new URLCodec();
			this.uri = new URI(baseUri + "/" + urlCodec.encode(data.getName()));
			Document document = Jsoup.parse(data.read());
			int i = 0;
			for (Element element : document.select(cssQuery)) {
				String fieldUri = this.uri + "/" + urlCodec.encode(cssQuery)
						+ "/" + i;
				List<Element> children = element.children();
				Element a = element
						.prepend(
								"<a id=\""
										+ fieldUri
										+ "\" href=\"#"
										+ fieldUri
										+ "\" class=\"codeable\" tabindex=\""
										+ i
										+ "\" data-fragment=\""
										+ i
										+ "\" draggable=\"true\" data-dnd-mime=\"text/plain\" data-dnd-data=\""
										+ fieldUri + "\"></a>").child(0);

				for (Element child : children) {
					if (!child.equals(a)) {
						a.appendChild(child);
					}
				}

				// DND handle
				// a.append(" <span draggable=\"true\" data-dnd-mime=\"text/plain\" data-dnd-data=\""
				// + fieldUri
				// +
				// "\"><span class=\"glyphicon glyphicon-share-alt no_click\" style=\"height: 1.5em; line-height: 1.4em;\"></span></span>");

				this.fields.put(new URI(fieldUri), element.text());
				i++;
			}
			this.modifiedFile = File.createTempFile(
					HtmlDocument.class.getSimpleName(), ".html");
			this.modifiedFile.deleteOnExit();
			IOUtils.write(document.outerHtml(), new FileWriter(
					this.modifiedFile));
		} catch (EncoderException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public String getCssQuery() {
		return this.cssQuery;
	}

	@Override
	public Map<URI, String> getFields() {
		return new HashMap<>(this.fields);
	}

	@Override
	public File getMarkedUpFile() {
		return this.modifiedFile;
	}

}
