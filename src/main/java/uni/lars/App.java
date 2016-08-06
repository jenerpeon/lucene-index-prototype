package uni.lars;


import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.TypeDescription;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App
{
    public static void main( String[] args )
    {
//        Indexer index = new Indexer(false);
//        Searcher searcher = new Searcher();

//        searcher.lookup("temp");
        Path sampleYaml = new java.io.File("documents/models/Generic/HeatPump/HeatPump.yml").toPath().toAbsolutePath();
//        System.out.println(sampleYaml.toAbsolutePath().toString());


        Constructor descriptionConstructor = new Constructor(Description.class);
        YAMLMapper o = new YAMLMapper();
        o.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        try {
            Description desc = o.readValue(sampleYaml.toFile(), Description.class);
            System.out.println(desc);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static String readFile(Path path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, encoding);
    }

    // Test yaml deserialization to pojo
    static class Description{
        private String Package;
        private String Parent;
        private Map<String, Map<String, String>> Tags;

        public String getPackage() {
            return Package;
        }

        public void setPackage(String aPackage) {
            Package = aPackage;
        }

        public Map<String, Map<String, String>> getTags() {
            return Tags;
        }

        public void setTags(Map<String, Map<String, String>> tags) {
            Tags = tags;
        }

        public String getParent() {
            return Parent;
        }

        public void setParent(String parent) {
            Parent = parent;
        }

        @Override
        public String toString(){
            return this.Package+'\n'+this.Parent+'\n'+this.Tags+'\n';
        }

    }


}
