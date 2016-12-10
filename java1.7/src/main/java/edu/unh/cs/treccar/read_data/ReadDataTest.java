package edu.unh.cs.treccar.read_data;

import edu.unh.cs.treccar.Data;

import java.io.File;
import java.io.FileInputStream;

/**
 * User: dietz
 * Date: 12/9/16
 * Time: 5:17 PM
 */
public class ReadDataTest {

    public static void main(String[] args) throws Exception{
        final FileInputStream fileInputStream = new FileInputStream(new File("release.outline"));
        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
            System.out.println(page);
            System.out.println();
        }


        System.out.println("\n\n");

       final FileInputStream fileInputStream2 = new FileInputStream(new File("release.paragraphs"));
        for(Data.Paragraph p: DeserializeData.iterableParagraphs(fileInputStream2)) {
            System.out.println(p);
            System.out.println();
        }


    }
}
