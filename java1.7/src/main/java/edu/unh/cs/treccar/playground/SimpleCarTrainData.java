package edu.unh.cs.treccar.playground;

import co.nstant.in.cbor.CborException;
import com.sun.deploy.util.StringUtils;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Creates some simple training/test/cluster data from a TREC Car articles-cbor file.
 *
 * Will create data in the form $sectionpathlist -> $paragraph, $pagetitle is preserved but not used
 *
 * Training and test data will contain exactly four negative examples for every positive example.
 *
 * Creates following outputs in tab-separated format
 *
 * - Train:
 *   - $pagename, $sectionpathlist, $posparagraph, $negpara1, $negpara2, $negpara3, $negpara4
 * - Test:
 *   - $pagename, $sectionpathlist, $paragraph, $judgment
 * - Cluster:
 *   - $pagename, $sectionpathlist, $posparagraph
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

        public JudgedInstance(Query query, String paragraphId, String paragraphContent, Judgment judgment) {
            super(query, paragraphId,paragraphContent);
            this.judgment = judgment;
        }

        public JudgedInstance(Instance instance, Judgment judgment){
            this(instance.query, instance.paragraphId, instance.paragraphContent, judgment);
        }


        public Judgment getJudgment() {
            return judgment;
        }

        @Override
        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(query.getQueryId());
            result.add(query.getPagename());
            result.add(query.getSectionPath());
            result.add(paragraphId);
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
                if (super.query != null ? !super.query.equals(super.query) : super.query != null) return false;
            return super.paragraphId != null ? super.paragraphId.equals(super.paragraphId) : super.paragraphId == null;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (judgment != null ? judgment.hashCode() : 0);
            result = 31 * result + (super.query != null ? super.query.hashCode() : 0);
            result = 31 * result + (super.paragraphId != null ? super.paragraphId.hashCode() : 0);
            return result;
        }
    }

    public static class Instance{
        protected final Query query;
        protected String paragraphId;
        protected String paragraphContent;


        public Instance(Query query, String paragraphId, String paragraphContent) {
            this.query = query;
            this.paragraphId = paragraphId;
            this.paragraphContent = paragraphContent.replaceAll("[\n\t\r]"," ");
            this.paragraphContent = paragraphContent;
        }


        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(query.queryId);
            result.add(query.pagename);
            result.add(query.sectionpath);
            result.add(paragraphId);
            result.add(paragraphContent);
            return result;
        }


        public String toTsvLine() {
            return StringUtils.join(toTsvSeqments(), "\t");
        }

        public String toQrelsLine() {

                List<String> result = new ArrayList<>();
                result.add(query.getQueryId());
                result.add("0");
                result.add(paragraphId);
                result.add("1");
                return StringUtils.join(result, " ");


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


        public InstanceWithNegatives(Query query, String paragraphId, String paragraphContent) {
            super(query, paragraphId, paragraphContent);
        }

        public InstanceWithNegatives(Instance instance){
            this(instance.query, instance.paragraphId, instance.paragraphContent);
        }


        public void addNegativeParagraph(String paragraphContent) {
            negativeParagraphs.add(paragraphContent.replaceAll("[\n\t\r]"," "));
        }

        @Override
        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(query.getQueryId());
            result.add(query.getPagename());
            result.add(query.getSectionPath());
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

            try{
                List<Instance> result = getInstances(page);

                for(Instance instance1:result) {
                    final InstanceWithNegatives instanceWithNegatives = new InstanceWithNegatives(instance1);
                    Set<Instance> paras = drawRandomParagraphs(result, 4, instance1.query.getSectionPath());

                    if(paras.size()==4) {  // only consider positive instances with 4 negative instances  --handled elsewhere
                        for (Instance instance2 : paras) {
                            instanceWithNegatives.addNegativeParagraph(instance2.paragraphContent);
                        }
                        megaresult.add(instanceWithNegatives);
                    } else {
                        System.err.println("Could not draw 4 elements from page "+page.getPageName());
                        System.out.println("instanceWithNegatives = " + instanceWithNegatives);
                    }

                }
            } catch (NotEnoughNegativesException ex){
                System.err.println("Not enough negatives for sectionpathlist "+ex.getNotSectionPathPrefix()+" in page "+page.getPageName());
            }

        }

        fileInputStream.close();

        return megaresult;
    }

    public List<JudgedInstance> extractTestData(final FileInputStream fileInputStream) throws CborException, IOException {
        List<JudgedInstance> megaresult = new ArrayList<JudgedInstance>();

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {

            try {

                List<Instance> result = getInstances(page);
                for (Instance instance : result) {
                    Set<Instance> paras = drawRandomParagraphs(result, 4, instance.query.getSectionPath());

                    if (paras.size() == 4) {  // only consider positive instances with 4 negative instances  --handled elsewhere

                        for (Instance negInstance : paras) {
                            JudgedInstance negative =
                                    new JudgedInstance(instance.query,
                                            negInstance.paragraphId,
                                            negInstance.paragraphContent,
                                            JudgedInstance.Judgment.SameArticleWrongSection);
                            megaresult.add(negative);
                        }
                        final JudgedInstance positive = new JudgedInstance(instance, JudgedInstance.Judgment.Relevant);
                        megaresult.add(positive);
                    }
                }
            } catch (NotEnoughNegativesException ex){
                System.err.println("Not enough negatives for sectionpathlist "+ex.getNotSectionPathPrefix()+" in page "+page.getPageName());
            }
        }

        fileInputStream.close();

        return megaresult;
    }




    static class Query {
        private final String pagename;
        private final List<String> sectionpathlist;
        private final String sectionpath;
        private String queryId;

        public Query(String pagename, List<String> sectionpathlist, String queryId) {

            this.pagename = pagename;
            this.sectionpathlist = sectionpathlist;
            this.sectionpath = StringUtils.join(sectionpathlist," ");
            this.queryId = queryId;
        }

        public String getSectionPath() {
            return sectionpath;
        }

        public String getPagename() {
            return pagename;
        }

        public List<String> getSectionPathList() {
            return sectionpathlist;
        }

        public String getQueryId() {
            return queryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Query)) return false;

            Query query = (Query) o;

            return getQueryId() != null ? getQueryId().equals(query.getQueryId()) : query.getQueryId() == null;
        }

        @Override
        public int hashCode() {
            return getQueryId() != null ? getQueryId().hashCode() : 0;
        }
    }

    public Map<String, Query> extractQueries(final FileInputStream fileInputStream) throws CborException, IOException {
        Map<String,Query> queryMap = new HashMap<>();

        for(Data.Page page: DeserializeData.iterableAnnotations((fileInputStream))){
            for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
                Query q = new Query(page.getPageName(), Data.sectionPathHeadings(sectionPath), Data.sectionPathId(page.getPageId(), sectionPath));
                queryMap.put(q.getQueryId(), q);
            }
        }
        return queryMap;
    }

    public List<Instance> extractClusteringData(final FileInputStream fileInputStream) throws CborException, IOException {
        List<Instance> megaresult = new ArrayList<Instance>();

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
            try{
                List<Instance> result = getInstances(page);
                megaresult.addAll(result);
            } catch (NotEnoughNegativesException ex){
                System.err.println("Not enough negatives for sectionpathlist "+ex.getNotSectionPathPrefix()+" in page "+page.getPageName());
            }

        }

        fileInputStream.close();

        return megaresult;
    }

    /**
     * Get instances from a page, exclude forbidden and empty headings, then filter instances with less than four negatives.
     * @param page
     * @return
     */
    private List<Instance> getInstances(Data.Page page) throws NotEnoughNegativesException {
        List<Instance> result = new ArrayList<Instance>();

        for(Data.Page.SectionPathParagraphs sectpara:page.flatSectionPathsParagraphs()) {
            final List<Data.Section> sectionpathList = sectpara.getSectionPath();

            boolean isExcludeItem = false;

            if(sectpara.getSectionPath().isEmpty()) isExcludeItem = true; // skip lead paragraph
            for(Data.Section section: sectpara.getSectionPath()) {
                if (forbiddenHeadings.contains(section.getHeading().toLowerCase())) isExcludeItem = true;
            }

            final String paraId = sectpara.getParagraph().getParaId();
            final String paratext = sectpara.getParagraph().getTextOnly();

            if(paratext.length()<10) isExcludeItem = true;

            if(!isExcludeItem) {
                Instance line = new Instance(
                        new Query(page.getPageName(),
                        Data.sectionPathHeadings(sectionpathList),
                        Data.sectionPathId(page.getPageId(), sectionpathList)
                        )
                        , paraId, paratext);
                result.add(line);
            }
        }

        return filterInstancesWithFewNegatives(result, 4);
//        return result;
    }

    private List<Instance> filterInstancesWithFewNegatives(List<Instance> result, int minNegs) throws NotEnoughNegativesException {
        // kill sections that have less than four negatives
        HashMap<String, Integer> sectionCounts = new HashMap<>();
        for(Instance instance: result) {
            String sectionpath = instance.query.getSectionPath();
            int count = 0;
            if(sectionCounts.containsKey(sectionpath)){
                count = sectionCounts.get(sectionpath);
            }
            sectionCounts.put(sectionpath, count+1);
        }

        int totalCount = result.size();
//        HashSet<String> sectionsToDrop = new HashSet<>();
        for(String sectionpath:sectionCounts.keySet()){

            int count = 0;
            for(String sectionOther:sectionCounts.keySet()){
                if(sectionOther.startsWith(sectionpath))
                    count += sectionCounts.get(sectionOther);
            }

            if(totalCount-count< minNegs)
                throw new NotEnoughNegativesException(sectionpath);
//            if(totalCount-count< minNegs) sectionsToDrop.add(sectionpathlist);
        }

//        List<Instance> filteredResult = new ArrayList<Instance>();
//        for(Instance instance: result) {
//            if(!sectionsToDrop.contains(instance.sectionpathlist)) filteredResult.add(instance);
//        }
        return result;
    }


    //--------------------------------

    private static Set<Instance> drawRandomParagraphs(List<Instance> lines, int draws, String notSectionPathPrefix) throws NotEnoughNegativesException {
        Set<Instance> negativeHash = new HashSet<>();
        ArrayList<Instance> negatives = new ArrayList<>();
        for(Instance line:lines) {
            if (!line.query.getSectionPath().startsWith(notSectionPathPrefix)) {
                if(!negativeHash.contains(line)) {
                    negatives.add(line);
                    negativeHash.add(line);
                }
            }
        }

        Collections.shuffle(negatives);


        if(negatives.size()<draws){
            System.out.println("negatives.size() < draws; negatives.size()="+negatives.size());
        }

        Set<Instance> samples = new HashSet<>();
        samples.addAll(negatives.subList(0,Math.min(draws, negatives.size())));
        return samples;
    }
//
//    private static Set<Instance> drawRandomParagraphsOld(List<Instance> lines, int draws, String notSectionPathPrefix){
//        Set<Instance> samples = new HashSet<>();
//        int abortCounter = 100;
//        while (samples.size()<draws && abortCounter>0){
//            Instance sample = lines.get(new Random().nextInt(lines.size()));
//            if(!sample.sectionpathlist.startsWith(notSectionPathPrefix)){
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
        final String qrelsOutputFile = args[4];



        System.out.println("hashing query ids");

        Map<String, Query> queryMap;
        {
            SimpleCarTrainData wikistein = new SimpleCarTrainData();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            queryMap = wikistein.extractQueries(fileInputStream);

        }





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


        System.out.println("qrels");


        {
            SimpleCarTrainData wikistein = new SimpleCarTrainData();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

            List<Instance> testData = wikistein.extractClusteringData(fileInputStream);
            BufferedWriter qrelsWriter = new BufferedWriter(new FileWriter(new File(qrelsOutputFile)));
            for(Instance line: testData){
                qrelsWriter.write(line.toQrelsLine());
                qrelsWriter.newLine();
            }
            qrelsWriter.close();

        }

    }

    private static class NotEnoughNegativesException extends Throwable {
        private final String notSectionPathPrefix;

        public NotEnoughNegativesException(String notSectionPathPrefix) {
            this.notSectionPathPrefix = notSectionPathPrefix;
        }

        public String getNotSectionPathPrefix() {
            return notSectionPathPrefix;
        }
    }
}
