package org.example.lucene;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.*;
import java.util.*;

public class searcher {

    public static void search(String indexDir, String q) throws Exception {
        // Get the directory of the index file
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        // Get all files through the directory of the index file
        IndexReader reader = DirectoryReader.open(dir);
        // build index searcher
        IndexSearcher is = new IndexSearcher(reader);
        // 实例化分析器
        //Analyzer analyzer = new EnglishAnalyzer();
        Analyzer analyzer = new StandardAnalyzer();
        //is.setSimilarity(new BM25Similarity());
        //is.setSimilarity(new ClassicSimilarity());
        is.setSimilarity(new BooleanSimilarity());

        QueryParser parser = new QueryParser("contents", analyzer); // The first parameter is the query you want to search, Second parameter is the analyzer you choice
        // 根据传进来的p查找
        Query query = parser.parse(q);

        long start = System.currentTimeMillis();//Calculate the starting time
        TopDocs hits = is.search(query, 10);
        System.out.println(query);
        long end = System.currentTimeMillis(); // Calculate the ending time.
        System.out.println("**********Begin Searching**********");
        System.out.println("Q: " + q);
        System.out.println("Get " + hits.totalHits + " records" + "Cost " + (end - start) + "ms");
        System.out.println("FIrst 10 Doc:");
        // 遍历hits.scoreDocs，得到scoreDoc
        /**
         * ScoreDoc:得分文档,即得到文档 scoreDocs:代表的是topDocs这个文档数组
         *
         * @throws Exception
         */
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println("Score: " + scoreDoc.score + " " + doc.get("fullPath"));
            //System.out.println(scoreDoc.score);
        }
        System.out.println("**********Finish Searching**********");

        // 关闭reader
        //reader.close();
        try {
            String[] docIdArr = new String[300];
            String[] qryArr = new String[300];
            docIdArr = createDocArray();
            qryArr = createQryArray();
            // support * and ? in qurey
            parser.setAllowLeadingWildcard(true);
            // answer.test will put the search answer
            Writer writer = new FileWriter(new File("data/test/answer.test"));
            int i = 0;
            while (i < docIdArr.length) {
                Query query1 = parser.parse(qryArr[i]); // 指定==>搜索域为content(即上一行代码指定的"content")中包含"java"的文档
                TopDocs tds = is.search(query1, 1000); // 第二个参数指定搜索后显示的条数,若查到5条则显示为5条,查到15条则只显示10条
                ScoreDoc[] sds = tds.scoreDocs; // TopDocs中存放的并不是我们的文档,而是文档的ScoreDoc对象
                int rank = 1;
                for (ScoreDoc scoreDoc : tds.scoreDocs) {
                    Document doc = is.doc(scoreDoc.doc);
                    int lengthofdoc = doc.get("fileName").length();
                    writer.write(docIdArr[i] + "\s" + "Q0" + "\s" + doc.get("fileName").substring(4,lengthofdoc) + "\s" + rank++ + "\s" + scoreDoc.score + "\s" + "STANDARD" + "\n");
                }
                i++;
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reader.close();
    }
    public static String[] createDocArray () {
        String[] res = new String[225];
        int num = 0;
        try {
            FileInputStream inputStream = new FileInputStream("data/cran.qry");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String str = null;
            while((str = bufferedReader.readLine()) != null) {
                if (str.startsWith(".I")) {
                    res[num] = Integer.toString(num + 1);
                    num++;
                }
            }
        }  catch (Exception e) {
            System.out.println("Can not read the cran.qry file");
            e.printStackTrace();
        }
        return res;
    }

    public static String[] createQryArray () {
        String[] res = new String[300];
        int num = 0;
        try {
            FileInputStream inputStream = new FileInputStream("data/cran.qry");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String str = null;
            String qry = "";
            String[] arr = new String[2];
            while((str = bufferedReader.readLine()) != null) {
                if (!str.startsWith(".I") && !str.startsWith(".W")) {
                    qry = qry + "\s" + str;
                } else if (str.startsWith(".I")) {
                    if (qry.length() > 0) {
                        res[num++] = qry;
                        qry = "";
                    }

                }
            }
            res[num] = qry;
        }  catch (Exception e) {
            System.out.println("Can not read the cran.qry file");
            e.printStackTrace();
        }
        return res;
    }
    public static String findQueryText (int number) {
        String[] qryArr = new String[300];
        qryArr = createQryArray();
        String res = "";
        int num = 0;
        String[] arr = new String[2];
        try {
            FileInputStream inputStream = new FileInputStream("data/cran.qry");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String str = null;
            while((str = bufferedReader.readLine()) != null) {
                if (str.startsWith(".I")) {
                    arr = str.split(" +");
                    if (Integer.valueOf(arr[1])  == number ) {
                        return qryArr[num];
                    }
                    num++;
                }
            }
        }  catch (Exception e) {
            System.out.println("Can not read the cran.qry file");
            e.printStackTrace();
        }
        return res;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Please choose the way how you want to enter your query.\n" +
                "1. Question Number\n" +
                "2. Text");
        Scanner sc = new Scanner(System.in);
        int c = sc.nextInt();
        String q = "";
        if (c == 1) {
            System.out.println("Please enter the question number:");
            int num = sc.nextInt();
            q = findQueryText(num);
        } else if (c == 2) {
            System.out.println("Please enter the question");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            q = br.readLine();
        }
        String indexDir = "data/index/";
        try {
            search(indexDir, q);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
