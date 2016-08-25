package uni.lars.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elemental.json.JsonObject;
import javafx.util.Pair;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by uni on 8/14/16.
 */
public class JsonDocReader {
    private ObjectMapper mapper;

    JsonDocReader() {
        mapper = new ObjectMapper();
    }

    public Pair<Set<String>, Document> getDocument(File file) {
        Document doc = new Document();
        Set<String> fields = new HashSet<>();
            try {
                mapper.readTree(file).fields().forEachRemaining(current -> {
                            if(!fields.contains(current.getKey())){
                                fields.add(current.getKey());
                            }
                            if (current.getValue().isTextual()) {
                                doc.add(new Field(current.getKey(), current.getValue().textValue(), TextField.TYPE_STORED));
                            } else {
                                doc.add(new Field(current.getKey(), current.getValue().toString()
                                        .replace("[", "")
                                        .replace("]", "")
                                        .replace(".", " "), TextField.TYPE_STORED));
                            }
                        }
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        return new Pair<>(fields, doc);

    }

}
