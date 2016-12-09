package edu.unh.cs.treccar.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: dietz
 * Date: 12/9/16
 * Time: 1:56 PM
 */
public class ReadData {

    public static Iterator<Page> iterAnnotations(InputStream inputStream) throws CborException {

        final CborDecoder decode = new CborDecoder(inputStream);

        return new Iterator<Page>(){
            Page next = lowLevelNext();
            @Override
            public boolean hasNext() {
                return next!=null;
            }

            private Page lowLevelNext() throws CborException {
                DataItem dataItem = decode.decodeNext();
                if (dataItem != null) {
                    Page page = Page.fromCbor(dataItem);
                    return page;
                } else return null;
            }

            @Override
            public Page next() {
                Page curr = next;
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

    public static Iterable<Page> iterableAnnotations(final InputStream inputStream) throws CborException {
        return new Iterable<Page>() {
            @Override
            public Iterator<Page> iterator() {
                try {
                    return iterAnnotations(inputStream);
                } catch (CborException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }





    public static Iterator<Paragraph> iterParagraphs(InputStream inputStream) throws CborException {

        final CborDecoder decode = new CborDecoder(inputStream);

        return new Iterator<Paragraph>(){
            Paragraph next = lowLevelNext();
            @Override
            public boolean hasNext() {
                return next!=null;
            }

            private Paragraph lowLevelNext() throws CborException {
                DataItem dataItem = decode.decodeNext();
                if (dataItem != null) {
                    Paragraph paragraph = Paragraph.fromCbor(dataItem);
                    return paragraph;
                } else return null;
            }

            @Override
            public Paragraph next() {
                Paragraph curr = next;
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



    public static Iterable<Paragraph> iterableParagraphs(final InputStream inputStream) throws CborException {
        return new Iterable<Paragraph>() {
            @Override
            public Iterator<Paragraph> iterator() {
                try {
                    return iterParagraphs(inputStream);
                } catch (CborException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }



    static interface PageSkeleton {
    }


    public final static class Page {
        String pageName;
        List<PageSkeleton> skeleton;

        public Page(String pageName, List<PageSkeleton> skeleton) {
            this.pageName = pageName;
            this.skeleton = skeleton;
        }

        static Page fromCbor(DataItem dataItem) {
            List<DataItem> array = ((Array) dataItem).getDataItems();

            assert(array.get(0).getTag().getValue() == 0L);

            List<DataItem> array2 = ((Array) array.get(1)).getDataItems();
            assert(array2.get(0).getTag().getValue() == 0L);

//            ByteString heading = (ByteString) array2.getDataItems().get(1);
//            new String(heading.getBytes())
            UnicodeString heading = (UnicodeString) array2.get(1);
            DataItem skeletons = array.get(2);

            return new Page(heading.getString(), pageSkeletonsFromCbor(skeletons));
        }

        @Override
        public String toString() {
            return "Page{" +
                    "pageName='" + pageName + '\'' +
                    ", skeleton=" + skeleton +
                    '}';
        }
    }



    static PageSkeleton pageSkeletonFromCbor(DataItem dataItem){
        List<DataItem> array = ((Array) dataItem).getDataItems();

        switch( ((UnsignedInteger) array.get(0)).getValue().intValue()) {
            case 0: {
                UnicodeString heading = (UnicodeString) ((Array) array.get(1)).getDataItems().get(1);
                return new Section(heading.getString(), pageSkeletonsFromCbor(array.get(2)));
            }
            case 1: return Para.fromCbor((array.get(1)));
            default: throw new RuntimeException("pageSkeletonFromCbor found an unhandled case: "+array.toString());
        }
    }
    static List<PageSkeleton> pageSkeletonsFromCbor(DataItem dataItem){

        Array skeletons = (Array) dataItem;
        List<PageSkeleton> result = new ArrayList<PageSkeleton>();
        for(DataItem item:skeletons.getDataItems()){
            if(isSpecialBreak(item))  break;
            result.add(pageSkeletonFromCbor(item));
        }
        return result;
    }


    public final static class Section implements PageSkeleton{
        String title;
        List<PageSkeleton> children;

        public Section(String title, List<PageSkeleton> children) {
            this.title = title;
            this.children = children;
        }

        @Override
        public String toString() {
            return "Section{" +
                    "title='" + title + '\'' +
                    ", children=" + children +
                    '}';
        }
    }




    static interface ParaBody {
    }

    static public List<ParaBody> paraBodiesFromCbor(DataItem dataItem) {
        Array bodies = (Array) dataItem;
        List<ParaBody> result = new ArrayList<ParaBody>();
        for (DataItem item : bodies.getDataItems()) {
            if (isSpecialBreak(item)) break;
            result.add(paraBodyFromCbor(item));
        }
        return result;
    }


    static public ParaBody paraBodyFromCbor(DataItem dataItem) {

        List<DataItem> array = ((Array) dataItem).getDataItems();

        switch( ((UnsignedInteger) array.get(0)).getValue().intValue()) {
            case 0: {
                UnicodeString text = (UnicodeString) array.get(1);
                return new ParaText(text.getString());
            }
            case 1: {
                UnicodeString heading = (UnicodeString) ((Array) array.get(1)).getDataItems().get(1);
                UnicodeString second = (UnicodeString) array.get(2);
                return new ParaLink(heading.getString(), second.getString());
            }
            default: throw new RuntimeException("paraBodyFromCbor found an unhandled case: "+array.toString());
        }
    }

    public final static class Para implements PageSkeleton {
        Paragraph paragraph;

        public Para(Paragraph paragraph) {
            this.paragraph = paragraph;
        }

        static Para fromCbor(DataItem dataItem){
            return new Para(Paragraph.fromCbor(dataItem));
        }


        @Override
        public String toString() {
            return "Para{" +
                    "paragraph=" + paragraph +
                    '}';
        }
    }

    public final static class Paragraph  {
        String paraId;
        List<ParaBody> bodies;

        public Paragraph(String paraId, List<ParaBody> bodies) {
            this.paraId = paraId;
            this.bodies = bodies;
        }


        @Override
        public String toString() {
            return "Paragraph{" +
                    "paraId='" + paraId + '\'' +
                    ", bodies=" + bodies +
                    '}';
        }

        public static Paragraph fromCbor(DataItem dataItem) {
            List<DataItem> array = ((Array) dataItem).getDataItems();
            assert(array.get(0).getTag().getValue() == 0L);

            List<DataItem> array2 = ((Array) array.get(1)).getDataItems();
            assert(((UnsignedInteger) array2.get(0)).getValue().intValue() == 0);
            ByteString paraid = ((ByteString) array2.get(1));

//            List<DataItem> bodiesItem = ((Array) array.get(2)).getDataItems();
            DataItem bodiesItem = ((Array) array.get(2));

            return new Paragraph( new String(paraid.getBytes()), paraBodiesFromCbor(bodiesItem));
        }
    }



    public final static class ParaText implements ParaBody {
        String text;

        public ParaText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "ParaText{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    public final static class ParaLink implements ParaBody {
        String anchorText;
        String page;

        public ParaLink(String anchorText, String page) {
            this.anchorText = anchorText;
            this.page = page;
        }

        @Override
        public String toString() {
            return "ParaLink{" +
                    "anchorText='" + anchorText + '\'' +
                    ", page='" + page + '\'' +
                    '}';
        }
    }

    private static boolean isSpecialBreak(DataItem item) {
        return item.getMajorType()== MajorType.SPECIAL && ((Special) item).getSpecialType() == SpecialType.BREAK;
    }

}
