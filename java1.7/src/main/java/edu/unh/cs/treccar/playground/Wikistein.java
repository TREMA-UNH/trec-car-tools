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
        forbiddenHeadings.add("references");
        forbiddenHeadings.add("external links");
        forbiddenHeadings.add("notes");
        forbiddenHeadings.add("bibliography");
        forbiddenHeadings.add("further reading");
    }

    public static class JudgedInstance extends Instance {

        public enum Judgment {Relevant, SameArticleWrongSection, WrongArticle}

        protected Judgment judgment = Judgment.WrongArticle;

        public JudgedInstance(String pagename, String sectionpath, String paragraphId, String paragraphContent, Judgment judgment) {
            super(pagename, sectionpath, paragraphId,paragraphContent);
            this.judgment = judgment;
        }

        public JudgedInstance(Instance instance, Judgment judgment){
            this(instance.pagename, instance.sectionpath, instance.paragraphId, instance.paragraphContent, judgment);
        }


        public Judgment getJudgment() {
            return judgment;
        }

        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(pagename);
            result.add(sectionpath);
            result.add(paragraphContent);

            result.add(judgment.toString());  // any preference on the judgment coding?
            return result;
        }


        public String toTsvLine() {
            return StringUtils.join(toTsvSeqments(), "\t");
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JudgedInstance)) return false;
            if (!super.equals(o)) return false;

            JudgedInstance that = (JudgedInstance) o;

            if (judgment != that.judgment) return false;
            if (super.pagename != null ? !super.pagename.equals(super.pagename) : super.pagename != null) return false;
            if (super.sectionpath != null ? !super.sectionpath.equals(super.sectionpath) : super.sectionpath != null)
                return false;
            return super.paragraphId != null ? super.paragraphId.equals(super.paragraphId) : super.paragraphId == null;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (judgment != null ? judgment.hashCode() : 0);
            result = 31 * result + (super.pagename != null ? super.pagename.hashCode() : 0);
            result = 31 * result + (super.sectionpath != null ? super.sectionpath.hashCode() : 0);
            result = 31 * result + (super.paragraphId != null ? super.paragraphId.hashCode() : 0);
            return result;
        }
    }

    public static class Instance{
        protected String pagename;
        protected String sectionpath;
        protected String paragraphId;
        protected String paragraphContent;


        public Instance(String pagename, String sectionpath, String paragraphId, String paragraphContent) {
            this.pagename = pagename;
            this.sectionpath = sectionpath;
            this.paragraphId = paragraphId;
            this.paragraphContent = paragraphContent;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Instance instance = (Instance) o;

            return paragraphId != null ? paragraphId.equals(instance.paragraphId) : instance.paragraphId == null;
        }

        @Override
        public int hashCode() {
            return paragraphId != null ? paragraphId.hashCode() : 0;
        }
    }

    public static class InstanceWithNegatives extends Instance {
        protected List<String> negativeParagraphs = new ArrayList<>();


        public InstanceWithNegatives(String pagename, String sectionpath, String paragraphId, String paragraphContent) {
            super(pagename, sectionpath, paragraphId, paragraphContent);
        }

        public InstanceWithNegatives(Instance instance){
            this(instance.pagename, instance.sectionpath, instance.paragraphId, instance.paragraphContent);
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


    }

    // --------------------------------


    public List<InstanceWithNegatives> extractTrainData(final FileInputStream fileInputStream) throws CborException, IOException {
        List<InstanceWithNegatives> megaresult = new ArrayList<InstanceWithNegatives>();

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {

            List<Instance> result = getInstances(page);

            for(Instance instance1:result) {
                final InstanceWithNegatives instanceWithNegatives = new InstanceWithNegatives(instance1);
                Set<Instance> paras = drawRandomParagraphs(result, 4, instance1.sectionpath);
                for(Instance instance2 : paras) {
                    instanceWithNegatives.addNegativeParagraph(instance2.paragraphContent);
                }
                megaresult.add(instanceWithNegatives);
            }
        }

        fileInputStream.close();

        return megaresult;
    }

    public List<JudgedInstance> extractTestData(final FileInputStream fileInputStream) throws CborException, IOException {
        List<JudgedInstance> megaresult = new ArrayList<JudgedInstance>();

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
            List<Instance> result = getInstances(page);
            for(Instance instance:result) {
               Set<Instance> paras = drawRandomParagraphs(result, 4, instance.sectionpath);
                for(Instance negInstance : paras) {
                    JudgedInstance negative = new  JudgedInstance(instance.pagename, instance.sectionpath, negInstance.paragraphId, negInstance.paragraphContent, JudgedInstance.Judgment.SameArticleWrongSection);
                    megaresult.add(negative);
                }
                final JudgedInstance positive = new JudgedInstance(instance, JudgedInstance.Judgment.Relevant);
                megaresult.add(positive);
            }
        }

        fileInputStream.close();

        return megaresult;
    }

    private List<Instance> getInstances(Data.Page page) {
        List<Instance> result = new ArrayList<Instance>();

        for(Data.Page.SectionPathParagraphs sectpara:page.flatSectionPathsParagraphs()) {
            final String sectionpath = StringUtils.join(sectpara.getSectionPath(), " ");

            boolean isExcludeItem = false;

            if(sectpara.getSectionPath().isEmpty()) isExcludeItem = true; // skip lead paragraph
            for(String heading: sectpara.getSectionPath()) {
                if (forbiddenHeadings.contains(heading.toLowerCase())) isExcludeItem = true;
            }


            final String paraId = sectpara.getParagraph().getParaId();
            final String paratext = sectpara.getParagraph().getTextOnly();

            if(paratext.length()<10) isExcludeItem = true;

            if(!isExcludeItem) {
                Instance line = new Instance(page.getPageName(), sectionpath, paraId, paratext);
                result.add(line);
            }
        }
        return result;
    }

    //--------------------------------

    private static Set<Instance> drawRandomParagraphs(List<Instance> lines, int draws, String notSectionPathPrefix){
        Set<Instance> samples = new HashSet<>();
        int abortCounter = 100;
        while (samples.size()<draws && abortCounter>0){
            Instance sample = lines.get(new Random().nextInt(lines.size()));
            if(!sample.sectionpath.startsWith(notSectionPathPrefix)){
                samples.add(sample);
            }
            abortCounter--;
        }

        return samples;
    }


    public static void main(String[] args) throws IOException, CborException {
        final String cborArticleInputFile = args[0];
        final String trainingOutputFile = args[1];
        final String testOutputFile = args[2];




        {
            Wikistein wikistein = new Wikistein();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            List<InstanceWithNegatives> trainData = wikistein.extractTrainData(fileInputStream);
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(trainingOutputFile)));
            for(InstanceWithNegatives line: trainData){
                System.out.println(line.toTsvSeqments());
                trainWriter.write(line.toTsvLine());
                trainWriter.newLine();
            }
            trainWriter.close();

        }


        {
            Wikistein wikistein = new Wikistein();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            List<JudgedInstance> testData = wikistein.extractTestData(fileInputStream);
            BufferedWriter testWriter = new BufferedWriter(new FileWriter(new File(testOutputFile)));
            for(JudgedInstance line: testData){
                System.out.println(line.toTsvSeqments());
                testWriter.write(line.toTsvLine());
                testWriter.newLine();
            }
            testWriter.close();

        }

    }

}
