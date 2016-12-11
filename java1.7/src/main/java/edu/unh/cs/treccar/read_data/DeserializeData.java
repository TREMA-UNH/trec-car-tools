package edu.unh.cs.treccar.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.unh.cs.treccar.Data;

/**
 * User: dietz
 * Date: 12/9/16
 * Time: 1:56 PM
 */
public class DeserializeData {
    
 
    public static Iterator<Data.Page> iterAnnotations(InputStream inputStream) throws CborException {

        final CborDecoder decode = new CborDecoder(inputStream);

        return new Iterator<Data.Page>(){
            Data.Page next = lowLevelNext();
            @Override
            public boolean hasNext() {
                return next!=null;
            }

            private Data.Page lowLevelNext() throws CborException {
                DataItem dataItem = decode.decodeNext();
                if (dataItem != null) {
                    Data.Page page = pageFromCbor(dataItem);
                    return page;
                } else return null;
            }

            @Override
            public Data.Page next() {
                Data.Page curr = next;
                try {
                    next = lowLevelNext();
                } catch (CborException e) {
                    e.printStackTrace();
                    next=null;
                }
                return curr;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("read-only");
            }
        } ;

    }

    public static Iterable<Data.Page> iterableAnnotations(final InputStream inputStream) throws CborException {
        return new Iterable<Data.Page>() {
            @Override
            public Iterator<Data.Page> iterator() {
                try {
                    return iterAnnotations(inputStream);
                } catch (CborException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }





    public static Iterator<Data.Paragraph> iterParagraphs(InputStream inputStream) throws CborException {

        final CborDecoder decode = new CborDecoder(inputStream);

        return new Iterator<Data.Paragraph>(){
            Data.Paragraph next = lowLevelNext();
            @Override
            public boolean hasNext() {
                return next!=null;
            }

            private Data.Paragraph lowLevelNext() throws CborException {
                DataItem dataItem = decode.decodeNext();
                if (dataItem != null) {
                    Data.Paragraph paragraph = paragraphFromCbor(dataItem);
                    return paragraph;
                } else return null;
            }

            @Override
            public Data.Paragraph next() {
                Data.Paragraph curr = next;
                try {
                    next = lowLevelNext();
                } catch (CborException e) {
                    e.printStackTrace();
                    next=null;
                }
                return curr;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("read-only");
            }
        } ;

    }



    public static Iterable<Data.Paragraph> iterableParagraphs(final InputStream inputStream) throws CborException {
        return new Iterable<Data.Paragraph>() {
            @Override
            public Iterator<Data.Paragraph> iterator() {
                try {
                    return iterParagraphs(inputStream);
                } catch (CborException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    public static Data.Page pageFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();

        assert(array.get(0).getTag().getValue() == 0L);

        List<DataItem> array2 = ((Array) array.get(1)).getDataItems();
        assert(array2.get(0).getTag().getValue() == 0L);

//            ByteString heading = (ByteString) array2.getDataItems().get(1);
//            new String(heading.getBytes())
        UnicodeString heading = (UnicodeString) array2.get(1);
        DataItem skeletons = array.get(2);

        return new Data.Page(heading.getString(), pageSkeletonsFromCbor(skeletons));
    }

    private static Data.Para paraFromCbor(DataItem dataItem){
        return new Data.Para(paragraphFromCbor(dataItem));
    }

    public static Data.Paragraph paragraphFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        assert(array.get(0).getTag().getValue() == 0L);

        List<DataItem> array2 = ((Array) array.get(1)).getDataItems();
        assert(((UnsignedInteger) array2.get(0)).getValue().intValue() == 0);
        ByteString paraid = ((ByteString) array2.get(1));

//            List<DataItem> bodiesItem = ((Array) array.get(2)).getDataItems();
        DataItem bodiesItem = ((Array) array.get(2));

        return new Data.Paragraph( new String(paraid.getBytes()), paraBodiesFromCbor(bodiesItem));
    }


    private static Data.PageSkeleton pageSkeletonFromCbor(DataItem dataItem){
        List<DataItem> array = ((Array) dataItem).getDataItems();

        switch( ((UnsignedInteger) array.get(0)).getValue().intValue()) {
            case 0: {
                UnicodeString heading = (UnicodeString) ((Array) array.get(1)).getDataItems().get(1);
                return new Data.Section(heading.getString(), pageSkeletonsFromCbor(array.get(2)));
            }
            case 1: return paraFromCbor((array.get(1)));
            default: throw new RuntimeException("pageSkeletonFromCbor found an unhandled case: "+array.toString());
        }
    }
    private static List<Data.PageSkeleton> pageSkeletonsFromCbor(DataItem dataItem){

        Array skeletons = (Array) dataItem;
        List<Data.PageSkeleton> result = new ArrayList<Data.PageSkeleton>();
        for(DataItem item:skeletons.getDataItems()){
            if(isSpecialBreak(item))  break;
            result.add(pageSkeletonFromCbor(item));
        }
        return result;
    }



    private static List<Data.ParaBody> paraBodiesFromCbor(DataItem dataItem) {
        Array bodies = (Array) dataItem;
        List<Data.ParaBody> result = new ArrayList<Data.ParaBody>();
        for (DataItem item : bodies.getDataItems()) {
            if (isSpecialBreak(item)) break;
            result.add(paraBodyFromCbor(item));
        }
        return result;
    }


    private static Data.ParaBody paraBodyFromCbor(DataItem dataItem) {

        List<DataItem> array = ((Array) dataItem).getDataItems();

        switch( ((UnsignedInteger) array.get(0)).getValue().intValue()) {
            case 0: {
                UnicodeString text = (UnicodeString) array.get(1);
                return new Data.ParaText(text.getString());
            }
            case 1: {
                UnicodeString heading = (UnicodeString) ((Array) array.get(1)).getDataItems().get(1);
                UnicodeString second = (UnicodeString) array.get(2);
                return new Data.ParaLink(second.getString(), heading.getString());
            }
            default: throw new RuntimeException("paraBodyFromCbor found an unhandled case: "+array.toString());
        }
    }
    
    
    private static boolean isSpecialBreak(DataItem item) {
        return item.getMajorType()== MajorType.SPECIAL && ((Special) item).getSpecialType() == SpecialType.BREAK;
    }

}
