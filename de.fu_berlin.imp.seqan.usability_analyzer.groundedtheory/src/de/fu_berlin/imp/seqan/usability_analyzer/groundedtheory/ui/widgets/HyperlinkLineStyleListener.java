package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;

public class HyperlinkLineStyleListener implements LineStyleListener {

	private static final Pattern URL_PATTERN = Pattern.compile("\\w+://.*");

	@Override
	public void lineGetStyle(LineStyleEvent event) {
		List<StyleRange> styles = new ArrayList<StyleRange>();
		int num = event.lineOffset;
		for (String part : event.lineText.split(" ")) {
			if (URL_PATTERN.matcher(part).matches()) {
				StyleRange s = new StyleRange(num, part.length(), null, null);
				s.underline = true;
				s.underlineStyle = SWT.UNDERLINE_LINK;
				s.data = part;
				styles.add(s);
			}
			num += part.length() + 1;
		}
		event.styles = styles.toArray(new StyleRange[0]);
	}
}
