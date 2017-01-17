package edu.unh.cs.treccar.read_data;

import edu.unh.cs.treccar.Data;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dietz
 * Date: 12/9/16
 * Time: 5:17 PM
 */
public class ReadDataTest {


    public static void main(String[] args) throws Exception{
        System.setProperty("file.encoding", "UTF-8");

        if (args.length<3) {
            System.out.println("Command line parameters: articlefile outlinefile paragraphfile");
            System.exit(-1);
        }

        String articles = args[0];
        String outlines = args[1];
        String paragraphs = args[2];

        final FileInputStream fileInputStream3 = new FileInputStream(new File(articles));
        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream3)) {
            for (List<Data.Section> sectionPath : page.flatSectionPaths()){
                System.out.println(Data.sectionPathId(page.getPageId(), sectionPath)+"   \t "+Data.sectionPathHeadings(sectionPath));
            }
            System.out.println();
        }

        System.out.println("\n\n");
//
//        final FileInputStream fileInputStream3 = new FileInputStream(new File("release.articles"));
//        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream3)) {
//            for (List<String> line : page.flatSectionPaths()){
//                System.out.println(line);
//            }
//            System.out.println();
//        }
//
//        System.out.println("\n\n");
//
//        final FileInputStream fileInputStream4 = new FileInputStream(new File("release.articles"));
//        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream4)) {
//            for (Data.Page.SectionPathParagraphs line : page.flatSectionPathsParagraphs()){
//                System.out.println(line.getSectionPath()+"\t"+line.getParagraph().getTextOnly());
//            }
//            System.out.println();
//        }


        System.out.println("\n\n");
        final FileInputStream fileInputStream = new FileInputStream(new File(outlines));
        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
            System.out.println(page);
            System.out.println();
        }


        System.out.println("\n\n");

       final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphs));
        for(Data.Paragraph p: DeserializeData.iterableParagraphs(fileInputStream2)) {
            System.out.println(p);
            System.out.println();
        }


    }
}
