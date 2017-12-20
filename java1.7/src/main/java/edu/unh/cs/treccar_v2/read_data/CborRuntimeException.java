package edu.unh.cs.treccar_v2.read_data;

import co.nstant.in.cbor.CborException;

public class CborRuntimeException extends RuntimeException {
    public CborRuntimeException(CborException cause) {
        super(cause);
    }
    public CborException getCborException() {
        return (CborException) this.getCause();
    }
}
