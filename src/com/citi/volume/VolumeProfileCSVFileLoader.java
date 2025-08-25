package com.citi.volume;

import com.citi.volume.data.Bucket;
import com.citi.volume.data.BucketType;
import com.citi.volume.data.VolumeProfile;
import com.citi.volume.data.exception.VolumeProfileLoadingException;
import com.citi.volume.util.ConstantUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Used to load volume profile from CSV file
 */
public class VolumeProfileCSVFileLoader {
    public static final String COMMA_DELIMITER = ",";

    /**
     * to load volume profile for a symbol
     *
     * @param symbol for which volume profile is being loaded
     * @param filename name of file which contains volume profile
     * @return Loaded VolumeProfile object
     * @throws VolumeProfileLoadingException when errors occurs during file load
     */
    public VolumeProfile loadVolumeProfile(String symbol, String filename) throws VolumeProfileLoadingException {
        List<Bucket> buckets = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) // line starting with # will be considered as comment
                    continue;
                buckets.add(extractBucketFromCSVString(line));
            }
            VolumeProfile volumeProfile = new VolumeProfile(symbol, buckets);
            volumeProfile.validate(true);
            return volumeProfile;
        } catch (FileNotFoundException fileNotfound) {
            throw new VolumeProfileLoadingException("Cannot read, " + filename, fileNotfound);
        } catch (IOException e) {
            throw new VolumeProfileLoadingException("Problem while reading, " + filename, e);
        }
    }

    /**
     * extracts bucket data from comma separated string
     * @param line string with bucket data
     * @return Bucket object with values read from string
     */
    private Bucket extractBucketFromCSVString(String line) {
        String[] values = line.split(COMMA_DELIMITER);
        if (values.length < 4)
            throw new VolumeProfileLoadingException("Invalid record found," + line);

        Date startTime = ConstantUtils.extractTime(values[0]);
        Date endTime = ConstantUtils.extractTime(values[1]);
        double percentOfDayVolume = ConstantUtils.extractDouble(values[2]);
        BucketType bucketType = ConstantUtils.extractBucketType(values[3]);
        validateDataRead(line, startTime, endTime, percentOfDayVolume, bucketType);

        return new Bucket(startTime.getTime(), endTime.getTime(), percentOfDayVolume, bucketType);
    }

    /**
     * Validates whether entries read are good to create bucket or not
     *
     * @param line string from which data is read
     * @param startTime bucket start time extracted from line
     * @param endTime bucket end time extracted from line
     * @param percentOfDayVolume percent of day volume for the bucket extracted from line
     * @param bucketType type of bucket extracted from line
     */
    private static void validateDataRead(String line, Date startTime, Date endTime, double percentOfDayVolume, BucketType bucketType) {
        if (startTime == null)
            throw new VolumeProfileLoadingException("Invalid start time in line, " + line);
        if (endTime == null)
            throw new VolumeProfileLoadingException("Invalid end time in line, " + line);
        if (startTime.getTime() >= endTime.getTime())
            throw new VolumeProfileLoadingException("Start time is before or same as end time in line, " + line);
        if (percentOfDayVolume < 0)
            throw new VolumeProfileLoadingException("Invalid percentOfDayVolume in line, " + line);
        if (bucketType == null)
            throw new VolumeProfileLoadingException("Invalid Bucket Type in line, " + line);
    }
}
