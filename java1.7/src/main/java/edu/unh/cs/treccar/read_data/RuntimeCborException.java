package edu.unh.cs.treccar.read_data;

import co.nstant.in.cbor.CborException;

public class RuntimeCborException extends RuntimeException {
    public RuntimeCborException(CborException cause) {
        super(cause);
    }
    public CborException getCborException() {
        return (CborException) this.getCause();
    }
}
