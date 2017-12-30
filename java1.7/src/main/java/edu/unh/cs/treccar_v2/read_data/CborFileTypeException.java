package edu.unh.cs.treccar_v2.read_data;

import co.nstant.in.cbor.CborException;

public class CborFileTypeException extends RuntimeException {
    public CborFileTypeException(String cause) {
        super(cause);
    }
}
