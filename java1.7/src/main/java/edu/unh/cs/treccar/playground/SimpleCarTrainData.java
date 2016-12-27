package edu.unh.cs.treccar.playground;

import co.nstant.in.cbor.CborException;
import com.sun.deploy.util.StringUtils;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

import java.io.*;
import java.util.*;

/**
 * Creates some simple training/test/cluster data from a TREC Car articles-cbor file.
 *
 * Will create data in the form $sectionpath -> $paragraph, $pagetitle is preserved but not used
 *
 * Training and test data will contain exactly four negative examples for every positive example.
 *
 * Creates following outputs in tab-separated format
 *
 * - Train:
 *   - $pagename, $sectionpath, $posparagraph, $negpara1, $negpara2, $negpara3, $negpara4
 * - Test:
 *   - $pagename, $sectionpath, $paragraph, $judgment
 * - Cluster:
 *   - $pagename, $sectionpath, $posparagraph
 *
 * For every positive instanve
 *
 *
 * User: dietz
 * Date: 12/10/16
 * Time: 6:11 PM
 */
public class SimpleCarTrainData {
    public Set<String> forbiddenHeadings;

    public SimpleCarTrainData() {
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

        @Override
        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(pagename);
            result.add(sectionpath);
            result.add(paragraphContent);

            result.add(judgment.toString());  // any preference on the judgment coding?
            return result;
        }


        @Override
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
            this.paragraphContent = paragraphContent.replaceAll("[\n\t\r]"," ");
        }


        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(pagename);
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
            negativeParagraphs.add(paragraphContent.replaceAll("[\n\t\r]"," "));
        }

        @Override
        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(pagename);
            result.add(sectionpath);
            result.add(paragraphContent);
            result.addAll(negativeParagraphs);
            return result;
        }


        @Override
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

                if(paras.size()==4) {  // only consider positive instances with 4 negative instances  --handled elsewhere
                    for (Instance instance2 : paras) {
                        instanceWithNegatives.addNegativeParagraph(instance2.paragraphContent);
                    }
                    megaresult.add(instanceWithNegatives);
                }
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

               if(paras.size()==4) {  // only consider positive instances with 4 negative instances  --handled elsewhere

                   for (Instance negInstance : paras) {
                       JudgedInstance negative = new JudgedInstance(instance.pagename, instance.sectionpath, negInstance.paragraphId, negInstance.paragraphContent, JudgedInstance.Judgment.SameArticleWrongSection);
                       megaresult.add(negative);
                   }
                   final JudgedInstance positive = new JudgedInstance(instance, JudgedInstance.Judgment.Relevant);
                   megaresult.add(positive);
               }
            }
        }

        fileInputStream.close();

        return megaresult;
    }

    public List<Instance> extractClusteringData(final FileInputStream fileInputStream) throws CborException, IOException {
        List<Instance> megaresult = new ArrayList<Instance>();

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
            List<Instance> result = getInstances(page);
            megaresult.addAll(result);
        }

        fileInputStream.close();

        return megaresult;
    }

    /**
     * Get instances from a page, exclude forbidden and empty headings, then filter instances with less than four negatives.
     * @param page
     * @return
     */
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

        return filterInstancesWithFewNegatives(result, 4);
    }

    private List<Instance> filterInstancesWithFewNegatives(List<Instance> result, int minNegs) {
        // kill sections that have less than four negatives
        HashMap<String, Integer> sectionCounts = new HashMap<>();
        for(Instance instance: result) {
            int count = 0;
            if(sectionCounts.containsKey(instance.sectionpath)){
                count = sectionCounts.get(instance.sectionpath);
            }
            sectionCounts.put(instance.sectionpath, count+1);
        }

        int totalCount = result.size();
        HashSet<String> sectionsToDrop = new HashSet<>();
        for(String sectionpath:sectionCounts.keySet()){
            int count = sectionCounts.get(sectionpath);
            if(totalCount-count< minNegs) sectionsToDrop.add(sectionpath);
        }

        List<Instance> filteredResult = new ArrayList<Instance>();
        for(Instance instance: result) {
            if(!sectionsToDrop.contains(instance.sectionpath)) filteredResult.add(instance);
        }
        return filteredResult;
    }


    //--------------------------------

    private static Set<Instance> drawRandomParagraphs(List<Instance> lines, int draws, String notSectionPathPrefix){
        List<Instance> negatives = new ArrayList<>();
        for(Instance line:lines) {
            if (!line.sectionpath.startsWith(notSectionPathPrefix)) negatives.add(line);
        }
        Collections.shuffle(negatives);

        Set<Instance> samples = new HashSet<>();
        samples.addAll(negatives.subList(0,draws));
        return samples;
    }
//
//    private static Set<Instance> drawRandomParagraphsOld(List<Instance> lines, int draws, String notSectionPathPrefix){
//        Set<Instance> samples = new HashSet<>();
//        int abortCounter = 100;
//        while (samples.size()<draws && abortCounter>0){
//            Instance sample = lines.get(new Random().nextInt(lines.size()));
//            if(!sample.sectionpath.startsWith(notSectionPathPrefix)){
//                samples.add(sample);
//            }
//            abortCounter--;
//        }
//
//        return samples;
//    }


    public static void main(String[] args) throws IOException, CborException {
        final String cborArticleInputFile = args[0];
        final String trainingOutputFile = args[1];
        final String testOutputFile = args[2];
        final String clusterOutputFile = args[3];


        System.out.println("training");

        {
            SimpleCarTrainData wikistein = new SimpleCarTrainData();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            List<InstanceWithNegatives> trainData = wikistein.extractTrainData(fileInputStream);
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(trainingOutputFile)));
            for(InstanceWithNegatives line: trainData){
//                System.out.println(line.toTsvSeqments());
                trainWriter.write(line.toTsvLine());
                trainWriter.newLine();
            }
            trainWriter.close();

        }

        System.out.println("testing");


        {
            SimpleCarTrainData wikistein = new SimpleCarTrainData();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            List<JudgedInstance> testData = wikistein.extractTestData(fileInputStream);
            BufferedWriter testWriter = new BufferedWriter(new FileWriter(new File(testOutputFile)));
            for(JudgedInstance line: testData){
//                System.out.println(line.toTsvSeqments());
                testWriter.write(line.toTsvLine());
                testWriter.newLine();
            }
            testWriter.close();

        }


        System.out.println("cluster");


        {
            SimpleCarTrainData wikistein = new SimpleCarTrainData();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            List<Instance> testData = wikistein.extractClusteringData(fileInputStream);
            BufferedWriter clusterWriter = new BufferedWriter(new FileWriter(new File(clusterOutputFile)));
            for(Instance line: testData){
//                System.out.println(line.toTsvSeqments());
                clusterWriter.write(line.toTsvLine());
                clusterWriter.newLine();
            }
            clusterWriter.close();

        }

    }

}
