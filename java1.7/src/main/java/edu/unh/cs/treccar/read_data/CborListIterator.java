package edu.unh.cs.treccar.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;

import java.util.Iterator;
import java.io.InputStream;

import edu.unh.cs.treccar.read_data.RuntimeCborException;

public abstract class CborListIterator<T> implements Iterator<T> {
    private CborDecoder decoder;
    private T next;

    public CborListIterator(CborDecoder decoder) throws RuntimeCborException {
        this.decoder = decoder;

        // decode contents, this should begin with an indefinite array
        try {
            this.decoder.setAutoDecodeInfinitiveArrays(false);
            Array arr = (Array) decoder.decodeNext();
            this.decoder.setAutoDecodeInfinitiveArrays(true);
        } catch (CborException e) {
            throw new RuntimeCborException(e);
        }
        this.next = lowLevelNext();
    }

    abstract protected T decodeItem(DataItem item);

    private T lowLevelNext() throws RuntimeCborException {
        try {
            DataItem dataItem = decoder.decodeNext();
            if (Special.BREAK.equals(dataItem)) {
                return null;
            } else {
                return decodeItem(dataItem);
            }
        } catch (CborException e) {
            throw new RuntimeCborException(e);
        }
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public T next() {
        this.next = lowLevelNext();
        return this.next;
    }

    public void remove() {
        throw new UnsupportedOperationException("read-only");
    }
}
