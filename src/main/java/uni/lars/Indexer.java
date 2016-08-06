package uni.lars;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.constructor.Constructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 8/5/16.
 */
public class Indexer {

    public static String IndexLocation = "index";
    public static String docDir = "documents";
    Directory dir;
    boolean debug;

    private class YamlFile{

        private String pkg;

        private String cls;

        private List<String> tags;

        public String getPkg(){
            return pkg;
        }
        public String getCls(){
            return cls;
        }
        public List<String> getTags(){
            return tags;
        }
        public void setPkg(String pkg){
            this.pkg = pkg;
        }
        public void setCls(String cls){
            this.cls = cls;
        }
        public void setTags(List<String> tags){
            this.tags = tags;
        }
    }

    // read YamlFile and return Yaml file object
    private YamlFile readYamlFile(final File file) throws IOException {
        Yaml yaml = new Yaml();
        Constructor classConstructor = new Constructor(YamlFile.class);
        try {
            Map<String, Object> map = (Map<String, Object>) yaml.load(readFile(file.getPath(), Charset.defaultCharset()));
//            System.out.println(file.getName().toString());
//            System.out.println(map.keySet());
        }catch(Exception e) {
            if(debug) System.out.println("Unable to parse Yaml File:"+file.getCanonicalPath().toString());
//        final YAMLMapper mapper = new YAMLMapper();
//        return mapper.readValue(file, YamlFile.class);
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
        String text = readFile(file.getCanonicalPath(), Charset.defaultCharset());
        readYamlFile(file);

//        System.out.println(bean.toString());
//
//        Field id = new Field("id", strid, TextField.TYPE_STORED);
//        Field parent = new Field("parent", strparent, TextField.TYPE_STORED);
//        Field cls = new Field("class", strclass, TextField.TYPE_STORED);
//        Field tags = new Field("tags", strtags, TextField.TYPE_STORED);
//        Field acl = new Field("acl", stracl, TextField.TYPE_STORED);
//
//        List<Field> fields = Arrays.asList(id, parent, cls, acl);
//
//        for(Field field : fields){
//            doc.add(field);
//        }

        return doc;

    }

    public Indexer(boolean debug) {
        this.debug = debug;
        try {
            IndexWriter iwriter = InitWriter();
            indexDirectory(iwriter, new File(docDir), "yml" );
            iwriter.close();
        }catch (IOException e){
            System.out.println("Indexer failed:"+e.getMessage());
        }
    }
}