package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer;

public class FormBuilder {

	private StringBuilder value;

	public FormBuilder() {
		this.value = new StringBuilder();
	}

	public void addRaw(String html) {
		this.value.append(html);
	}

	public void addStaticField(String id, String name, String caption,
			String value) {
		this.value.append("<div class=\"form-group\">");
		this.value
				.append("<label for=\"" + id
						+ "\" class=\"col-lg-2 control-label\">" + caption
						+ "</label>");

		this.value.append("<div class=\"col-lg-8\">");
		this.value.append("<div class=\"form-control-static\">" + value
				+ "</div>");
		this.value.append("</div>");
		this.value.append("</div>");
	}

	public void addStaticField(String idAndName, String caption, String value) {
		this.addStaticField(idAndName, idAndName, caption, value);
	}

	@Override
	public String toString() {
		return "<form class=\"form-horizontal\" role=\"form\">" + this.value
				+ "</form>";
	}

}
