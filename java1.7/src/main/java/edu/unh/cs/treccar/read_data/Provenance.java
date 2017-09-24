package edu.unh.cs.treccar.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;

import java.util.List;

public class Provenance {
    final String wikiDumpDate;
    final String wikiSite;
    final String dataReleaseName;
    final String comments;
    final String toolsCommit;

    public Provenance(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();

        if (array.size() != 6) {
            throw new RuntimeException("Provenance: invalid length");
        }

        wikiDumpDate = ((UnicodeString) array.get(1)).getString();
        wikiSite = ((UnicodeString) array.get(2)).getString();
        dataReleaseName = ((UnicodeString) array.get(3)).getString();
        comments = ((UnicodeString) array.get(4)).getString();
        toolsCommit = ((UnicodeString) array.get(5)).getString();

    }
}
