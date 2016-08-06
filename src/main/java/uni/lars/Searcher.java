package uni.lars;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.surround.parser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by lars on 8/5/16.
 */
public class Searcher{

    IndexSearcher indexSearcher;
    Analyzer analyzer;
    QueryParser parser;
    String field = "contents";

    private void lookString(String text) throws ParseException, QueryNodeException, IOException, org.apache.lucene.queryparser.classic.ParseException {
        parser = new QueryParser(field, analyzer);
        Query query = parser.parse("WATT");
        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;
        System.out.println(hits.length);
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = indexSearcher.doc(hits[i].doc);
            System.out.println("indexed Text:"+ hitDoc.get(field));
        }
    }

    public boolean lookup(String text){
        try{
            lookString(text);
            return true;

        }catch (Exception e){
            System.out.println("lookup failed:"+e.getMessage());
            return false;
        }
    }

    private void init() throws IOException {
        this.analyzer = new StandardAnalyzer();
        Path Index = new java.io.File(Indexer.IndexLocation).toPath();
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Index));
        this.indexSearcher = new IndexSearcher(indexReader);
    }

    public Searcher(){
        try{
            init();
        }catch (IOException e){
            System.out.println("Searcher failed:"+e.getMessage());
        }
    }
}
