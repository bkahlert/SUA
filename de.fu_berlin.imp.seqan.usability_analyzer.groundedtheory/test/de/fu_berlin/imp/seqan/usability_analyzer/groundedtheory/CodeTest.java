package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;

public class CodeTest {
	@SuppressWarnings("serial")
	@Test
	public void testCalculateId() {
		Assert.assertEquals(Long.MIN_VALUE,
				Code.calculateId(new HashSet<Long>() {
					{
					}
				}));

		Assert.assertEquals(5l, Code.calculateId(new HashSet<Long>() {
			{
				this.add(4l);
			}
		}));

		Assert.assertEquals(5l, Code.calculateId(new HashSet<Long>() {
			{
				this.add(3l);
				this.add(4l);
			}
		}));

		Assert.assertEquals(5l, Code.calculateId(new HashSet<Long>() {
			{
				this.add(4l);
				this.add(3l);
			}
		}));
	}
}
