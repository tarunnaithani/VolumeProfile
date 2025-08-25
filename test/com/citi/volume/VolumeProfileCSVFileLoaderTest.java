package com.citi.volume;

import static org.junit.Assert.*;

import com.citi.volume.data.VolumeProfile;
import com.citi.volume.data.exception.VolumeProfileLoadingException;
import static com.citi.volume.util.ConstantUtils.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VolumeProfileCSVFileLoaderTest {

	@Test
	public void testLoadVolumeProfileErrors() {
		VolumeProfileCSVFileLoader volumeProfileCSVFileLoader = new VolumeProfileCSVFileLoader();
        Exception fileNotFound = assertThrows(VolumeProfileLoadingException.class,
                () -> volumeProfileCSVFileLoader.loadVolumeProfile( "HK","NoFile.csv")
        );
        assertEquals("Cannot read, NoFile.csv", fileNotFound.getMessage());

        Exception invalid = assertThrows(VolumeProfileLoadingException.class,
                () -> volumeProfileCSVFileLoader.loadVolumeProfile( "HK","error_file.csv")
        );
        assertEquals("Invalid record found,,09:30:00,2.03355251671", invalid.getMessage());

        Exception errorStartTime = assertThrows(VolumeProfileLoadingException.class,
                () -> volumeProfileCSVFileLoader.loadVolumeProfile( "HK","error_starttime.csv")
        );
        assertEquals("Invalid start time in line, ,09:30:00,2.03355251671,Auction", errorStartTime.getMessage());

        Exception errorEndTime = assertThrows(VolumeProfileLoadingException.class,
                () -> volumeProfileCSVFileLoader.loadVolumeProfile( "HK","error_endtime.csv")
        );
        assertEquals("Invalid end time in line, 09:30:00,,2.03355251671,Auction", errorEndTime.getMessage());

        Exception errorStartEndTime = assertThrows(VolumeProfileLoadingException.class,
                () -> volumeProfileCSVFileLoader.loadVolumeProfile( "HK","error_start_endtime.csv")
        );
        assertEquals("Start time is before or same as end time in line, 09:30:00,09:30:00,2.03355251671,Auction", errorStartEndTime.getMessage());

        Exception errorPercentOfDayVolume = assertThrows(VolumeProfileLoadingException.class,
                () -> volumeProfileCSVFileLoader.loadVolumeProfile( "HK","error_percentofdayvolume.csv")
        );
        assertEquals("Invalid percentOfDayVolume in line, 09:00:00,09:30:00,-1,Auction", errorPercentOfDayVolume.getMessage());

        Exception errorBucketType = assertThrows(VolumeProfileLoadingException.class,
                () -> volumeProfileCSVFileLoader.loadVolumeProfile( "HK","error_buckettype.csv")
        );
        assertEquals("Invalid Bucket Type in line, 09:00:00,09:30:00,10,AUC", errorBucketType.getMessage());
	}

    @Test
    public void testLoadVolumeProfile() {
        VolumeProfileCSVFileLoader volumeProfileCSVFileLoader = new VolumeProfileCSVFileLoader();

        VolumeProfile volumeProfile = volumeProfileCSVFileLoader.loadVolumeProfile( "9988_HK","9988_HK.csv");
        assertEquals("9988_HK", volumeProfile.getSymbol());
        assertEquals(399, volumeProfile.getBuckets().size());

    }

    @Test
    public void testGetCumulativeVolumeProfile() {
        VolumeProfileCSVFileLoader volumeProfileCSVFileLoader = new VolumeProfileCSVFileLoader();

        VolumeProfile volumeProfile = volumeProfileCSVFileLoader.loadVolumeProfile( "9988_HK","9988_HK.csv");
        assertEquals("9988_HK", volumeProfile.getSymbol());
        assertEquals(399, volumeProfile.getBuckets().size());

        VolumeProfile subProfile = volumeProfile.getCumulativeVolumeProfile(extractTime("09:00:00").getTime(), extractTime("09:35:00").getTime());
        assertEquals("""
                VolumeProfile:9988_HK
                [Bucket{startTime=09:00:00, endTime=09:30:00, percentOfDayVolume=2.03355251671, bucketType=Auction}]
                [Bucket{startTime=09:30:00, endTime=09:31:00, percentOfDayVolume=1.16360116965, bucketType=Continuous}]
                [Bucket{startTime=09:31:00, endTime=09:32:00, percentOfDayVolume=0.49560281387, bucketType=Continuous}]
                [Bucket{startTime=09:32:00, endTime=09:33:00, percentOfDayVolume=0.48900394145, bucketType=Continuous}]
                [Bucket{startTime=09:33:00, endTime=09:34:00, percentOfDayVolume=0.26972891007, bucketType=Continuous}]
                [Bucket{startTime=09:34:00, endTime=09:35:00, percentOfDayVolume=0.36321293599, bucketType=Continuous}]
                """,subProfile.toMultiLineString());

    }

    @Test
    public void testGetCumulativeVolumeProfileMidInterval() {
        VolumeProfileCSVFileLoader volumeProfileCSVFileLoader = new VolumeProfileCSVFileLoader();

        VolumeProfile volumeProfile = volumeProfileCSVFileLoader.loadVolumeProfile( "9988_HK","9988_HK.csv");
        assertEquals("9988_HK", volumeProfile.getSymbol());
        assertEquals(399, volumeProfile.getBuckets().size());

        VolumeProfile subProfile = volumeProfile.getCumulativeVolumeProfile(extractTime("09:15:00").getTime(), extractTime("09:34:30").getTime());
        assertEquals("""
                VolumeProfile:9988_HK
                [Bucket{startTime=09:00:00, endTime=09:30:00, percentOfDayVolume=2.03355251671, bucketType=Auction}]
                [Bucket{startTime=09:30:00, endTime=09:31:00, percentOfDayVolume=1.16360116965, bucketType=Continuous}]
                [Bucket{startTime=09:31:00, endTime=09:32:00, percentOfDayVolume=0.49560281387, bucketType=Continuous}]
                [Bucket{startTime=09:32:00, endTime=09:33:00, percentOfDayVolume=0.48900394145, bucketType=Continuous}]
                [Bucket{startTime=09:33:00, endTime=09:34:00, percentOfDayVolume=0.26972891007, bucketType=Continuous}]
                [Bucket{startTime=09:34:00, endTime=09:34:30, percentOfDayVolume=0.181606467995, bucketType=Continuous}]
                """,subProfile.toMultiLineString());

    }

    @Test
    public void testGetCumulativeVolumeProfileSingleBucketAuction() {
        VolumeProfileCSVFileLoader volumeProfileCSVFileLoader = new VolumeProfileCSVFileLoader();

        VolumeProfile volumeProfile = volumeProfileCSVFileLoader.loadVolumeProfile( "9988_HK","9988_HK.csv");
        assertEquals("9988_HK", volumeProfile.getSymbol());
        assertEquals(399, volumeProfile.getBuckets().size());

        VolumeProfile subProfile = volumeProfile.getCumulativeVolumeProfile(extractTime("09:00:00").getTime(), extractTime("09:30:00").getTime());
        assertEquals(1,subProfile.getBuckets().size());
        assertEquals("""
                VolumeProfile:9988_HK
                [Bucket{startTime=09:00:00, endTime=09:30:00, percentOfDayVolume=2.03355251671, bucketType=Auction}]
                """,subProfile.toMultiLineString());

    }

    @Test
    public void testGetCumulativeVolumeProfileSingleBucketContinuous() {
        VolumeProfileCSVFileLoader volumeProfileCSVFileLoader = new VolumeProfileCSVFileLoader();

        VolumeProfile volumeProfile = volumeProfileCSVFileLoader.loadVolumeProfile( "9988_HK","9988_HK.csv");
        assertEquals("9988_HK", volumeProfile.getSymbol());
        assertEquals(399, volumeProfile.getBuckets().size());

        VolumeProfile subProfile = volumeProfile.getCumulativeVolumeProfile(extractTime("09:30:00").getTime(), extractTime("09:30:30").getTime());
        assertEquals(1,subProfile.getBuckets().size());
        assertEquals("""
                VolumeProfile:9988_HK
                [Bucket{startTime=09:30:00, endTime=09:30:30, percentOfDayVolume=0.581800584825, bucketType=Continuous}]
                """,subProfile.toMultiLineString());

    }

    @Test
    public void testGetCumulativeVolumeProfileSingleBucketContinuousAndLunch() {
        VolumeProfileCSVFileLoader volumeProfileCSVFileLoader = new VolumeProfileCSVFileLoader();

        VolumeProfile volumeProfile = volumeProfileCSVFileLoader.loadVolumeProfile( "9988_HK","9988_HK.csv");
        assertEquals("9988_HK", volumeProfile.getSymbol());
        assertEquals(399, volumeProfile.getBuckets().size());

        VolumeProfile subProfile = volumeProfile.getCumulativeVolumeProfile(extractTime("11:59:00").getTime(), extractTime("13:01:00").getTime());
        assertEquals(62,subProfile.getBuckets().size());
    }

}
