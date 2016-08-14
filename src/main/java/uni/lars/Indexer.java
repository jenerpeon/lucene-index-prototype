package uni.lars;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Created by lars on 8/5/16.
 */
public class Indexer {

    public static String IndexLocation = "/tmp/index";
    public static String docDir = "/tmp/JsonDocuments";

    public static Directory dir;
    boolean debug;

    // capsulate indexWriter, to call new writer for every document to add. Always based on latest Index
    private IndexWriter InitPersistentWriter() throws IOException {
        Path Index = new java.io.File(this.IndexLocation).toPath();
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
    public List<Document> retrieveJsonDocuments(File dataDir, JsonDocReader p){
        File[] docs = dataDir.getAbsoluteFile().listFiles();
        ArrayList<Document> lst = new ArrayList<>();
//        assert(dataDir.isDirectory());
//        System.out.println(dataDir.toString());
        for(File file: dataDir.listFiles()){
            if(file.isDirectory()){
                continue;
            } else{
                lst.add(p.getDocument(file));
            }
        }
        return lst;
    }

    // prepare indexable documents
    private List<Document> retrieveDocuments(ArrayList<Document> documents, YamlParser yparser, File dataDir, String suffix) throws IOException {
        File[] docs = dataDir.getAbsoluteFile().listFiles();
        for (File file : docs) {
            if (file.isDirectory()) {
                retrieveDocuments(documents, yparser, file.getAbsoluteFile(), suffix);
            } else {

                if (file.isHidden() || file.isDirectory() || !file.canRead() || !file.exists()) {
                    if (debug) System.out.println("no such file:" + file.getCanonicalPath().toString());
                    continue;
                }
                if (suffix != null && !file.getName().endsWith(suffix)) {
                    if (debug)
                        System.out.println("no suitable file with suffix:" + suffix + ":" + file.getCanonicalPath().toString());
                    continue;
                }

                if (suffix != null && file.getName().endsWith("tag.en." + suffix) ||
                        file.getName().endsWith("tag.de." + suffix) ||
                        file.getName().endsWith("errors.en." + suffix) ||
                        file.getName().endsWith("errors.de." + suffix) ||
                        file.getName().endsWith("meta." + suffix) ||
                        file.getName().endsWith("meta.de." + suffix) ||
                        file.getName().endsWith("meta.en." + suffix))
                    continue;

                if (debug) System.out.println("Indexing file" + file.getCanonicalPath().toString());

                Map<String, List<String>> config = yparser.readYamlFile(file);
                Document doc = new Document();
                for (String attr : config.keySet()) {
                    List<String> lst = config.get(attr);
                    doc.add(new Field(attr, lst.toString()
                            .replace("[", "")
                            .replace("]", "")
                            .replace(".", " ")
                            , TextField.TYPE_STORED));
                }
                documents.add(doc);
            }
        }
        return documents;
    }

    private void indexdocs(IndexWriter iwriter, List<Document> documents) throws IOException {
        iwriter.addDocuments(documents);
    }

    public Indexer(){

    }
    public Indexer(boolean init) {
        Measure m1 = new Measure("inline file reading and document creation");
        Measure m2 = new Measure("inline indexing");
        Measure m3 = new Measure("inline all");
        m3.start();
        if (init) {
            try {
                m1.start();
                IndexWriter iwriter = InitPersistentWriter();
//                List<Document> documents = retrieveDocuments(new ArrayList<Document>(), new YamlParser(), new File(docDir), "yml");

                List<Document> documents = retrieveJsonDocuments(new File(docDir), new JsonDocReader());
                m1.end();
                m2.start();
                indexdocs(iwriter, documents);
                m2.end();
                iwriter.close();

            } catch (IOException e) {
                System.out.println("Indexer failed:" + e.getMessage());
            }
        }
        m3.end();
    }

    public void clean() {
        File root = new File(IndexLocation).getAbsoluteFile();
        try {
            clean_wrapped(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clean_wrapped(File f) throws IOException {

        for (File file : f.listFiles()) {
            if (file.isDirectory()) {
                clean_wrapped(file);
            }
            Files.delete(file.toPath().toAbsolutePath());
        }

    }
}