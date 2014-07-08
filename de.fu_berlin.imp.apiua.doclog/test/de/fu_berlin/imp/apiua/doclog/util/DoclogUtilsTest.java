package de.fu_berlin.imp.apiua.doclog.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.bkahlert.nebula.screenshots.webpage.IFormContainingWebpage.IFieldFill;
import com.bkahlert.nebula.widgets.browser.extended.ISelector;

import de.fu_berlin.imp.apiua.doclog.util.DoclogUtils;

public class DoclogUtilsTest {

	private void testFieldFill(String expectedName, String expectedValue,
			IFieldFill fieldFill) {
		String expectedSelector = new ISelector.FieldSelector(expectedName)
				.toString();
		assertEquals(expectedSelector, fieldFill.getFieldSelector().toString());
		assertEquals(expectedValue, fieldFill.getFieldValue());
	}

	@Test
	public void testGetPossibleFieldNames() throws Exception {
		String param = "abc-def-hij--klmnop-";

		List<IFieldFill> possibleFieldFills = new ArrayList<IFieldFill>();
		for (IFieldFill possibleFieldFill : DoclogUtils
				.getPossibleFields(param)) {
			possibleFieldFills.add(possibleFieldFill);
		}

		assertEquals(possibleFieldFills.size(), 5);
		this.testFieldFill("abc", "def-hij--klmnop-", possibleFieldFills.get(0));
		this.testFieldFill("abc-def", "hij--klmnop-", possibleFieldFills.get(1));
		this.testFieldFill("abc-def-hij", "-klmnop-", possibleFieldFills.get(2));
		this.testFieldFill("abc-def-hij-", "klmnop-", possibleFieldFills.get(3));
		this.testFieldFill("abc-def-hij--klmnop", "", possibleFieldFills.get(4));
	}
}
