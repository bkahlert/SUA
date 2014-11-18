package de.fu_berlin.imp.apiua.groundedtheory.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.bkahlert.nebula.widgets.jointjs.JointJSLink;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel.IURIEndpoint;

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
		String json = "{\"title\": \"Test\", \"cells\":[{\"type\":\"html.Element\",\"position\":{\"x\":0,\"y\":35},\"size\":{\"width\":130,\"height\":30},\"angle\":0,\"id\":\"apiua://code1\",\"title\":\"my box\",\"content\":\"<ul><li>jkjk</li></ul>\",\"z\":0,\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":107.55000000000001,\"y\":150},\"size\":{\"width\":130,\"height\":30},\"angle\":0,\"id\":\"apiua://code2\",\"title\":\"my box233333\",\"z\":1,\"content\":\"XN dskjd sdkds dskdsdjks dskj \",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua://code1\"},\"target\":{\"id\":\"apiua://code2\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"my_label\"}}}],\"id\":\"apiua://link1\",\"smooth\":true,\"permanent\":true,\"z\":2,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":200},\"size\":{\"width\":300,\"height\":100},\"angle\":0,\"id\":\"apiua://code3\",\"title\":\"my box233333\",\"z\":3,\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua://code3\"},\"target\":{\"id\":\"apiua://code2\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Label2\"}}}],\"id\":\"apiua://link2\",\"smooth\":true,\"z\":4,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua://code3\"},\"target\":{\"x\":1000,\"y\":250},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"I'm pointing to nowhere\"}}}],\"id\":\"apiua://link3\",\"smooth\":true,\"z\":5,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}}]}";

		JointJSAxialCodingModel model = new JointJSAxialCodingModel(uri, json);

		assertEquals("Test", model.getTitle());

		URI code1 = new URI("apiua://code1");
		URI code2 = new URI("apiua://code2");
		URI code3 = new URI("apiua://code3");
		assertEquals(Arrays.asList(code1, code2, code3), model.getCodes());

		// contains only 1 non-permanent link
		assertEquals(2, model.getRelations().size());

		URI link1 = model.getRelations().get(0);
		assertEquals("Label2", model.getTitle(link1));
		assertEquals(code3, ((IURIEndpoint) model.getSource(link1)).getURI());
		assertEquals(code2, ((IURIEndpoint) model.getTarget(link1)).getURI());

		URI link2 = model.getRelations().get(1);
		assertEquals("I'm pointing to nowhere", model.getTitle(link2));
		assertEquals(code3, ((IURIEndpoint) model.getSource(link2)).getURI());
		assertEquals(new JointJSLink.CoordinateEndpoint(1000, 250),
				model.getTarget(link2));
	}
}
