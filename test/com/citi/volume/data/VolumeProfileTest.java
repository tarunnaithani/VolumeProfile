package com.citi.volume.data;

import com.citi.volume.data.exception.VolumeProfileValidationException;
import com.citi.volume.util.ConstantUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class VolumeProfileTest {

    private static long time(String s){
        return ConstantUtils.extractDateTime(s).getTime();
    }
    private static final VolumeProfile TEST_VOLUME_PROFILE = new VolumeProfile("TEST_SYMBOL", Arrays.stream(new Bucket[]{
            new Bucket(time("24/08/2025 09:00:00"), time("24/08/2025 09:01:00"), 25, BucketType.Auction),
            new Bucket(time("24/08/2025 09:01:00"), time("24/08/2025 09:02:00"), 25, BucketType.Continuous),
            new Bucket(time("24/08/2025 09:02:00"), time("24/08/2025 09:03:00"), 20, BucketType.Continuous),
            new Bucket(time("24/08/2025 09:03:00"), time("24/08/2025 09:04:00"), 30, BucketType.Auction),
            new Bucket(time("24/08/2025 09:04:00"), time("24/08/2025 09:05:00"), 30, BucketType.Continuous),
            new Bucket(time("24/08/2025 09:05:00"), time("24/08/2025 09:06:00"), 30, BucketType.Continuous),
    }).toList()
    );

    private static VolumeProfile cumulativeVolumeProfile(String startTime, String endTime){
        return TEST_VOLUME_PROFILE.getCumulativeVolumeProfile(time(startTime), time(endTime));
    }

    private static double targetPercentForTimePeriod(String startTime, String endTime){
        return TEST_VOLUME_PROFILE.getTargetPercentForTimePeriod(time(startTime), time(endTime));
    }

    @Test
    public void testVolumeProfileCreation() {
        long now = 1756025143766L;
        VolumeProfile volumeProfile = new VolumeProfile("TEST_SYMBOL", Arrays.stream(new Bucket[]{
                new Bucket(time("24/08/2025 09:00:00"), time("24/08/2025 09:01:00"), 25, BucketType.Auction),
                new Bucket(time("24/08/2025 09:01:00"), time("24/08/2025 09:02:00"), 25, BucketType.Continuous),
                new Bucket(time("24/08/2025 09:02:00"), time("24/08/2025 09:03:00"), 20, BucketType.Continuous),
                new Bucket(time("24/08/2025 09:03:00"), time("24/08/2025 09:04:00"), 30, BucketType.Continuous),
        }).toList()
        );
        assertEquals("TEST_SYMBOL", volumeProfile.getSymbol());
        assertEquals(4, volumeProfile.getBuckets().size());
    }

    @Test
    public void testVolumeProfileCreationAsserts() {
        long now = 1756025143766L;

        Exception exception = assertThrows(VolumeProfileValidationException.class,
                () -> new VolumeProfile("TEST_SYMBOL", Arrays.stream(new Bucket[]{
                        new Bucket(time("24/08/2025 09:00:00"), time("24/08/2025 09:01:00"), 1, BucketType.Auction),
                        new Bucket(time("24/08/2025 09:01:00"), time("24/08/2025 09:02:00"), 1, BucketType.Continuous),
                        new Bucket(time("24/08/2025 09:02:00"), time("24/08/2025 09:03:00"), 1, BucketType.Continuous),
                        new Bucket(time("24/08/2025 09:03:00"), time("24/08/2025 09:04:00"), 1, BucketType.Continuous),
                }).toList()
                ).validate(true)

        );
        assertEquals("Total percentages in all buckets do not total to 100.0,4.0", exception.getMessage());

        exception = assertThrows(VolumeProfileValidationException.class,
                () -> new VolumeProfile("TEST_SYMBOL", Arrays.stream(new Bucket[]{
                        new Bucket(time("24/08/2025 09:00:00"), time("24/08/2025 09:01:00"), 1, BucketType.Auction),
                        new Bucket(time("24/08/2025 09:00:00") , time("24/08/2025 09:01:00"), 1, BucketType.Continuous),
                        new Bucket(time("24/08/2025 09:02:00"), time("24/08/2025 09:03:00"), 1, BucketType.Continuous),
                        new Bucket(time("24/08/2025 09:03:00"), time("24/08/2025 09:04:00"), 1, BucketType.Continuous),
                }).toList()
                )

        );
        assertEquals("Overlapping buckets found in Volume Profile,Bucket{startTime=09:00:00, endTime=09:01:00, percentOfDayVolume=1.0, bucketType=Continuous}", exception.getMessage());

    }

    @Test
    public void whenStartTimeEndTimeCoverEntireVolumeProfile(){
        VolumeProfile cumulativeVolumeProfile = cumulativeVolumeProfile("24/08/2025 09:00:00", "24/08/2025 09:06:00");
        assertEquals("start time and end time covering entire volume profile should return all buckets", 6, cumulativeVolumeProfile.getBuckets().size());
        assertEquals("""
                VolumeProfile:TEST_SYMBOL
                [Bucket{startTime=09:00:00, endTime=09:01:00, percentOfDayVolume=25.0, bucketType=Auction}]
                [Bucket{startTime=09:01:00, endTime=09:02:00, percentOfDayVolume=25.0, bucketType=Continuous}]
                [Bucket{startTime=09:02:00, endTime=09:03:00, percentOfDayVolume=20.0, bucketType=Continuous}]
                [Bucket{startTime=09:03:00, endTime=09:04:00, percentOfDayVolume=30.0, bucketType=Auction}]
                [Bucket{startTime=09:04:00, endTime=09:05:00, percentOfDayVolume=30.0, bucketType=Continuous}]
                [Bucket{startTime=09:05:00, endTime=09:06:00, percentOfDayVolume=30.0, bucketType=Continuous}]
                """, cumulativeVolumeProfile.toMultiLineString());

        assertEquals(160.0, targetPercentForTimePeriod("24/08/2025 09:00:00", "24/08/2025 09:06:00"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenStartTimeBeforeAnyBucketTime(){
        VolumeProfile cumulativeVolumeProfile = cumulativeVolumeProfile("24/08/2025 08:59:00", "24/08/2025 09:00:00");
        assertEquals("""
                VolumeProfile:TEST_SYMBOL
                """,cumulativeVolumeProfile.toMultiLineString());

        assertEquals(0.0, targetPercentForTimePeriod("24/08/2025 08:59:00", "24/08/2025 09:00:00"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenStartTimeEndTimeAreSame(){
        VolumeProfile cumulativeVolumeProfile = cumulativeVolumeProfile( "24/08/2025 09:01:00",  "24/08/2025 09:01:00");
        assertEquals("start time and end time same should return 0 buckets",0, cumulativeVolumeProfile.getBuckets().size());
    }

    @Test
    public void whenStartTimeEndTimeCoverAuctionOnly(){
        VolumeProfile subVolumeProfile = cumulativeVolumeProfile("24/08/2025 09:00:00", "24/08/2025 09:01:00");
        assertEquals(1, subVolumeProfile.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:00:00, endTime=09:01:00, percentOfDayVolume=25.0, bucketType=Auction}""", subVolumeProfile.getBuckets().getFirst().toString());

        assertEquals(25.0, targetPercentForTimePeriod("24/08/2025 09:00:00", "24/08/2025 09:01:00"), ConstantUtils.EPILSON);
    }


    @Test
    public void whenStartTimeIsBetweenAuctionBucket(){
        VolumeProfile cumulativeVolumeProfile =  cumulativeVolumeProfile( "24/08/2025 09:00:30",  "24/08/2025 09:03:00");
        assertEquals("start time between auction bucket should return entire auction bucket", 3, cumulativeVolumeProfile.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:00:00, endTime=09:01:00, percentOfDayVolume=25.0, bucketType=Auction}""", cumulativeVolumeProfile.getBuckets().getFirst().toString());

        assertEquals(70.0, targetPercentForTimePeriod("24/08/2025 09:00:30",  "24/08/2025 09:03:00"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenStartTimeIsAtEndOfAuctionBucket(){
        VolumeProfile cumulativeVolumeProfile =  cumulativeVolumeProfile( "24/08/2025 09:01:00",  "24/08/2025 09:03:00");
        assertEquals(2, cumulativeVolumeProfile.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:01:00, endTime=09:02:00, percentOfDayVolume=25.0, bucketType=Continuous}""", cumulativeVolumeProfile.getBuckets().getFirst().toString());

        assertEquals(45.0, targetPercentForTimePeriod("24/08/2025 09:01:00",  "24/08/2025 09:03:00"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenStartTimeIsMiddleOfContinuousBucket(){
        VolumeProfile cumulativeVolumeProfile =  cumulativeVolumeProfile( "24/08/2025 09:01:30",  "24/08/2025 09:03:00");
        assertEquals(2, cumulativeVolumeProfile.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:01:30, endTime=09:02:00, percentOfDayVolume=12.5, bucketType=Continuous}""", cumulativeVolumeProfile.getBuckets().getFirst().toString());

        assertEquals(32.5, targetPercentForTimePeriod("24/08/2025 09:01:30",  "24/08/2025 09:03:00"), ConstantUtils.EPILSON);


    }

    @Test
    public void whenStartTimeEndTimeCoverOnlyOneBucket(){
        VolumeProfile cumulativeVolumeProfile =  cumulativeVolumeProfile( "24/08/2025 09:02:00",  "24/08/2025 09:03:00");
        assertEquals("start time and end time covering exact one bucket should return 1 bucket",1, cumulativeVolumeProfile.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:02:00, endTime=09:03:00, percentOfDayVolume=20.0, bucketType=Continuous}""", cumulativeVolumeProfile.getBuckets().getFirst().toString());

        assertEquals(20.0, targetPercentForTimePeriod("24/08/2025 09:02:00",  "24/08/2025 09:03:00"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenEndTimeIsMiddleOfAuctionBucket(){
        VolumeProfile cumulativeVolumeProfile =  cumulativeVolumeProfile( "24/08/2025 09:02:00",  "24/08/2025 09:03:30");
        assertEquals("End time in middle of auction bucket should return entire Auction bucket",2, cumulativeVolumeProfile.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:03:00, endTime=09:04:00, percentOfDayVolume=30.0, bucketType=Auction}""", cumulativeVolumeProfile.getBuckets().getLast().toString());

        assertEquals(20.0, targetPercentForTimePeriod("24/08/2025 09:02:00",  "24/08/2025 09:03:00"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenStartTimeEndTimeCoverTwoBuckets(){
        VolumeProfile cumulativeVolumeProfile7 =  cumulativeVolumeProfile( "24/08/2025 09:02:00",  "24/08/2025 09:04:00");
        assertEquals(2, cumulativeVolumeProfile7.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:02:00, endTime=09:03:00, percentOfDayVolume=20.0, bucketType=Continuous}""", cumulativeVolumeProfile7.getBuckets().getFirst().toString());
        assertEquals("""
                Bucket{startTime=09:03:00, endTime=09:04:00, percentOfDayVolume=30.0, bucketType=Auction}""", cumulativeVolumeProfile7.getBuckets().getLast().toString());

        assertEquals(50.0, targetPercentForTimePeriod("24/08/2025 09:02:00",  "24/08/2025 09:04:00"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenEndTimeIsMiddleOfContinuousBucket(){
        VolumeProfile cumulativeVolumeProfile8 =  cumulativeVolumeProfile( "24/08/2025 09:02:00",  "24/08/2025 09:04:30");
        assertEquals(3, cumulativeVolumeProfile8.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:02:00, endTime=09:03:00, percentOfDayVolume=20.0, bucketType=Continuous}""", cumulativeVolumeProfile8.getBuckets().getFirst().toString());
        assertEquals("""
                Bucket{startTime=09:04:00, endTime=09:04:30, percentOfDayVolume=15.0, bucketType=Continuous}""", cumulativeVolumeProfile8.getBuckets().getLast().toString());

        assertEquals(65.0, targetPercentForTimePeriod("24/08/2025 09:02:00",  "24/08/2025 09:04:30"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenStartTimeEndTimeAreInMiddleOfDifferentBucket(){
        VolumeProfile cumulativeVolumeProfile9 =  cumulativeVolumeProfile( "24/08/2025 09:01:30",  "24/08/2025 09:04:30");
        assertEquals(4, cumulativeVolumeProfile9.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:01:30, endTime=09:02:00, percentOfDayVolume=12.5, bucketType=Continuous}""", cumulativeVolumeProfile9.getBuckets().getFirst().toString());
        assertEquals("""
                Bucket{startTime=09:04:00, endTime=09:04:30, percentOfDayVolume=15.0, bucketType=Continuous}""", cumulativeVolumeProfile9.getBuckets().getLast().toString());

        assertEquals(77.5, targetPercentForTimePeriod("24/08/2025 09:01:30",  "24/08/2025 09:04:30"), ConstantUtils.EPILSON);
    }

    @Test
    public void whenStartTimeEndTimeAreInMiddleOfSameBucket(){
        VolumeProfile cumulativeVolumeProfile9 =  cumulativeVolumeProfile( "24/08/2025 09:01:30",  "24/08/2025 09:01:40");
        assertEquals(1, cumulativeVolumeProfile9.getBuckets().size());
        assertEquals("""
                Bucket{startTime=09:01:30, endTime=09:01:40, percentOfDayVolume=4.166666666666666, bucketType=Continuous}""", cumulativeVolumeProfile9.getBuckets().getFirst().toString());

        assertEquals(4.166666, targetPercentForTimePeriod("24/08/2025 09:01:30",  "24/08/2025 09:01:40"), ConstantUtils.EPILSON);
    }

}
