package edu.unh.cs.treccar.playground;

import co.nstant.in.cbor.CborException;
import com.sun.deploy.util.StringUtils;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dietz
 * Date: 12/23/16
 * Time: 4:36 PM
 */
public class LinksWithContextKeywords {


    public static class LinkInstance {
        protected String fromPage;
        protected String sectionpath;
        protected String toPage;
        protected String paragraphContent;


        public LinkInstance(String fromPage, String sectionpath, String toPage, String paragraphContent) {
            this.fromPage = fromPage;
            this.sectionpath = sectionpath;
            this.toPage = toPage;
            this.paragraphContent = paragraphContent.replaceAll("[\n\t\r]"," ");
        }


        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(fromPage);
            result.add(toPage);
            result.add(sectionpath);
            result.add(paragraphContent);
            return result;
        }


        public String toTsvLine() {
            return StringUtils.join(toTsvSeqments(), "\t");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LinkInstance)) return false;

            LinkInstance instance = (LinkInstance) o;

            if (fromPage != null ? !fromPage.equals(instance.fromPage) : instance.fromPage != null) return false;
            if (sectionpath != null ? !sectionpath.equals(instance.sectionpath) : instance.sectionpath != null)
                return false;
            return toPage != null ? toPage.equals(instance.toPage) : instance.toPage == null;
        }

        @Override
        public int hashCode() {
            int result = fromPage != null ? fromPage.hashCode() : 0;
            result = 31 * result + (sectionpath != null ? sectionpath.hashCode() : 0);
            result = 31 * result + (toPage != null ? toPage.hashCode() : 0);
            return result;
        }
    }

    public LinksWithContextKeywords() {
    }


    private List<LinkInstance> extractLinkData(FileInputStream fileInputStream, String keyword, boolean addParagraph, boolean filterByKeyword) throws IOException, CborException {
        List<LinkInstance> megaresult = new ArrayList<LinkInstance>();

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {

            List<LinkInstance> result = getInstances(page, keyword, addParagraph, filterByKeyword);
            megaresult.addAll(result);
        }

        fileInputStream.close();

        return megaresult;

    }

    private static boolean paragraphTextContainsKeyword(Data.Paragraph para, String keyword){
        return para.getTextOnly().toLowerCase().contains(keyword);
    }

    private List<LinkInstance> getInstances(Data.Page page, String keyword, boolean addParagraph, boolean filterByKeyword) {
        List<LinkInstance> result = new ArrayList<>();
        for(Data.Page.SectionPathParagraphs sectparas : page.flatSectionPathsParagraphs()){
            if(!filterByKeyword || paragraphTextContainsKeyword(sectparas.getParagraph(), keyword)){
                for(String toPage :sectparas.getParagraph().getEntitiesOnly()){
                    String text = "";
                    if(addParagraph) text = sectparas.getParagraph().getTextOnly();
                    final String sectPath = StringUtils.join(sectparas.getSectionPath(), " ");
                    result.add(new LinkInstance(page.getPageName(), sectPath, toPage, text));
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException, CborException {
        final String cborArticleInputFile = args[0];
        final String keyword = args[1];
        final String linkOutputFile = args[2];

//        final String testOutputFile = args[2];
//        final String clusterOutputFile = args[3];

        boolean addParagraph = false;
        boolean filterByKeyword= false;



        if(filterByKeyword) {
            System.out.println("extract links with keyword "+keyword+" from file "+cborArticleInputFile);
        } else {
            System.out.println("extract all links from "+cborArticleInputFile);
        }

        {
            LinksWithContextKeywords extract = new LinksWithContextKeywords();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            List<LinkInstance> trainData = extract.extractLinkData(fileInputStream, keyword, addParagraph, filterByKeyword);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(linkOutputFile)));
            for(LinkInstance line: trainData){
                System.out.println(line.toTsvSeqments());
                writer.write(line.toTsvLine());
                writer.newLine();
            }
            writer.close();

        }
    }

}
