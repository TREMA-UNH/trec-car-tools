package edu.unh.cs.treccar_v2.read_data;

import edu.unh.cs.treccar_v2.Data;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * User: dietz
 * Date: 12/9/16
 * Time: 5:17 PM
 */
public class ReadDataTest {
    public static void usage() {
        System.out.println("Command line parameters: (pages|outlines|paragraphs) FILE");
        System.exit(-1);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");

        if (args.length<2)
            usage();

        String mode = args[0];
        if (mode.equals("pages")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
            for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
                System.out.println(page);
                System.out.println();
            }
        } else if (mode.equals("outlines")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream3 = new FileInputStream(new File(pagesFile));
            for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream3)) {
                for (List<Data.Section> sectionPath : page.flatSectionPaths()){
                    System.out.println(Data.sectionPathId(page.getPageId(), sectionPath)+"   \t "+Data.sectionPathHeadings(sectionPath));
                }
                System.out.println();
            }
        } else if (mode.equals("paragraphs")) {
            final String paragraphsFile = args[1];
            final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphsFile));
            for(Data.Paragraph p: DeserializeData.iterableParagraphs(fileInputStream2)) {
                System.out.println(p);
                System.out.println();
            }
        } else {
            usage();
        }

//        final FileInputStream fileInputStream4 = new FileInputStream(new File("release.articles"));
//        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream4)) {
//            for (Data.Page.SectionPathParagraphs line : page.flatSectionPathsParagraphs()){
//                System.out.println(line.getSectionPath()+"\t"+line.getParagraph().getTextOnly());
//            }
//            System.out.println();
//        }
    }

}
