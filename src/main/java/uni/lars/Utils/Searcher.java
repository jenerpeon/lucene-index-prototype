package uni.lars.Utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.surround.parser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by uni on 8/5/16.
 */
public class Searcher {

    private IndexSearcher indexSearcher;
    private Indexer indexer;
    private Analyzer analyzer;
    private QueryParser parser;

    private List<Document> lookString(String text) throws ParseException, QueryNodeException, IOException, org.apache.lucene.queryparser.classic.ParseException {
        List<Document> results = new ArrayList<>();
        for (String field : indexer.getFields()) {
            parser = new QueryParser(field, analyzer);
            Query query = parser.parse(text);

            ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = indexSearcher.doc(hits[i].doc);
                results.add(hitDoc);
//                System.out.println("Hit:" + hitDoc.get(field));
            }
        }
        return results;
    }

    private Analyzer getCustomAnalyzer() {
        return new StandardAnalyzer();
    }

    private List<Document> LookupTemplate(String queryTxt, List<String> fields) throws IOException {
        List<List<Document>> lst = fields.stream()
                .filter(f -> indexer.getFields().contains(f))
                .map(field -> {

                            try {
                                parser = new QueryParser(field, analyzer);
                                Query query = parser.parse(queryTxt);
                                return Arrays.asList(indexSearcher.search(query, 1000000).scoreDocs).stream()
                                        .map(r -> {
                                            try {
                                                return indexSearcher.doc(r.doc);
                                            } catch (Exception e) {
                                                return null;
                                            }
                                        })
                                        .collect(Collectors.toCollection(ArrayList::new));
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                return null;
                            }
                        }
                ).collect(Collectors.toCollection(ArrayList::new));

        System.out.println("generated list" + lst.get(0).toString() +"\nsize: " + lst.get(0).size());

        return new ArrayList<Document>();
    }


    public List<Document> lookup(String text) {
        List<String> selectors = Arrays.asList("package", "tags");
        try {
            Timeit.code(() -> {
                try {
                    LookupTemplate(text, selectors);
                } catch (Exception e) {
                    System.out.println(e);
                }
            });
            return lookString(text);
//            return true;
        } catch (Exception e) {
            System.out.println("lookup failed:" + e.getMessage());
            return null;
//            return false;
        }
    }

    private void init() throws IOException {
        indexer = Indexer.getInstance();
        analyzer = new StandardAnalyzer();
        IndexReader indexReader = DirectoryReader.open(Indexer.dir);
        this.indexSearcher = new IndexSearcher(indexReader);
    }

    public String printIndex() {

        String buf = "";
        for (Document doc : lookup("com*")) {
            for (IndexableField field : doc.getFields()) {
                buf += field.stringValue();
            }

        }

        return buf;

    }

    public Searcher() {
        try {
            init();
        } catch (IOException e) {
            System.out.println("Searcher failed:" + e.getMessage());
        }
    }
}
