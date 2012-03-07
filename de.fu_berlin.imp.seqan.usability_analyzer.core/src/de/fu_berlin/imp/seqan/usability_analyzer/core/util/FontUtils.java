package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import org.eclipse.swt.graphics.FontData;

public class FontUtils {
	public static FontData[] getModifiedFontData(FontData[] originalData,
			int additionalStyle) {
		FontData[] styleData = new FontData[originalData.length];
		for (int i = 0; i < styleData.length; i++) {
			FontData base = originalData[i];
			styleData[i] = new FontData(base.getName(), base.getHeight(),
					base.getStyle() | additionalStyle);
		}
		return styleData;
	}
}
