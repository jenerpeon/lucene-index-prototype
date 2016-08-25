package uni.lars.Utils;

import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by uni on 8/5/16.
 */
public class Indexer {

    public static String IndexLocation = "/tmp/index";
    public static String docDir = "/tmp/JsonDocuments";

    private static final Indexer instance = new Indexer(true);

    private final Set<String> fields = new HashSet<>();
    public static Directory dir;

    public Set<String> getFields(){
        return fields;
    }

    // capsulate indexWriter, to call new writer for every document to add. Always based on latest Index
    private IndexWriter InitPersistentWriter() throws IOException {
        Path Index = new java.io.File(IndexLocation).toPath();
        dir = FSDirectory.open(Index);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        return new IndexWriter(dir, iwc);

    }

    // capsulate indexWriter, to call new writer for every document to add. Always based on latest Index
    private IndexWriter InitVolatileWriter() throws IOException {
        this.dir = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        return new IndexWriter(dir, iwc);

    }

    // read JsonDocuments
    public List<Document> retrieveJsonDocuments(File dataDir, JsonDocReader p) throws NullPointerException {
        return Arrays.asList(dataDir.listFiles()).stream()
                .filter(File::isFile)
                .map(f -> {
                    Pair<Set<String>, Document> entry = p.getDocument(f);
                    fields.addAll(entry.getKey());
                    return entry.getValue();
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void indexdocs(IndexWriter iwriter, List<Document> documents) throws IOException {
        iwriter.addDocuments(documents);
        iwriter.close();
    }

    public static Indexer getInstance() {
        return instance;
    }

    private Indexer(boolean init) {
        if (init) {
            index(docDir);
        }
    }

    public Long index(String dir) {
        try {
            System.out.println("start indexing");
            Long start, end;
            start = System.currentTimeMillis();
            IndexWriter iwriter = InitPersistentWriter();
            List<Document> documents = retrieveJsonDocuments(new File(dir), new JsonDocReader());
            indexdocs(iwriter, documents);
            iwriter.close();
            end = System.currentTimeMillis();
            return end - start;
        } catch (IOException e) {
            System.out.println("Indexer failed:" + e.getMessage());
        }
        return new Long(0);
    }

    public Long reindex(String dir) {
        clean();
        return index(dir);
    }

    public void clean() {
        File root = new File(IndexLocation);
        Arrays.asList(root.listFiles()).forEach(f -> {
            try {
                Files.delete(f.getAbsoluteFile().toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}