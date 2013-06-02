package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DoclogUtilsTest {

	@Test
	public void testGetPossibleFieldNames() throws Exception {
		String param = "abc-def-hij--klmnop-";

		List<String> possibleFieldNames = new ArrayList<String>();
		for (String possibleFieldName : DoclogUtils
				.getPossibleFieldNames(param)) {
			possibleFieldNames.add(possibleFieldName);
		}

		assertEquals(possibleFieldNames.size(), 5);
		assertEquals("abc", possibleFieldNames.get(0));
		assertEquals("abc-def", possibleFieldNames.get(1));
		assertEquals("abc-def-hij", possibleFieldNames.get(2));
		assertEquals("abc-def-hij-", possibleFieldNames.get(3));
		assertEquals("abc-def-hij--klmnop", possibleFieldNames.get(4));

		assertEquals("def-hij--klmnop-",
				DoclogUtils.getFieldContent("abc", param));
		assertEquals("hij--klmnop-",
				DoclogUtils.getFieldContent("abc-def", param));
		assertEquals("-klmnop-",
				DoclogUtils.getFieldContent("abc-def-hij", param));
		assertEquals("klmnop-",
				DoclogUtils.getFieldContent("abc-def-hij-", param));
		assertEquals("",
				DoclogUtils.getFieldContent("abc-def-hij--klmnop", param));
	}
}
