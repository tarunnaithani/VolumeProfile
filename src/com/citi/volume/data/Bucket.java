package com.citi.volume.data;

import com.citi.volume.data.exception.VolumeProfileValidationException;
import com.citi.volume.util.ConstantUtils;

import java.util.Date;

/**
 * Stores data for each interval for a volume profile
 */
public class Bucket {
    /**
     * Start time for the bucket
     */
    private final long startTime;

    /**
     * End time for the bucket
     */
    private final long endTime;

    /**
     * Percent of day volume done within the bucket
     */
    private final double percentOfDayVolume;

    /**
     * Bucket type whether Auction or Continuous
     */
    private final BucketType bucketType;

    public Bucket(long startTime, long endTime, double percentOfDayVolume, BucketType bucketType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.percentOfDayVolume = percentOfDayVolume;
        this.bucketType = bucketType;
        validate();
    }

    public Bucket(Bucket bucket) {
        this.startTime = bucket.getStartTime();;
        this.endTime = bucket.getEndTime();
        this.percentOfDayVolume = bucket.getPercentOfDayVolume();
        this.bucketType = bucket.getBucketType();
        validate();
    }

    /**
     * Validates if bucket entries are good.
     *
     */
    public void validate() {
        if (startTime >= endTime)
            throw new VolumeProfileValidationException("Start time cannot be equal or later than end time, " + this);
        if (bucketType == null)
            throw new VolumeProfileValidationException("Bucket type cannot be null, " + this);
        if (percentOfDayVolume < 0)
            throw new VolumeProfileValidationException("Percent of day volume cannot be less than 0, " + this);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getPercentOfDayVolume() {
        return percentOfDayVolume;
    }

    public BucketType getBucketType() {
        return bucketType;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "startTime=" + ConstantUtils.printTime(startTime) +
                ", endTime=" + ConstantUtils.printTime(endTime) +
                ", percentOfDayVolume=" + percentOfDayVolume +
                ", bucketType=" + bucketType +
                '}';
    }
}
