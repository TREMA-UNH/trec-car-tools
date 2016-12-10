package edu.unh.cs.treccar;

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
        String pageName;
        List<PageSkeleton> skeleton;

        public Page(String pageName, List<PageSkeleton> skeleton) {
            this.pageName = pageName;
            this.skeleton = skeleton;
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

    public static interface ParaBody {
    }

    public final static class Para implements PageSkeleton {
        Paragraph paragraph;

        public Para(Paragraph paragraph) {
            this.paragraph = paragraph;
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

}
