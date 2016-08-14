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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lars on 8/5/16.
 */
public class Searcher{

    IndexSearcher indexSearcher;
    Analyzer analyzer;
    QueryParser parser;
    ArrayList<String> fields;

    private List<Document> lookString(String text) throws ParseException, QueryNodeException, IOException, org.apache.lucene.queryparser.classic.ParseException {
        List<Document> results = new ArrayList<>();
        for(String field : fields) {
            parser = new QueryParser(field, analyzer);
            Query query = parser.parse(text);
//            System.out.println("Results "+field.toString()+":"+indexSearcher.count(query));
            ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = indexSearcher.doc(hits[i].doc);
                results.add(hitDoc);
//                System.out.println("Hit:" + hitDoc.get(field));
            }
        }
        return results;
    }

    public List<Document> lookup(String text){
        try{
            return lookString(text);
//            return true;
        }catch (Exception e){
            System.out.println("lookup failed:"+e.getMessage());
            return null;
//            return false;
        }
    }

    private void init() throws IOException {
        this.analyzer = new StandardAnalyzer();
        Path Index = new java.io.File(Indexer.IndexLocation).toPath().toAbsolutePath();
//        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Index));
        IndexReader indexReader = DirectoryReader.open(Indexer.dir);
        this.indexSearcher = new IndexSearcher(indexReader);

        fields = new ArrayList<String>();
        fields.addAll(Arrays.asList("package", "parent", "tags","guids","acls"));
    }

    public Searcher(){
        try{
            init();
        }catch (IOException e){
            System.out.println("Searcher failed:"+e.getMessage());
        }
    }
}
