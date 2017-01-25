package edu.unh.cs.treccar;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: dietz
 * Date: 12/10/16
 * Time: 11:55 AM
 */
public class Data {
    public static String sectionPathId(String pageId, List<Section> sections){
        List<String> result = new ArrayList<>();
        result.add(pageId);
        for(Section section:sections){
            result.add(section.headingId);
        }
        return StringUtils.join(result, "/");
    }

    public static List<String> sectionPathHeadings(List<Section> sections){
        List<String> result = new ArrayList<>();
        for(Section section:sections){
            result.add(section.heading);
        }
        return result;
    }

    public static interface PageSkeleton {
    }


    public final static class Page {
        private final String pageName;
        private final String pageId;
        private final List<PageSkeleton> skeleton;
        private final ArrayList<Section> childSections;

        public Page(String pageName, String pageId, List<PageSkeleton> skeleton) {
            this.pageName = pageName;
            this.pageId = pageId;
            this.skeleton = skeleton;
            this.childSections = new ArrayList<Section>();
            for(PageSkeleton skel : skeleton) {
                if (skel instanceof Section) childSections.add((Section) skel);
            }

        }

        public String getPageName() {
            return pageName;
        }

        public String getPageId() {
            return pageId;
        }

        public List<PageSkeleton> getSkeleton() {
            return skeleton;
        }

        public ArrayList<Section> getChildSections() {
            return childSections;
        }

        private static List<List<Section>> flatSectionPaths_(List<Section> prefix, List<Section> headings) {
            List<List<Section>> result = new ArrayList<>();

            for(Section heading : headings){
                final List<Section> newPrefix = new ArrayList<>();
                newPrefix.addAll(prefix);
                newPrefix.add(heading);

                if(heading.getChildSections().isEmpty()) {
                    result.add(newPrefix);
                } else {
                    result.addAll(flatSectionPaths_(newPrefix,heading.getChildSections()));
                }

            }
            return result;

        }

        public List<List<Section>> flatSectionPaths() {
            return flatSectionPaths_(Collections.<Section>emptyList(), childSections);
        }


        static public class SectionPathParagraphs{
            private final List<Section> sectionPath;
            private final Paragraph paragraph;

            public SectionPathParagraphs(List<Section> sectionPath, Paragraph paragraph) {
                this.sectionPath = sectionPath;
                this.paragraph = paragraph;
            }

            public List<Section> getSectionPath() {
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
        private static List<SectionPathParagraphs> flatSectionPathsParagraphs_(List<Section> prefix, List<PageSkeleton> skeletonList) {
            List<SectionPathParagraphs> result = new ArrayList<>();

            for(PageSkeleton skel : skeletonList){
                if(skel instanceof Section) {
                    Section section = (Section) skel;

                    final List<Section> newPrefix = new ArrayList<>();
                    newPrefix.addAll(prefix);
                    newPrefix.add(section);

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
            return flatSectionPathsParagraphs_(Collections.<Section>emptyList(), getSkeleton());
        }





        @Override
        public String toString() {
            return "Page{" +
                    "pageName='" + pageName + '\'' +
                    ", skeleton=" + skeleton +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Page)) return false;

            Page page = (Page) o;

            if (getPageId() != null ? !getPageId().equals(page.getPageId()) : page.getPageId() != null) return false;
            return getSkeleton() != null ? getSkeleton().equals(page.getSkeleton()) : page.getSkeleton() == null;
        }

        @Override
        public int hashCode() {
            int result = getPageId() != null ? getPageId().hashCode() : 0;
            result = 31 * result + (getSkeleton() != null ? getSkeleton().hashCode() : 0);
            return result;
        }
    }

    public final static class Section implements PageSkeleton{
        private final String heading;
        private final String headingId;
        private final List<PageSkeleton> children;
        private final List<Section> childSections;

        public Section(String heading, String headingId,  List<PageSkeleton> children) {
            this.heading = heading;
            this.headingId = headingId;
            this.children = children;
            this.childSections = new ArrayList<Section>();
            for(PageSkeleton skel : children) {
                if (skel instanceof Section) childSections.add((Section) skel);
            }

        }

        public String getHeading() {
            return heading;
        }

        public String getHeadingId() {
            return headingId;
        }

        public List<PageSkeleton> getChildren() {
            return children;
        }

        public List<Section> getChildSections() { return childSections; }


        @Override
        public String toString() {
            return "Section{" +
                    "heading='" + heading + '\'' +
                    ", headingId='" + headingId + '\'' +
                    ", children=" + children +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Section)) return false;

            Section section = (Section) o;

            if (getHeadingId() != null ? !getHeadingId().equals(section.getHeadingId()) : section.getHeadingId() != null)
                return false;
            return getChildren() != null ? getChildren().equals(section.getChildren()) : section.getChildren() == null;
        }

        @Override
        public int hashCode() {
            int result = getHeadingId() != null ? getHeadingId().hashCode() : 0;
            result = 31 * result + (getChildren() != null ? getChildren().hashCode() : 0);
            return result;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Para)) return false;

            Para para = (Para) o;

            return getParagraph() != null ? getParagraph().equals(para.getParagraph()) : para.getParagraph() == null;
        }

        @Override
        public int hashCode() {
            return getParagraph() != null ? getParagraph().hashCode() : 0;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Paragraph)) return false;

            Paragraph paragraph = (Paragraph) o;

            return getParaId() != null ? getParaId().equals(paragraph.getParaId()) : paragraph.getParaId() == null;
        }

        @Override
        public int hashCode() {
            return getParaId() != null ? getParaId().hashCode() : 0;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParaText)) return false;

            ParaText paraText = (ParaText) o;

            return getText() != null ? getText().equals(paraText.getText()) : paraText.getText() == null;
        }

        @Override
        public int hashCode() {
            return getText() != null ? getText().hashCode() : 0;
        }
    }

    public final static class ParaLink implements ParaBody {
        private final String linkSection;
        private final String pageId;
        private final String anchorText;
        private final String page;

        public ParaLink(String pageId, String anchorText, String page) {
            this.pageId = pageId;
            this.anchorText = anchorText;
            this.page = page;
            this.linkSection = null;
        }

        public ParaLink(String page, String pageId, String linkSection, String anchorText) {
            this.linkSection = linkSection;
            this.pageId = pageId;
            this.anchorText = anchorText;
            this.page = page;
        }

        public boolean hasLinkSection(){
            return this.linkSection != null;
        }

        /** May return null if not defined. Check with #hasLinkSection*/
        public String getLinkSection() {
            return linkSection;
        }

        public String getPageId() {
            return pageId;
        }

        public String getAnchorText() {
            return anchorText;
        }

        public String getPage() {
            return page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParaLink)) return false;

            ParaLink paraLink = (ParaLink) o;

            if (getLinkSection() != null ? !getLinkSection().equals(paraLink.getLinkSection()) : paraLink.getLinkSection() != null)
                return false;
            if (getPageId() != null ? !getPageId().equals(paraLink.getPageId()) : paraLink.getPageId() != null)
                return false;
            if (getAnchorText() != null ? !getAnchorText().equals(paraLink.getAnchorText()) : paraLink.getAnchorText() != null)
                return false;
            return getPage() != null ? getPage().equals(paraLink.getPage()) : paraLink.getPage() == null;
        }

        @Override
        public int hashCode() {
            int result = getLinkSection() != null ? getLinkSection().hashCode() : 0;
            result = 31 * result + (getPageId() != null ? getPageId().hashCode() : 0);
            result = 31 * result + (getAnchorText() != null ? getAnchorText().hashCode() : 0);
            result = 31 * result + (getPage() != null ? getPage().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ParaLink{" +
                    " page='" + page + '\'' +
                    ", linkSection='" + linkSection + '\'' +
                    ", pageId='" + pageId + '\'' +
                    ", anchorText='" + anchorText + '\'' +
                    '}';
        }
    }

}
