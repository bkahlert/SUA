package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import org.junit.Assert;
import org.junit.Test;

public class SerializationUtilsTest {

	@Test
	public void testSerializeAndDeserialize() {
		String[] array = new String[] { "abc", "§&52-_#*", "\"Ó" };

		String serializedArray = SerializationUtils.serialize(array);
		Assert.assertNotNull(serializedArray);

		String[] desiralizedArray = (String[]) SerializationUtils
				.deserialize(serializedArray);
		Assert.assertNotNull(desiralizedArray);

		Assert.assertEquals(array.length, desiralizedArray.length);
		for (int i = 0, j = array.length; i < j; i++) {
			Assert.assertEquals(array[i], desiralizedArray[i]);
		}
	}
}
