package edu.unh.cs.treccar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: dietz
 * Date: 12/10/16
 * Time: 11:55 AM
 */
public class Data {


    public static interface PageSkeleton {
    }


    public final static class Page {
        private final String pageName;
        private final List<PageSkeleton> skeleton;
        private final ArrayList<Section> childSections;

        public Page(String pageName, List<PageSkeleton> skeleton) {
            this.pageName = pageName;
            this.skeleton = skeleton;
            this.childSections = new ArrayList<Section>();
            for(PageSkeleton skel : skeleton) {
                if (skel instanceof Section) childSections.add((Section) skel);
            }

        }

        public String getPageName() {
            return pageName;
        }

        public List<PageSkeleton> getSkeleton() {
            return skeleton;
        }

        public ArrayList<Section> getChildSections() {
            return childSections;
        }

        private static List<List<String>> flatSectionPaths_(List<String> prefix, List<Section> headings) {
            List<List<String>> result = new ArrayList<>();

            for(Section heading : headings){
                final List<String> newPrefix = new ArrayList<String>();
                newPrefix.addAll(prefix);
                newPrefix.add(heading.getTitle());

                if(heading.getChildSections().isEmpty()) {
                    result.add(newPrefix);
                } else {
                    result.addAll(flatSectionPaths_(newPrefix,heading.getChildSections()));
                }

            }
            return result;

        }

        public List<List<String>> flatSectionPaths() {
            return flatSectionPaths_(Collections.<String>emptyList(), childSections);
        }


        static public class SectionPathParagraphs{
            private final List<String> sectionPath;
            private final Paragraph paragraph;

            public SectionPathParagraphs(List<String> sectionPath, Paragraph paragraph) {
                this.sectionPath = sectionPath;
                this.paragraph = paragraph;
            }

            public List<String> getSectionPath() {
                return sectionPath;
            }

            public Paragraph getParagraph() {
                return paragraph;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                SectionPathParagraphs that = (SectionPathParagraphs) o;

                if (sectionPath != null ? !sectionPath.equals(that.sectionPath) : that.sectionPath != null)
                    return false;
                return paragraph != null ? paragraph.equals(that.paragraph) : that.paragraph == null;
            }

            @Override
            public int hashCode() {
                int result = sectionPath != null ? sectionPath.hashCode() : 0;
                result = 31 * result + (paragraph != null ? paragraph.hashCode() : 0);
                return result;
            }

            @Override
            public String toString() {
                return "SectionPathParagraphs{" +
                        "sectionPath=" + sectionPath +
                        ", paragraph=" + paragraph +
                        '}';
            }
        }
        private static List<SectionPathParagraphs> flatSectionPathsParagraphs_(List<String> prefix, List<PageSkeleton> skeletonList) {
            List<SectionPathParagraphs> result = new ArrayList<>();

            for(PageSkeleton skel : skeletonList){
                if(skel instanceof Section) {
                    Section section = (Section) skel;

                    final List<String> newPrefix = new ArrayList<String>();
                    newPrefix.addAll(prefix);
                    newPrefix.add(section.getTitle());

                    if (section.getChildren().isEmpty()) {

                    } else {
                        result.addAll(flatSectionPathsParagraphs_(newPrefix, section.getChildren()));
                    }

                }

                if(skel instanceof Para) {
                    Paragraph paragraph = ((Para) skel).getParagraph();
                    result.add(new SectionPathParagraphs(prefix, paragraph));
                }
            }
            return result;
        }


        public List<SectionPathParagraphs> flatSectionPathsParagraphs() {
            return flatSectionPathsParagraphs_(Collections.<String>emptyList(), getSkeleton());
        }





        @Override
        public String toString() {
            return "Page{" +
                    "pageName='" + pageName + '\'' +
                    ", skeleton=" + skeleton +
                    '}';
        }
    }

    public final static class Section implements PageSkeleton{
        private final String title;
        private final List<PageSkeleton> children;
        private final List<Section> childSections;

        public Section(String title, List<PageSkeleton> children) {
            this.title = title;
            this.children = children;
            this.childSections = new ArrayList<Section>();
            for(PageSkeleton skel : children) {
                if (skel instanceof Section) childSections.add((Section) skel);
            }

        }

        public String getTitle() {
            return title;
        }

        public List<PageSkeleton> getChildren() {
            return children;
        }

        public List<Section> getChildSections() { return childSections; }

        @Override
        public String toString() {
            return "Section{" +
                    "title='" + title + '\'' +
                    ", children=" + children +
                    '}';
        }
    }

    public static interface ParaBody {
    }

    public final static class Para implements PageSkeleton {
        private final Paragraph paragraph;

        public Para(Paragraph paragraph) {
            this.paragraph = paragraph;
        }

        public Paragraph getParagraph() {
            return paragraph;
        }

        @Override
        public String toString() {
            return "Para{" +
                    "paragraph=" + paragraph +
                    '}';
        }
    }

    public final static class Paragraph  {
        private final String paraId;
        private final List<ParaBody> bodies;

        public Paragraph(String paraId, List<ParaBody> bodies) {
            this.paraId = paraId;
            this.bodies = bodies;
        }

        public String getParaId() {
            return paraId;
        }

        public List<ParaBody> getBodies() {
            return bodies;
        }

        @Override
        public String toString() {
            return "Paragraph{" +
                    "paraId='" + paraId + '\'' +
                    ", bodies=" + bodies +
                    '}';
        }

        public String getTextOnly() {
            String result = "";
            for(ParaBody body: bodies){
                if(body instanceof ParaLink){
                    result += ((ParaLink) body).getAnchorText();
                }
                else if (body instanceof ParaText){
                    result += ((ParaText)body).getText();
                }
            }
            return result;
        }

        public List<String> getEntitiesOnly() {
            List<String> result = new ArrayList<>();
            for(ParaBody body: bodies){
                if(body instanceof ParaLink){
                    result.add(((ParaLink) body).getPage());
                }
            }
            return result;
        }
    }



    public final static class ParaText implements ParaBody {
        private final String text;

        public ParaText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "ParaText{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    public final static class ParaLink implements ParaBody {
        private final String anchorText;
        private final String page;

        public ParaLink(String page, String anchorText) {
            this.anchorText = anchorText;
            this.page = page;
        }

        public String getAnchorText() {
            return anchorText;
        }

        public String getPage() {
            return page;
        }

        @Override
        public String toString() {
            return "ParaLink{" +
                    "anchorText='" + anchorText + '\'' +
                    ", page='" + page + '\'' +
                    '}';
        }
    }

}
