package edu.unh.cs.treccar.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;

import java.util.Iterator;

import edu.unh.cs.treccar.read_data.TrecCarHeader;
import edu.unh.cs.treccar.read_data.CborRuntimeException;
import edu.unh.cs.treccar.read_data.CborDataItemIterator;

public abstract class CborListWithHeaderIterator<T> implements Iterator<T> {
    CborDataItemIterator listIter;
    DataItem firstElem;
    TrecCarHeader header;

    public CborListWithHeaderIterator(CborDecoder decoder) throws CborRuntimeException {
        // try reading the header
        try {
            DataItem dataItem = decoder.decodeNext();
            try {
                this.header = new TrecCarHeader(dataItem);

                // decode contents, this should begin with an indefinite array
                try {
                    decoder.setAutoDecodeInfinitiveArrays(false);
                    Array arr = (Array) decoder.decodeNext();
                    decoder.setAutoDecodeInfinitiveArrays(true);
                } catch (CborException e) {
                    throw new CborRuntimeException(e);
                }
                this.firstElem = null;
            } catch (TrecCarHeader.InvalidHeaderException e) {
                // there is no header
                this.header = null;
                this.firstElem = dataItem;
            }

            this.listIter = new CborDataItemIterator(decoder);
        } catch (CborException e) {
            throw new CborRuntimeException(e);
        }
    }

    public boolean hasNext() {
        return this.firstElem != null || this.listIter.hasNext();
    }

    public T next() {
        if (this.firstElem != null) {
            DataItem first = this.firstElem;
            this.firstElem = null;
            return parseItem(first);
        } else {
            return parseItem(this.listIter.next());
        }
    }

    protected abstract T parseItem(DataItem dataItem);
}
