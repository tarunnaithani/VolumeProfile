package com.citi.volume.data;

import com.citi.volume.data.exception.VolumeProfileValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(JUnit4.class)
public class BucketTest {

    @Test
    public void testBucketCreation() {
        Bucket bucket = new Bucket(1, 2, 2.045, BucketType.Auction);
        assertEquals(1, bucket.getStartTime());
        assertEquals(2, bucket.getEndTime());
        assertEquals(2.045, bucket.getPercentOfDayVolume(), 0.000001);
        assertEquals(BucketType.Auction, bucket.getBucketType());

    }

    @Test
    public void testBucketCreationAsserts() {
        long now = 1756025143766L;

        Exception exception = assertThrows(VolumeProfileValidationException.class,
                () -> new Bucket(now, now, 1, BucketType.Auction)
        );
        assertEquals("Start time cannot be equal or later than end time, Bucket{startTime=16:45:43, endTime=16:45:43, percentOfDayVolume=1.0, bucketType=Auction}", exception.getMessage());

        exception = assertThrows(VolumeProfileValidationException.class,
                () -> new Bucket(now + 100000, now, 1, BucketType.Auction)
        );

        assertEquals("Start time cannot be equal or later than end time, Bucket{startTime=16:47:23, endTime=16:45:43, percentOfDayVolume=1.0, bucketType=Auction}", exception.getMessage());

        exception = assertThrows(VolumeProfileValidationException.class,
                () -> new Bucket(now, now + 60000, -1, BucketType.Auction)
        );

        assertEquals("Percent of day volume cannot be less than 0, Bucket{startTime=16:45:43, endTime=16:46:43, percentOfDayVolume=-1.0, bucketType=Auction}", exception.getMessage());

        exception = assertThrows(VolumeProfileValidationException.class,
                () -> new Bucket(1, 2, 1, null)
        );

        assertEquals("Bucket type cannot be null, Bucket{startTime=08:00:00, endTime=08:00:00, percentOfDayVolume=1.0, bucketType=null}", exception.getMessage());
    }

}
