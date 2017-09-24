package edu.unh.cs.treccar.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;

import java.util.Iterator;

public class CborDataItemIterator implements Iterator<DataItem> {
    private CborDecoder decoder;
    private DataItem next;

    public CborDataItemIterator(CborDecoder decoder) throws CborRuntimeException {
        this.decoder = decoder;
        this.next = lowLevelNext();
    }

    private DataItem lowLevelNext() throws CborRuntimeException {
        try {
            DataItem dataItem = decoder.decodeNext();
            if (Special.BREAK.equals(dataItem)) {
                return null;
            } else {
                return dataItem;
            }
        } catch (CborException e) {
            throw new CborRuntimeException(e);
        }
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public DataItem next() {
        DataItem current = this.next;
        this.next = lowLevelNext();
        return current;
    }

    public void remove() {
        throw new UnsupportedOperationException("read-only");
    }
}
