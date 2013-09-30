package de.fu_berlin.imp.seqan.usability_analyzer.survey.viewer;

import java.util.List;

public class BootstrapBuilder {

	public static class NavigationElement {
		private final String caption;
		private final String link;

		public NavigationElement(String caption, String link) {
			super();
			this.caption = caption;
			this.link = link;
		}

		/**
		 * @return the caption
		 */
		public String getCaption() {
			return this.caption;
		}

		/**
		 * @return the link
		 */
		public String getLink() {
			return this.link;
		}

	}

	private StringBuilder value;

	public BootstrapBuilder() {
		this.value = new StringBuilder();
	}

	public void addRaw(String html) {
		this.value.append(html);
	}

	public void addHeaderNavigation(List<NavigationElement> navigationElements,
			int activeIndex) {
		this.value
				.append("<header class=\"navbar navbar-fixed-top navbar-inverse\" role=\"banner\">");
		this.value.append("<div class=\"container\">");
		this.value.append("<div class=\"navbar-header\">");
		this.value
				.append("<button class=\"navbar-toggle\" type=\"button\" data-toggle=\"collapse\" data-target=\".bs-navbar-collapse\">");
		this.value.append("<span class=\"sr-only\">Toggle navigation</span>");
		this.value.append("<span class=\"icon-bar\"></span>");
		this.value.append("<span class=\"icon-bar\"></span>");
		this.value.append("<span class=\"icon-bar\"></span>");
		this.value.append("</button>");
		this.value.append("</div>");
		this.value
				.append("<nav class=\"collapse navbar-collapse bs-navbar-collapse\" role=\"navigation\">");
		this.value.append("<ul class=\"nav navbar-nav\">");
		for (int i = 0, m = navigationElements.size(); i < m; i++) {
			this.value.append("<li");
			if (i == activeIndex) {
				this.value.append(" class=\"active\"");
			}
			this.value.append("><a href=\""
					+ navigationElements.get(i).getLink() + "\">");
			this.value.append(navigationElements.get(i).getCaption());
			this.value.append("</a></li>");
		}
		this.value.append("</ul>");
		this.value.append("</nav>");
		this.value.append("</div>");
		this.value.append("</header>");
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

}
