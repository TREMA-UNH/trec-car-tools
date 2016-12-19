package edu.unh.cs.treccar.playground;

import co.nstant.in.cbor.CborException;
import com.sun.deploy.util.StringUtils;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

import java.io.*;
import java.util.*;

/**
 * User: dietz
 * Date: 12/10/16
 * Time: 6:11 PM
 */
public class Wikistein {
    public Set<String> forbiddenHeadings;

    public Wikistein() {
        forbiddenHeadings = new HashSet<>();
        forbiddenHeadings.add("see also");
        forbiddenHeadings.add("external links");

    }

    public static class Line{
        public String pagename;
        public String sectionpath;
        public String paragraphId;
        public String paragraphContent;
        public List<String> negativeParagraphs = new ArrayList<>();


        public Line(String pagename, String sectionpath, String paragraphId, String paragraphContent) {
            this.pagename = pagename;
            this.sectionpath = sectionpath;
            this.paragraphId = paragraphId;
            this.paragraphContent = paragraphContent;
        }


        public void addNegativeParagraph(String paragraphContent) {
            negativeParagraphs.add(paragraphContent);
        }

        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(pagename);
            result.add(sectionpath);
            result.add(paragraphContent);
            result.addAll(negativeParagraphs);
            return result;
        }


        public String toTsvLine() {
            return StringUtils.join(toTsvSeqments(), "\t");
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Line line = (Line) o;

            return paragraphId != null ? paragraphId.equals(line.paragraphId) : line.paragraphId == null;
        }

        @Override
        public int hashCode() {
            return paragraphId != null ? paragraphId.hashCode() : 0;
        }
    }

    // --------------------------------


    private List<Line> recurseArticle(Data.PageSkeleton skel, String pagename, String sectionpath){

        if(skel instanceof Data.Section){
            final Data.Section section = (Data.Section) skel;
            String heading = section.getTitle();
            if(forbiddenHeadings.contains(heading.toLowerCase())) return Collections.emptyList();
            else {
                List<Line> result = new ArrayList<>();
                for (Data.PageSkeleton child : section.getChildren()) {
                    result.addAll(recurseArticle(child, pagename, sectionpath + " " + heading));
                }
                return result;
            }

        } else if (skel instanceof Data.Para) {
            Data.Para para = (Data.Para) skel;
            Data.Paragraph paragraph = para.getParagraph();

            String text = "";
            for(Data.ParaBody body: paragraph.getBodies()){
                if(body instanceof Data.ParaLink) text += ((Data.ParaLink)body).getAnchorText();
                if(body instanceof Data.ParaText) text += ((Data.ParaText)body).getText();
            }
            if(text.length()>10 && sectionpath.length()>3) {
                Line line = new Line(pagename, sectionpath, paragraph.getParaId(), text);
                return Collections.singletonList(line);
            } else return Collections.emptyList();
        }
        else throw new UnsupportedOperationException("not known skel "+skel);
    }




    public List<Line> doit(final FileInputStream fileInputStream) throws CborException {
        List<Line> megaresult = new ArrayList<Line>();

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {

            List<Line> result = new ArrayList<Line>();
            for(Data.PageSkeleton skel: page.getSkeleton()){
                result.addAll(recurseArticle(skel, page.getPageName(), ""));
            }

            for(Line line:result) {
                Set<Line> paras = drawRandomParagraphs(result, 4, line.sectionpath);
                for(Line p : paras) {
                    line.addNegativeParagraph(p.paragraphContent);
                }
            }

            megaresult.addAll(result);
        }

        return megaresult;
    }

    //--------------------------------

    private static Set<Line> drawRandomParagraphs(List<Line> lines, int draws, String notSectionPathPrefix){
        Set<Line> samples = new HashSet<>();
        int abortCounter = 100;
        while (samples.size()<draws && abortCounter>0){
            Line sample = lines.get(new Random().nextInt(lines.size()));
            if(!sample.sectionpath.startsWith(notSectionPathPrefix)){
                samples.add(sample);
            }
            abortCounter--;
        }

        return samples;
    }


    public static void main(String[] args) throws IOException, CborException {
        final FileInputStream fileInputStream = new FileInputStream(new File(args[0]));

        Wikistein wikistein = new Wikistein();
        List<Line> result = wikistein.doit(fileInputStream);

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(args[1])));

        for(Line line: result){
            System.out.println(line.toTsvSeqments());
            writer.write(line.toTsvLine());
            writer.newLine();

        }

        writer.close();

    }

}
