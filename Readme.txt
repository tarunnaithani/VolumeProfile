BRIEF SUMMARY

PROJECT DETAILS
    Project has been created in IntelliJ.

SYSTEM REQUIREMENT
    Minimum JDK version 1.7, 24 preferred as it is configured at project level
    Minimum JUnit 4, JUnit 5 preferred as it is configured at project level

Volume Profile
	This is basic implementation of Volume Profile being loaded from csv file.
	Once loaded functionality is available to extract volume profile for shorter time periods
	or calculate percent of day volume which will be traded within a time period.

	The main class VolumeProfileCSVFileLoader reads csv data as Bucket objects and instantiates VolumeProfile.
	VolumeProfile class holds the data and provides functionality needed.

	Data Validations are done in 3 levels:
	    -> All CSV data verification is done by VolumeProfileCSVFileLoader
	    -> All validation to ensure bucket data is correct is done in Bucket class
	    -> Validations across multiple buckets are done in VolumeProfile class,
	       for example, entire day profile adds upto 100% and buc ket do not overlap

    Two method in VolumeProfile class provide the requested functionality
        -> getCumulativeVolumeProfile -> It returns VolumeProfile relevant to time period specified in input
        -> getTargetPercentForTimePeriod -> It returns target percent of day volume to be used within time period specified in input

Assumptions
	-> Volume profile data available in file is as ascending time-series
	-> Buckets are not allowed to be overlapping with each other
	-> No validation is needed against market times to ensure validity of the Volume Profile
	-> Only two Bucket type is supported: Auction and continuous

SYSTEM LIMITATIONS
	-> start time or end time within auction is considered to be taking part in auction
	-> Compaction logic to remove buckets with 0 traded volume has not been implemented
	-> Current system does lot of Object creation during operation which will cause GC issues if used in load testing.

TESTING
The main idea is to get 100% code coverage using behaviour testing so that refactoring is easier later on.
-> VolumeProfileCSVFileLoaderTest.java runs file loading and other integration scenarios

-> VolumeProfileTest.java verifies bulk of VolumeProfile operations

-> ConstantUtilsTest.java verifies the csv conversion operations



