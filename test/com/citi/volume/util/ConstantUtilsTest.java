package com.citi.volume.util;

import com.citi.volume.data.BucketType;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConstantUtilsTest {

	@Test
	public void testStringNotEmpty() {
		assertTrue(ConstantUtils.stringNotEmpty("scsdc"));
        assertFalse(ConstantUtils.stringNotEmpty(null));
        assertFalse(ConstantUtils.stringNotEmpty(""));
        assertFalse(ConstantUtils.stringNotEmpty(" "));
	}

	@Test
	public void testExtractBucketType() {
		assertEquals(BucketType.Auction, ConstantUtils.extractBucketType("Auction"));
        assertEquals(BucketType.Continuous, ConstantUtils.extractBucketType("Continuous"));
        assertNull(ConstantUtils.extractBucketType("xyz"));
        assertNull(ConstantUtils.extractBucketType(null));
	}

    @Test
    public void testExtractDouble() {
        assertEquals(0.00002, ConstantUtils.extractDouble("0.00002"), 0.0000001);
        assertEquals(2.0, ConstantUtils.extractDouble("2.0"), 0.0000001);
        assertEquals(-1, ConstantUtils.extractDouble("xyz"), 0.0000001);
        assertEquals(-1, ConstantUtils.extractDouble(null), 0.0000001);
    }

    @Test
    public void testExtractDateTime() {
        assertNotNull(ConstantUtils.extractDateTime("22/08/2025 09:00:00"));
        assertNull(ConstantUtils.extractDateTime("22/08/2025 09:00"));
        assertNull(ConstantUtils.extractDateTime(""));
        assertNull(ConstantUtils.extractDateTime(null));
    }

    @Test
    public void equalDouble() {
        assertTrue(ConstantUtils.equalDouble(100.0, 100.000001));
        assertFalse(ConstantUtils.equalDouble(100.1, 100.000001));
        assertFalse(ConstantUtils.equalDouble(100.00001, 100.000001));
    }

}
