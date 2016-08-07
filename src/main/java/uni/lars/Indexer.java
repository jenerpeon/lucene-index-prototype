package uni.lars;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/**
 * Created by lars on 8/5/16.
 */
public class Indexer {

    public static String IndexLocation = "index";
    public static String docDir = "documents";
    public static String corrupt = "corrupt";

    Directory dir;
    boolean debug;


    // deserialize yaml file and return Map of Attributes
    private Map<String, List<String>> readYamlFile(final File file) throws IOException {
        Map<String, List<String>> config = new HashMap<String, List<String>>();

        YAMLMapper o = new YAMLMapper();

        try {
            JsonNode node = o.readTree(file);

            for (Iterator<Map.Entry<String, JsonNode>> n = node.fields(); n.hasNext(); ) {
                Map.Entry<String, JsonNode> current = n.next();
                if(current.getValue().isTextual()){
                    config.put(current.getKey(), Arrays.asList(current.getValue().textValue()));
                    continue;
                }
                List<String> lst = new ArrayList<String>();
                for (Iterator<String> subn = current.getValue().fieldNames(); subn.hasNext(); ) {
                    lst.add(subn.next());
                }
                config.put(current.getKey(), lst);
            }

            return config;

        } catch (IOException e) {
            // Copy file to corrupt
            Path full_corrupt = new File(corrupt+"/"+file.getName()).toPath().toAbsolutePath();
            System.out.println("unable to store Device Data:"+
                    file.getAbsolutePath()+
                    "\n saving to: "+
                    full_corrupt.toString());
            Files.copy(file.toPath().toAbsolutePath(), full_corrupt, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(file.toPath().toAbsolutePath());
        }
        return null;
    }

    // read file and return content as String
    static String readFile(String path, Charset encoding) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    // capsulate this, to call new writer for every document to add. Always based on latest Index
    private IndexWriter InitWriter() throws IOException {
        Path Index = new java.io.File(this.IndexLocation).toPath();
            dir = FSDirectory.open(Index);
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            return new IndexWriter(dir, iwc);

    }

    // index directory recursively
    private void indexDirectory(IndexWriter indexWriter, File dataDir, String suffix) throws IOException{
        File[] docs = dataDir.listFiles();

        for(File file : docs){
            if(file.isDirectory()){
                indexDirectory(indexWriter, file, suffix);
            }else{
                indexFileWithIndexWriter(indexWriter, file , suffix);
            }
        }
    }
    // does yaml file match pojo?
    private boolean validate(File file){
       return true;
    }

    // called by indexDirectory: adds file to Index
    private void indexFileWithIndexWriter(IndexWriter indexWriter, File file, String suffix) throws IOException{
        if(file.isHidden() || file.isDirectory() || !file.canRead() || !file.exists()){
            if(debug) System.out.println("no such file:"+file.getCanonicalPath().toString());
            return;
        }
        if(suffix != null && !file.getName().endsWith(suffix)){
            if(debug) System.out.println("no suitable file with suffix:"+suffix+":"+file.getCanonicalPath().toString());
            return;
        }

        if(suffix != null && file.getName().endsWith("tag.en."+suffix) ||
                file.getName().endsWith("tag.de."+suffix) ||
                file.getName().endsWith("errors.en."+suffix) ||
                file.getName().endsWith("errors.de."+suffix) ||
                file.getName().endsWith("meta."+suffix) ||
                file.getName().endsWith("meta.de."+suffix) ||
                file.getName().endsWith("meta.en."+suffix) )
        {
            return;
        }
        if(debug) System.out.println("Indexing file"+ file.getCanonicalPath().toString());
        indexWriter.addDocument(documentParser(file));
    }

    private Document documentParser(File file) throws IOException {
        Document doc = new Document();
        Map<String, List<String>> config = readYamlFile(file);

//
        for(String attr : config.keySet()){
            List<String> lst = config.get(attr);
//            System.out.println(attr+lst.toString().replace("[","").replace("]",""));

            doc.add(new Field(attr, lst.toString().replace("[","").replace("]",""), TextField.TYPE_STORED));
        }
//        Field acl = new Field("acl", stracl, TextField.TYPE_STORED);
//        doc.add(new Field(file.getAbsolutePath(), readFile(file.getAbsolutePath(), Charset.defaultCharset()), TextField.TYPE_STORED));

        return doc;
    }

    public Indexer(boolean debug) {
        this.debug = debug;
        IndexWriter iwriter = null;
        try {
            iwriter = InitWriter();
            indexDirectory(iwriter, new File(docDir), "yml" );
//            iwriter.commit();
        }catch (IOException e){
            System.out.println("Indexer failed:"+e.getMessage());
        }finally {
            if (iwriter != null) {
                try {
                   iwriter.close();
                } catch (IOException e) {
                   if(iwriter.isOpen())
                       System.out.println("still open writer");
                    e.printStackTrace();
                }
            }
        }
    }
}