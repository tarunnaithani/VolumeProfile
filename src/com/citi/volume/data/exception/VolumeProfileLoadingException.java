package com.citi.volume.data.exception;

public class VolumeProfileLoadingException extends RuntimeException {
    public VolumeProfileLoadingException(String s, Throwable e) {
        super(s, e);
    }

    public VolumeProfileLoadingException(String s) {
        super(s);
    }
}
