package com.citi.volume.data;

import com.citi.volume.data.exception.VolumeProfileValidationException;
import com.citi.volume.util.ConstantUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores Volume Profile for a symbol
 */
public class VolumeProfile {
    /**
     * Symbol who's volume profile is stored
     */
    private final String symbol;
    private final List<Bucket> buckets;


    public VolumeProfile(String symbol, List<Bucket> buckets) {
        this.symbol = symbol;
        this.buckets = buckets;
        validate(false);
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    /**
     * Validates whether volume profile
     * Checks there should be no overlapping buckets.
     * For complete day profile checks if sum total percent of day volume is 100.0
     *
     * @param completeDayProfile if volume profile is for entire day's volume then runs additional checks
     */
    public void validate(boolean completeDayProfile) {
        double totalPercentageOfDayVolume = 0;
        long previousEndTime = 0;
        for (Bucket bucket : buckets) {
            if (bucket.getStartTime() < previousEndTime)
                throw new VolumeProfileValidationException("Overlapping buckets found in Volume Profile," + bucket);
            totalPercentageOfDayVolume += bucket.getPercentOfDayVolume();
            previousEndTime = bucket.getEndTime();
        }

        if (completeDayProfile && !ConstantUtils.equalDouble(totalPercentageOfDayVolume, 100.0)) {
            throw new VolumeProfileValidationException("Total percentages in all buckets do not total to 100.0," + totalPercentageOfDayVolume);
        }
    }

    /**
     * returns target percent of day volume expected to be traded within the start and end time
     *
     * @param startTime start time for time-period
     * @param endTime   end time for time-period
     * @return target percent of day volume expected to trade within the time period
     */
    public double getTargetPercentForTimePeriod(long startTime, long endTime) {
        VolumeProfile profile = getCumulativeVolumeProfile(startTime, endTime);
        double targetPercent = 0.0;
        for (Bucket bucket : profile.getBuckets()) {
            targetPercent += bucket.getPercentOfDayVolume();
        }
        return targetPercent;
    }

    /**
     * returns volume profile for the time period
     *
     * @param startTime when the time period starts
     * @param endTime   when the time period ends
     * @return Volume profile applicable for the time period
     */
    public VolumeProfile getCumulativeVolumeProfile(long startTime, long endTime) {
        List<Bucket> resultBuckets = new ArrayList<>();
        if (startTime < endTime) {
            int startIndex = bucketIndexOf(startTime, true);
            int endIndex = bucketIndexOf(endTime, false);
            if (startIndex != -1 && endIndex != -1) {
                // Single bucket has both start time and end time in it
                if (startIndex == endIndex) {
                    Bucket bucket = new Bucket(buckets.get(startIndex));
                    if (bucket.getBucketType() == BucketType.Auction)
                        resultBuckets.add(bucket); // return entire bucket if it is auction type
                    else {
                        //Compute percentOfDayVolume using start time and end time
                        double percentOfDayVolume = calculateRemainingPercentOfDayVolume(endTime - startTime, bucket);
                        resultBuckets.add(new Bucket(startTime, endTime, percentOfDayVolume, bucket.getBucketType()));
                    }
                } else {
                    // multiple buckets within start and end time
                    List<Bucket> bucketList = new ArrayList<>();
                    //create copy of buckets from main list
                    for (Bucket b : buckets.subList(startIndex, endIndex + 1)) {
                        bucketList.add(new Bucket(b));
                    }

                    //Create first bucket using start time
                    Bucket firstBucket = bucketList.removeFirst();
                    if (firstBucket.getStartTime() < startTime && firstBucket.getBucketType() == BucketType.Continuous) { // if start time is in middle of the bucket then calculate remaining percent
                        double percentOfDayVolume = calculateRemainingPercentOfDayVolume(firstBucket.getEndTime() - startTime, firstBucket);
                        resultBuckets.add(new Bucket(startTime, firstBucket.getEndTime(), percentOfDayVolume, firstBucket.getBucketType()));
                    } else if (firstBucket.getEndTime() > startTime) {
                        resultBuckets.add(firstBucket);
                    }

                    //Create last bucket using end time
                    if (!bucketList.isEmpty()) {
                        Bucket lastBucket = bucketList.removeLast();

                        resultBuckets.addAll(bucketList); // add all in between buckets to result

                        if (lastBucket.getEndTime() > endTime && lastBucket.getBucketType() == BucketType.Continuous) { // if end time is in middle of the bucket then calculate remaining percent
                            double percentOfDayVolume = calculateRemainingPercentOfDayVolume(endTime - lastBucket.getStartTime(), lastBucket);
                            resultBuckets.add(new Bucket(lastBucket.getStartTime(), endTime, percentOfDayVolume, lastBucket.getBucketType()));
                        } else
                            resultBuckets.add(lastBucket);
                    }
                }
            }
        }
        return new VolumeProfile(symbol, resultBuckets);
    }

    /**
     * Computes percent of day volume for smaller time window than bucket time
     *
     * @param remainingBucketTime
     * @param bucket
     * @return
     */
    private static double calculateRemainingPercentOfDayVolume(long remainingBucketTime, Bucket bucket) {
        long totalBucketTime = bucket.getEndTime() - bucket.getStartTime();
        return ((double) remainingBucketTime / totalBucketTime) * bucket.getPercentOfDayVolume();
    }

    /**
     * Finds index of bucket which covers passed time.
     * It works in two modes,
     * earliestBucket tries to find first bucket which can cover time
     * or
     * tries to find last bucket which can cover time
     *
     * @param time           time to be searched
     * @param earliestBucket whether pick earliest bucket or last bucket
     * @return index of bucket which covers time
     */
    private int bucketIndexOf(long time, boolean earliestBucket) {
        int index = -1;
        for (int i = 0; i < buckets.size(); i++) {
            if (earliestBucket && buckets.get(i).getStartTime() <= time && buckets.get(i).getEndTime() > time) {
                index = i;
                break;
            } else if (!earliestBucket && buckets.get(i).getStartTime() < time && buckets.get(i).getEndTime() >= time) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public String toString() {
        return "VolumeProfile{" +
                "symbol='" + symbol + '\'' +
                ", buckets=" + buckets +
                '}';
    }

    public String toMultiLineString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VolumeProfile:" + symbol).append("\n");
        for (Bucket bucket : buckets) {
            sb.append("[").append(bucket).append("]\n");
        }
        return sb.toString();
    }
}
