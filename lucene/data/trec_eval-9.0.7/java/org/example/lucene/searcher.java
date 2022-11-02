package org.example.lucene;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.*;


public class searcher {

    public static void search(String indexDir, String q) throws Exception {
        // Get the directory of the index file
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        // Get all files through the directory of the index file
        IndexReader reader = DirectoryReader.open(dir);
        // build index searcher
        IndexSearcher is = new IndexSearcher(reader);
        // 实例化分析器
        Analyzer analyzer = new StandardAnalyzer();
        //is.setSimilarity(new BM25Similarity());
        is.setSimilarity(new ClassicSimilarity());
        // 建立查询解析器
        /**
         * 第一个参数是要查询的字段； 第二个参数是分析器Analyzer
         */
        QueryParser parser = new QueryParser("contents", analyzer);
        // 根据传进来的p查找
        Query query = parser.parse(q);
        // 计算索引开始时间
        long start = System.currentTimeMillis();
        // 开始查询
        /**
         * 第一个参数是通过传过来的参数来查找得到的query； 第二个参数是要出查询的行数
         */
        TopDocs hits = is.search(query, 10);
        // 计算索引结束时间
        long end = System.currentTimeMillis();
        System.out.println("匹配 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");
        // 遍历hits.scoreDocs，得到scoreDoc
        /**
         * ScoreDoc:得分文档,即得到文档 scoreDocs:代表的是topDocs这个文档数组
         *
         * @throws Exception
         */
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullPath"));
            System.out.println(scoreDoc.score);
        }

        // 关闭reader
        //reader.close();
        try {
            String[] docIdArr = new String[300];
            String[] qryArr = new String[300];
            docIdArr = createDocArray();
            qryArr = createQryArray();
            String docId = new String();
            // support * and ? in qurey
            parser.setAllowLeadingWildcard(true);
            // answer.test will put the search answer
            Writer writer = new FileWriter(new File("data/test/answer.test"));
            int i = 0;
            while (i < docIdArr.length) {
                Query query1 = parser.parse(qryArr[i]); // 指定==>搜索域为content(即上一行代码指定的"content")中包含"java"的文档
                TopDocs tds = is.search(query1, 10000); // 第二个参数指定搜索后显示的条数,若查到5条则显示为5条,查到15条则只显示10条
                ScoreDoc[] sds = tds.scoreDocs; // TopDocs中存放的并不是我们的文档,而是文档的ScoreDoc对象

                FileInputStream inputRelId = new FileInputStream("data/question/" + Integer.parseInt(docIdArr[i]) + ".txt");
                BufferedReader bfreader = new BufferedReader(new InputStreamReader(inputRelId));

                int rank = 1;
                while ((docId = bfreader.readLine()) != null) {
                   // Document doc = is.doc(Integer.parseInt(docId)); // docId 得到的是文档的序号
                    ScoreDoc qryScoreDoc = findDoc(sds, docId, is);
                    if (qryScoreDoc == null) {
                        System.out.println(docId + "  " + docIdArr[i]);
                        continue;
                    }
                    writer.write(docIdArr[i] + "\s" + "Q0" + "\s");
                    writer.write(docId + "\s" + rank++ + "\s" + qryScoreDoc.score + "\s" + "STANDARD" + "\n");
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
    public static ScoreDoc findDoc (ScoreDoc[] sds,String docId,IndexSearcher searcher) {
        try {
            for (ScoreDoc sd : sds) {
                Document doc = searcher.doc(sd.doc);
                if (doc.get("fileName").equals("cran" + docId)) {
                    return sd;
                }
            }
            return null;
        }  catch (Exception e) {
            System.out.println("An exception was encountered while searching for files. The stack trace is as follows");
            e.printStackTrace();
        }
        return null;
    }
    public static String removeSuffix(final String s, final String suffix)
    {
        if (s != null && suffix != null && s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }
    public static void main(String[] args) {
        String indexDir = "dataindex/";
        //我们要搜索的内容
        String q = "what are the structural and aeroelastic problems associated with flight of high speed aircraft .";

        try {
            search(indexDir, q);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
