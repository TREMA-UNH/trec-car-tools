package edu.unh.cs.treccar_v2;

import java.util.ArrayList;
import java.util.Objects;

/*
 * User: dietz
 * Date: 12/29/17
 * Time: 11:55 AM
 */


/**
 * Representation of header information of TREC CAR's Wikipedia dump.
 */
public class Header {
    /**
     * Content type of this CBOR archive, i.e., PagesFile, OutlinesFile, ParagraphsFile
     */
    public static enum FileType {
        PagesFile(0),
        OutlinesFile(1),
        ParagraphsFile(2);

        private int value;
        private FileType(int value) {
            this.value = value;
        }

        private static FileType[] values = null;
        public static FileType fromInt(int i) {
            if (FileType.values == null) {
                FileType.values = FileType.values();
            }
            return FileType.values[i];
        }
    };


    public final static class TrecCarHeader {
        final FileType fileType;
        final Provenance provenance;

        public TrecCarHeader(FileType fileType, Provenance provenance) {
            this.fileType = fileType;
            this.provenance = provenance;
        }

        public FileType getFileType() {
            return fileType;
        }

        public Provenance getProvenance() {
            return provenance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TrecCarHeader)) return false;
            TrecCarHeader that = (TrecCarHeader) o;
            return getFileType() == that.getFileType() &&
                    Objects.equals(getProvenance(), that.getProvenance());
        }

        @Override
        public int hashCode() {

            return Objects.hash(getFileType(), getProvenance());
        }

        @Override
        public String toString() {
            return "TrecCarHeader{" +
                    "\nfileType=" + fileType +
                    "\n, provenance=" + provenance +
                    "\n}";
        }



    }

    public final static class InvalidHeaderException extends Exception {
        public InvalidHeaderException() {}
    }


    /**
     * Page metadata containing information about categories, disambiguations, redirect names, and inlinks.
     */
    public final static class Provenance {
        private final ArrayList<SiteProvenance> siteProvenance = new ArrayList<>();
        private final String dataReleaseName;
        private final ArrayList<String> comments = new ArrayList<>();
        private final ArrayList<Transform> transforms = new ArrayList<>();

        public Provenance(String dataReleaseName) {
            this.dataReleaseName = dataReleaseName;
        }

        /**
         * List of site provenances, when the CBOR archive may combine content from different sites, which is reflected in different
         * siteIds of {@link Data.Page#getPageId()}.
         *
         * @return An list with a sinle entry in the case of single-source archives
         */
        public ArrayList<SiteProvenance> getSiteProvenance() {
            return siteProvenance;
        }

        /**
         *  Name of this data release, e.g., "trec-car v2.0"
         */
        public String getDataReleaseName() {
            return dataReleaseName;
        }

        /**
         * Free form comments about the origin of this CBOR archive.
         * @return
         */
        public ArrayList<String> getComments() {
            return comments;
        }

        /**
         * Documentation of transformation steps in the data creation pipeline.
         * @return
         */
        public ArrayList<Transform> getTransforms() {
            return transforms;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Provenance)) return false;
            Provenance that = (Provenance) o;
            return Objects.equals(getSiteProvenance(), that.getSiteProvenance()) &&
                    Objects.equals(getDataReleaseName(), that.getDataReleaseName());
        }

        @Override
        public int hashCode() {

            return Objects.hash(getSiteProvenance(), getDataReleaseName());
        }

        @Override
        public String toString() {
            return "Provenance{" +
                    "\nsiteProvenance=" + siteProvenance +
                    "\n, dataReleaseName='" + dataReleaseName + '\'' +
                    "\n, comments=" + comments +
                    "\n, transforms=" + transforms +
                    "\n}";
        }
    }




    /**
     * Representation site provenance TREC CAR data set, e.g., English Wikipedia.
     *
     * One CBOR archive may combine content from different sites, which is reflected in different
     * siteIds of {@link Data.Page#getPageId()}.
     */
    public final static class SiteProvenance {
        private String provSiteId;
        private String language;
        private String sourceName;
        private final ArrayList<String> siteComments = new ArrayList<>();

        public SiteProvenance(String provSiteId, String language, String sourceName) {
            this.provSiteId = provSiteId;
            this.language = language;
            this.sourceName = sourceName;
        }

        /**
         * Prefix of {@link Data.Page#getPageId()} indicating provenance from this site.
         */
        public String getProvSiteId() {
            return provSiteId;
        }

        public void setProvSiteId(String provSiteId) {
            this.provSiteId = provSiteId;
        }

        /**
         * Language of the site as IETF (RFC 5646, RFC 4647) language code, e.g., "en-US"
         */
        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        /**
         * Source name, for Wikipedia dumps the dump date, e.g., "20161220"
         */
        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        /**
         * Free text comments on the site.
         */
        public ArrayList<String> getSiteComments() {
            return siteComments;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SiteProvenance)) return false;
            SiteProvenance that = (SiteProvenance) o;
            return Objects.equals(getProvSiteId(), that.getProvSiteId()) &&
                    Objects.equals(getLanguage(), that.getLanguage()) &&
                    Objects.equals(getSourceName(), that.getSourceName());
        }

        @Override
        public int hashCode() {

            return Objects.hash(getProvSiteId(), getLanguage(), getSourceName());
        }

        @Override
        public String toString() {
            return "SiteProvenance{" +
                    "provSiteId='" + provSiteId + '\'' +
                    ", language='" + language + '\'' +
                    ", sourceName='" + sourceName + '\'' +
                    ", siteComments=" + siteComments +
                    '}';
        }
    }





    /**
     * Representation of a transformation step in the data generation pipeline
     */
    public final static class Transform {
        private final String toolName;
        private final String toolCommit;
        private String toolInfo = "<unknown>";

        public Transform(String toolName, String toolCommit) {
            this.toolName = toolName;
            this.toolCommit = toolCommit;
        }

        public String getToolName() {
            return toolName;
        }

        public String getToolCommit() {
            return toolCommit;
        }

        public String getToolInfo() {
            return toolInfo;
        }

        public void setToolInfo(String toolInfo) {
            this.toolInfo = toolInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Transform)) return false;
            Transform transform = (Transform) o;
            return Objects.equals(getToolName(), transform.getToolName()) &&
                    Objects.equals(getToolCommit(), transform.getToolCommit()) &&
                    Objects.equals(getToolInfo(), transform.getToolInfo());
        }

        @Override
        public int hashCode() {

            return Objects.hash(getToolName(), getToolCommit(), getToolInfo());
        }

        @Override
        public String toString() {
            return "Transform{" +
                    "toolName='" + toolName + '\'' +
                    ", toolCommit='" + toolCommit + '\'' +
                    ", toolInfo='" + toolInfo + '\'' +
                    '}';
        }
    }



}
