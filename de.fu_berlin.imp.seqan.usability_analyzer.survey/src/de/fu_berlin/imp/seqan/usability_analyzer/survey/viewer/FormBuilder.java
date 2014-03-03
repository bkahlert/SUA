package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer;

public class FormBuilder {

	private final StringBuilder value;

	public FormBuilder() {
		this.value = new StringBuilder();
	}

	public void addRaw(String html) {
		this.value.append(html);
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param caption
	 * @param value
	 * @param tabIndex
	 *            if >= 0 the markup is changed so the field container can have
	 *            focus. The provided id is also stored in the container
	 *            attribute <code>data-focus-id</code>.
	 */
	public void addStaticField(String id, String name, String caption,
			String value, int tabIndex) {
		this.value.append("<div class=\"form-group\"");
		if (tabIndex >= 0) {
			this.value.append(" tabindex=\"0\" data-focus-id=\"" + id + "\"");
		}
		this.value.append(">");
		this.value
				.append("<label for=\"" + id
						+ "\" class=\"col-lg-2 control-label\">" + caption
						+ "</label>");

		this.value.append("<div class=\"col-lg-8\">");
		this.value.append("<div class=\"form-control-static\"><a id=\"" + id
				+ "\" name=\"" + name + "\"></a>" + value + "</div>");
		this.value.append("</div>");
		this.value.append("</div>");
	}

	public void addStaticField(String idAndName, String caption, String value,
			int tabIndex) {
		this.addStaticField(idAndName, idAndName, caption, value, tabIndex);
	}

	@Override
	public String toString() {
		return "<form class=\"form-horizontal\" role=\"form\">" + this.value
				+ "</form>";
	}

}
