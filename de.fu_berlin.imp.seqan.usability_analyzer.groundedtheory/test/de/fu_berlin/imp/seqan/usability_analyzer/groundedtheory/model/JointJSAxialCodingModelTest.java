package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

public class JointJSAxialCodingModelTest {

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyURI() throws Exception {
		new JointJSAxialCodingModel(null, "{}");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidJSON() throws Exception {
		new JointJSAxialCodingModel(new URI("test://test"), "abc");
	}

	@Test
	public void test() throws Exception {
		URI uri = new URI("test://test");
		String json = "{\"title\": \"Test\", \"cells\":[{\"type\":\"html.Element\",\"position\":{\"x\":0,\"y\":35},\"size\":{\"width\":130,\"height\":30},\"angle\":0,\"id\":\"sua://code1\",\"title\":\"my box\",\"content\":\"<ul><li>jkjk</li></ul>\",\"z\":0,\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":107.55000000000001,\"y\":150},\"size\":{\"width\":130,\"height\":30},\"angle\":0,\"id\":\"sua://code2\",\"title\":\"my box233333\",\"z\":1,\"content\":\"XN dskjd sdkds dskdsdjks dskj \",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"sua://code1\"},\"target\":{\"id\":\"sua://code2\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"my_label\"}}}],\"id\":\"sua://link1\",\"smooth\":true,\"permanent\":true,\"z\":2,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":200},\"size\":{\"width\":300,\"height\":100},\"angle\":0,\"id\":\"sua://code3\",\"title\":\"my box233333\",\"z\":3,\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"sua://code3\"},\"target\":{\"id\":\"sua://code2\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Label2\"}}}],\"id\":\"sua://link2\",\"smooth\":true,\"z\":4,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"sua://code3\"},\"target\":{\"x\":1000,\"y\":250},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"I'm pointing to nowhere\"}}}],\"id\":\"sua://link3\",\"smooth\":true,\"z\":5,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}}]}";

		JointJSAxialCodingModel model = new JointJSAxialCodingModel(uri, json);

		assertEquals("Test", model.getTitle());
		model.setTitle("New Title");
		assertEquals("New Title", model.getTitle());

		URI code1 = new URI("sua://code1");
		URI code2 = new URI("sua://code2");
		URI code3 = new URI("sua://code3");
		assertEquals(Arrays.asList(code1, code2, code3), model.getCodes());

		// contains only 1 non-permanent link
		assertEquals(2, model.getLinks().size());

		ILink link1 = model.getLinks().get(0);
		assertEquals("Label2", link1.getTitle());
		assertEquals(new ILink.NodeEndpoint(code3.toString()),
				link1.getSource());
		assertEquals(new ILink.NodeEndpoint(code2.toString()),
				link1.getTarget());

		ILink link2 = model.getLinks().get(1);
		assertEquals("I'm pointing to nowhere", link2.getTitle());
		assertEquals(new ILink.NodeEndpoint(code3.toString()),
				link2.getSource());
		assertEquals(new ILink.CoordinateEndpoint(1000, 250), link2.getTarget());
	}
}
