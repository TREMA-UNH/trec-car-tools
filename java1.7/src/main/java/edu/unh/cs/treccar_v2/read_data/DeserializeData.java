package edu.unh.cs.treccar_v2.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.unh.cs.treccar_v2.Data;

public class DeserializeData {

    public static Iterator<Data.Page> iterAnnotations(InputStream inputStream) throws CborRuntimeException {
        class PageIterator extends CborListWithHeaderIterator<Data.Page> {
            public PageIterator(CborDecoder decoder) throws CborRuntimeException {
                super(decoder);
            }
            protected Data.Page parseItem(DataItem dataItem) {
                return pageFromCbor(dataItem);
            }
        };

        final CborDecoder decode = new CborDecoder(inputStream);
        return new PageIterator(decode);
    }


    public static Iterable<Data.Page> iterableAnnotations(final InputStream inputStream) throws CborRuntimeException {
        return new Iterable<Data.Page>() {
            public Iterator<Data.Page> iterator() {
                return iterAnnotations(inputStream);
            }
        };
    }


    /**
     * @return null if no valid object can be located at the byte offset
     */
    private static Data.Page annotationAtOffset(final InputStream inputStream, long offset) throws CborRuntimeException, IOException {
        inputStream.skip(offset);
        return iterAnnotations(inputStream).next();
    }


    public static Iterator<Data.Paragraph> iterParagraphs(InputStream inputStream) throws CborRuntimeException {
        class ParagraphIterator extends CborListWithHeaderIterator<Data.Paragraph> {
            ParagraphIterator(CborDecoder decoder) throws CborRuntimeException {
                super(decoder);
            }
            protected Data.Paragraph parseItem(DataItem dataItem) {
                return paragraphFromCbor(dataItem);
            }
        };

        final CborDecoder decode = new CborDecoder(inputStream);
        return new ParagraphIterator(decode);
    }


    public static Iterable<Data.Paragraph> iterableParagraphs(final InputStream inputStream) throws CborRuntimeException {
        return new Iterable<Data.Paragraph>() {
            public Iterator<Data.Paragraph> iterator() {
                return iterParagraphs(inputStream);
            }
        };
    }


    private static ArrayList<String> getMaybeListText(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        if (array.size() == 0) {
            return null;
        } else if (array.size() == 1) {
            List<DataItem> resultArray = ((Array) array.get(0)).getDataItems();
            ArrayList<String> result = getUnicodeArray(resultArray);
            return result;
        } else {
            throw new RuntimeException("Invalid Maybe [PageName]");
        }
    }

    private static ArrayList<String> getUnicodeArray(List<DataItem> resultArray) {
        ArrayList<String> result = new ArrayList(resultArray.size());
        for (DataItem item: resultArray) {
            if (Special.BREAK.equals(item)) {
                break;
            }
            String s = ((UnicodeString) item).getString();
            result.add(s);
        }
        return result;
    }


    private static ArrayList<String> getMaybeListPageIds(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        if (array.size() == 0) {
            return null;
        } else if (array.size() == 1) {
            List<DataItem> resultArray = ((Array) array.get(0)).getDataItems();
            ArrayList<String> result = getByteArray(resultArray);
            return result;
        } else {
            throw new RuntimeException("Invalid Maybe [PageId]");
        }
    }

    private static ArrayList<String> getByteArray(List<DataItem> resultArray) {
        ArrayList<String> result = new ArrayList(resultArray.size());
        for (DataItem item: resultArray) {
            if (Special.BREAK.equals(item)) {
                break;
            }
            String s = new String(((ByteString) item).getBytes());
            result.add(s);
        }
        return result;
    }

    public static Data.PageType pageTypeFromCbor(DataItem dataItem) {
//        Data.PageType pageType = Data.PageType.fromInt(((UnsignedInteger) (((Array) dataItem).get(1)).getDataItems().get(0)).getValue().intValue());
        final DataItem tag = ((Array) dataItem).getDataItems().get(0);
        final int tagValue = ((UnsignedInteger) tag).getValue().intValue();
        return Data.PageType.fromInt(tagValue);
//        return  pageType;
    }

    // page type 0: article, 1: category, 2: Disambiguation, 3: redirect (with link)
    public static Data.PageMetadata pageMetadataFromCbor(DataItem dataItem) {
        List<DataItem> outerArray = ((Array) dataItem).getDataItems();

        final Data.PageMetadata pageMetadata = new Data.PageMetadata();

        for (int i = 0; i < outerArray.size(); i+=2) {
            DataItem tagArray = outerArray.get(i);
            if (Special.BREAK.equals(tagArray)) {
                break;
            }


            final DataItem tag = ((Array) tagArray).getDataItems().get(0);
            final long tagValue = ((UnsignedInteger) tag).getValue().longValue();

            DataItem item = outerArray.get(i+1);
            if (Special.BREAK.equals(item)) {
                throw new RuntimeException("Illegal protocol when decoding page metadata. Tag is "+tag+" but item is BREAK.");
            }


//            long tagValue = tag.getTag().getValue();
            if (tagValue == 0L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getRedirectNames().addAll(array);
            } else if (tagValue == 1L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getDisambiguationNames().addAll(array);
            } else if (tagValue == 2L) {
                final ArrayList<String> array = getByteArray(((Array) item).getDataItems());
                pageMetadata.getDisambiguationIds().addAll(array);
            } else if (tagValue == 3L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getCategoryNames().addAll(array);
            } else if (tagValue == 4L) {
                final ArrayList<String> array = getByteArray(((Array) item).getDataItems());
                pageMetadata.getCategoryIds().addAll(array);
            } else if (tagValue == 5L) {
                final ArrayList<String> array = getByteArray(((Array) item).getDataItems());
                pageMetadata.getInlinkIds().addAll(array);
            } else if (tagValue == 6L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getInlinkAnchors().addAll(array);
            }
        }

        return pageMetadata;

//
//        assert(array.get(0).getTag().getValue() == 0L);
//        ArrayList<String> redirectNames = getMaybeListText(array.get(2));
//        ArrayList<String> disambiguationNames = getMaybeListText(array.get(3));
//        ArrayList<String> disambiguationIds = getMaybeListPageIds(array.get(4));
//        ArrayList<String> categoryNames = getMaybeListText(array.get(5));
//        ArrayList<String> categoryIds = getMaybeListPageIds(array.get(6));
//        ArrayList<String> inlinkIds = getMaybeListPageIds(array.get(7));
//        return new Data.PageMetadata(pageType, redirectNames, disambiguationNames, disambiguationIds, categoryNames, categoryIds, inlinkIds);
//    }
    }

    public static Data.Page pageFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();

        assert(array.get(0).getTag().getValue() == 0L);

        UnicodeString pageName = (UnicodeString) array.get(1);
        ByteString pageId = (ByteString) array.get(2);
        DataItem skeletons = array.get(3);
        Data.PageType pageType = null;
        Data.PageMetadata pageMetadata = null;
        if (array.size() > 4) {
            pageType = pageTypeFromCbor(array.get(4));
            pageMetadata = pageMetadataFromCbor(array.get(5));// [ tag1, payload1, tag2, payload2, ...]
/*
          RedirectNames xs -> simple 0 xs
          DisambiguationNames xs -> simple 1 xs
          DisambiguationIds xs -> simple 2 xs
          CategoryNames xs -> simple 3 xs
          CategoryIds xs -> simple 4 xs
          InlinkIds xs -> simple 5 xs
          InlinkAnchors xs -> simple 6 xs
          UnknownMetadata len tag y ->
                 CBOR.encodeListLen (fromIntegral len)
              <> CBOR.encodeInt tag
              <> CBOR.encodeTerm y

             */
        }

        return new Data.Page(pageName.getString(), new String(pageId.getBytes()), pageSkeletonsFromCbor(skeletons), pageType, pageMetadata);
    }

    private static Data.Image imageFromCbor(DataItem imageUrlDataItem, DataItem skeletonDataItem) {
        UnicodeString imageUrl = (UnicodeString) imageUrlDataItem;

        return new Data.Image(imageUrl.getString(), pageSkeletonsFromCbor(skeletonDataItem));
    }

    private static Data.ListItem listFromCbor(DataItem nestingLevelItem, DataItem paragraphItem) {
        UnsignedInteger nestingLevel = (UnsignedInteger) nestingLevelItem;
        return new Data.ListItem(nestingLevel.getValue().intValue(), paragraphFromCbor(paragraphItem));
    }

    private static Data.Para paraFromCbor(DataItem dataItem){
        return new Data.Para(paragraphFromCbor(dataItem));
    }

    public static Data.Paragraph paragraphFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        assert(array.get(0).getTag().getValue() == 0L);

//        List<DataItem> array2 = ((Array) array.get(1)).getDataItems();
//        assert(((UnsignedInteger) array2.get(0)).getValue().intValue() == 0);
        ByteString paraid = (ByteString) array.get(1);

//            List<DataItem> bodiesItem = ((Array) array.get(2)).getDataItems();
        DataItem bodiesItem = (Array) array.get(2);

        return new Data.Paragraph( new String(paraid.getBytes()), paraBodiesFromCbor(bodiesItem));
    }


    private static Data.PageSkeleton pageSkeletonFromCbor(DataItem dataItem){
        List<DataItem> array = ((Array) dataItem).getDataItems();

        switch( ((UnsignedInteger) array.get(0)).getValue().intValue()) {
            case 0: {
                UnicodeString heading = (UnicodeString) array.get(1);
                ByteString headingId = (ByteString) array.get(2);
                return new Data.Section(heading.getString(), new String(headingId.getBytes()), pageSkeletonsFromCbor(array.get(3)));
            }
            case 1: return paraFromCbor((array.get(1)));
            case 2: return imageFromCbor(array.get(1), array.get(2));
            case 3: return listFromCbor(array.get(1), array.get(2));
            default: throw new RuntimeException("pageSkeletonFromCbor found an unhandled case: "+array.toString());
        }
    }
    private static List<Data.PageSkeleton> pageSkeletonsFromCbor(DataItem dataItem){

        Array skeletons = (Array) dataItem;
        List<Data.PageSkeleton> result = new ArrayList<Data.PageSkeleton>();
        for(DataItem item:skeletons.getDataItems()){
            if (Special.BREAK.equals(item))  break;
            result.add(pageSkeletonFromCbor(item));
        }
        return result;
    }



    private static List<Data.ParaBody> paraBodiesFromCbor(DataItem dataItem) {
        Array bodies = (Array) dataItem;
        List<Data.ParaBody> result = new ArrayList<Data.ParaBody>();
        for (DataItem item : bodies.getDataItems()) {
            if (Special.BREAK.equals(item)) break;
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
                List<DataItem> array_ = ((Array) array.get(1)).getDataItems();

                UnicodeString page = (UnicodeString) array_.get(1);
                ByteString pageId = (ByteString) array_.get(3);
                UnicodeString anchorText = (UnicodeString) array_.get(4);
                // this is either a list of one or zero elements
                List<DataItem> linkSectionMaybe = ((Array) array_.get(2)).getDataItems();
                if(linkSectionMaybe.size()>0) {
                    UnicodeString linkSection = ((UnicodeString) linkSectionMaybe.get(0));
                    return new Data.ParaLink(page.getString(), new String(pageId.getBytes()), linkSection.getString(), anchorText.getString());
                }else {
                    return new Data.ParaLink(page.getString(),  new String(pageId.getBytes()), anchorText.getString());
                }
            }
            default: throw new RuntimeException("paraBodyFromCbor found an unhandled case: "+array.toString());
        }
    }
}
