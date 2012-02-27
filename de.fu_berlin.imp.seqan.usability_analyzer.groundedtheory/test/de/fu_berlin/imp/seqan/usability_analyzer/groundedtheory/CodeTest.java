package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;

public class CodeTest {
	@SuppressWarnings("serial")
	@Test
	public void testCalculateId() {
		Assert.assertEquals(Long.MIN_VALUE,
				Code.calculateId(new ArrayList<Long>() {
					{
					}
				}));

		Assert.assertEquals(5l, Code.calculateId(new ArrayList<Long>() {
			{
				add(4l);
			}
		}));

		Assert.assertEquals(5l, Code.calculateId(new ArrayList<Long>() {
			{
				add(3l);
				add(4l);
			}
		}));

		Assert.assertEquals(5l, Code.calculateId(new ArrayList<Long>() {
			{
				add(4l);
				add(3l);
			}
		}));
	}
}
