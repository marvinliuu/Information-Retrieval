package org.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;



public class CreateIndex {
    private IndexWriter writer;
    public CreateIndex(String indexDir) throws Exception{
        Directory directory = FSDirectory.open(Paths.get(indexDir)); //Get the directory to generate the index
        Analyzer analyzer = new StandardAnalyzer(); // Set Standard Analyzer as the create index analyzer
        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(directory, iwConfig);
    }
    public void close() throws Exception{
        writer.close();
    }
    public int index(String dataDir) throws Exception{
        File[] files = new File(dataDir).listFiles();
        for (File file : files){
            indexFile(file);
        }
        return writer.numRamDocs();
    }
    private void indexFile(File f) throws Exception {
        //System.out.println("Indexfile：" + f.getCanonicalPath());
        Document doc = getDocument(f);
        writer.addDocument(doc);
    }
    private Document getDocument(File f) throws Exception {
        Document doc = new Document();
        doc.add(new TextField("contents", new FileReader(f)));
        doc.add(new TextField("fileName", f.getName(), Field.Store.YES));
        doc.add(new TextField("fullPath", f.getCanonicalPath(), Field.Store.YES));
        return doc;
    }
    public static void main(String[] args) {
        try {
            System.out.println("***********Begin Preprocessing**********");
            BufferedReader file = new BufferedReader(new FileReader("data/cran.all.1400")); // Read the original big file
            String str;
            int n = 0;
            while ((str = file.readLine()) != null) {
                if (str.startsWith(".I")) n ++;
                File file1 = new File("data/cran_preprocess/cran" + n);
                if (!file1.exists()) file1.createNewFile();
                FileWriter fileWritter = new FileWriter("data/cran_preprocess/" + file1.getName(), true);
                fileWritter.write(str + "\n");
                fileWritter.close();
            }
            System.out.println("Split the big file into 1400 small files\n" +
                    "Directory:/data/cran_preprocess");
            System.out.println("**********Finish Preprocessing**********");
        } catch (IOException e) {
            System.out.println("hello");
        }
        System.out.println("**********Begin Creating Index**********");
        String indexDir = "data/index/"; // Directory of the index
        String dataDir = "data/cran_preprocess/"; // Directory of the full information
        CreateIndex createIndex = null;
        int numIndexed = 0;
        long start = System.currentTimeMillis(); // Time start to get index
        try {
            createIndex = new CreateIndex(indexDir);
            numIndexed = createIndex.index(dataDir);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                createIndex.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis(); //End time of create index
        System.out.println("Index：" + numIndexed + " files cost " + (end - start) + " ms");
        System.out.println("Directory:/data/index");
        System.out.println("**********Finish Creating Index**********");
    }
}
