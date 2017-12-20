package edu.unh.cs.treccar_v2.read_data;

import co.nstant.in.cbor.model.*;

import java.util.List;
import java.util.Objects;

public class TrecCarHeader {
    final FileType fileType;
    final Provenance provenance;

    public static enum FileType {
        PagesFile(0),
        OutlinesFile(1),
        ParagraphsFile(2);

        private int value;
        private FileType(int value) {
            this.value = value;
        }

        private static FileType[] values = null;
        public static FileType fromInt(int i) {
            if (FileType.values == null) {
                FileType.values = FileType.values();
            }
            return FileType.values[i];
        }
    };

    private FileType decodeFileType(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        return FileType.fromInt(((UnsignedInteger) array.get(0)).getValue().intValue());
    }

    public TrecCarHeader(DataItem dataItem) throws InvalidHeaderException {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        if (array.size() != 3) {
            throw new InvalidHeaderException();
        }

        try {
            String magicWord = ((UnicodeString) array.get(0)).getString();
            if (!Objects.equals(magicWord, "CAR")) {
                throw new InvalidHeaderException();
            }
        } catch (ClassCastException e) {
            throw new InvalidHeaderException();
        }

        fileType = decodeFileType(array.get(1));
        provenance = new Provenance(array.get(2));
    }

    public static class InvalidHeaderException extends Exception {
        public InvalidHeaderException() {}
    }
}
