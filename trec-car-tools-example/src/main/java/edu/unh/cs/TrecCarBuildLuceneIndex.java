package edu.unh.cs;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.CborFileTypeException;
import edu.unh.cs.treccar_v2.read_data.CborRuntimeException;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;

/*
 * User: dietz
 * Date: 1/4/18
 * Time: 1:23 PM
 */

/**
 * Example of how to build a lucene index of trec car paragraphs
 */
public class TrecCarBuildLuceneIndex {

    private static void usage() {
        System.out.println("Command line parameters: paragraphs CBOR INDEX");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");

        if (args.length < 3)
            usage();

        String mode = args[0];
        String indexPath = args[2];

        if (mode.equals("paragraphs")) {
            final String paragraphsFile = args[1];
            final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphsFile));

            final IndexWriter indexWriter = setupIndexWriter(indexPath);
            final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fileInputStream2);

            ParaToLuceneIterator docsIter = new ParaToLuceneIterator(paragraphIterator);
            indexWriter.addDocuments(toIterable(docsIter));

            indexWriter.commit();
            indexWriter.close();
        }
    }

    private static Iterable<Document> toIterable(final Iterator<Document> iter) throws CborRuntimeException, CborFileTypeException {
        return new Iterable<Document>() {
            @Override
            @NotNull
            public Iterator<Document> iterator() {
                return iter;
            }
        };
    }


    public static class ParaToLuceneIterator implements Iterator<Document> {
        private final Iterator<Data.Paragraph> paragraphIterator;

        ParaToLuceneIterator(Iterator<Data.Paragraph> paragraphIterator){
            this.paragraphIterator = paragraphIterator;
        }

        @Override
        public boolean hasNext() {
            return this.paragraphIterator.hasNext();
        }

        @Override
        public Document next() {
            Data.Paragraph p = this.paragraphIterator.next();
            final Document doc = new Document();
            doc.add(new TextField("text", p.getTextOnly(), Field.Store.YES));
            return doc;
        }

        @Override
        public void remove() {
            this.paragraphIterator.remove();
        }


    }


    @NotNull
    private static IndexWriter setupIndexWriter(String indexPath) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, "paragraph.lucene");
        Directory indexDir = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        return new IndexWriter(indexDir, config);
    }
}