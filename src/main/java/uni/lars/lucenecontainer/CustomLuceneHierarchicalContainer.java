package uni.lars.lucenecontainer;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.ObjectProperty;
import javafx.collections.transformation.SortedList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import uni.lars.Utils.Indexer;
import uni.lars.Utils.Searcher;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lars on 8/22/16.
 */
public class CustomLuceneHierarchicalContainer extends HierarchicalContainer {

    //    public static final Version LUCENE_VERSION = Version.LUCENE_6_1_0;
    public static final String DEFAULT_UNIQUE_DOC_ID = "UNIQUE_DOC_IDFR";
    private static String IndexLocation = "/tmp/index";
    private String uniqueDocIdentifier = CustomLuceneContainer.DEFAULT_UNIQUE_DOC_ID;
    private static final long serialVersionUID = -7098827954488865887L;


//    private Indexer indexer = Indexer.getInstance();
    private Set<String> hierarchy = new TreeSet<>();
    private IndexReader reader;
    private IndexSearcher searcher;
    private Searcher customSearcher = new Searcher();
    private Directory index;
    private String indexPath;
    private ScoreDoc[] cache;
    private Analyzer analyzer;
    private int size;
    private int indexOfFirstCachedDoc;

    private Map<Object, Field> fields;
    private String queryString, sortField;
    private Collection<Object> propertyIds;
    private Collection<ObjectProperty> properties;
    private QueryParser parser;
    private boolean reverse;

    private int pageLength = 100;

    /**
     * Opens the index from directory and loads field names
     */

    public CustomLuceneHierarchicalContainer(String directory) {
        super();
        fields = new HashMap<>();
        analyzer = new StandardAnalyzer();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        indexPath = directory;


        // open the index and load field names
        try {
            index = FSDirectory.open(new File(IndexLocation).toPath());
            reader = DirectoryReader.open(index);
            Document doc = reader.document(0);
            propertyIds = doc.getFields().stream()
                    .map(IndexableField::name)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }


        propertyIds.forEach(pid -> {
            addContainerProperty(pid, String.class, null);
            fields.put(pid, new Field((String) pid, "", TextField.TYPE_STORED));
        });

        // Query Parser Configuration
//        parser = new MultiFieldQueryParser(fields.keySet().toArray(new String[fields.size()]), analyzer);

        parser = new MultiFieldQueryParser(new String[]{"package"}, analyzer);
        parser.setAllowLeadingWildcard(true);

    }

    public void search(String queryString, String sortField, boolean reverse) {
//        search(queryString);
        hierarchy.clear();
//        internalRemoveAllItems();
        removeAllItems();
        search(queryString, sortField, reverse, 0);
    }

    public Long search(String queryString) {
        customSearcher.lookup(queryString);
        return new Long(0);
    }

    private void search(String queryString, String sortField, boolean reverse,
                        int startIndex) {

        if (reader != null)
            close();
        removeAllItems();
        TopDocs hits;
        final AtomicInteger count = new AtomicInteger();

        int endIndex;
        int cacheSize = pageLength;
        this.queryString = queryString;
        this.sortField = sortField;
        this.reverse = reverse;
        cache = new ScoreDoc[cacheSize];

        try {
            Query query = parser.parse(queryString);
            reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);

            hits = searcher.search(query, cacheSize);
            size = hits.totalHits;

            if (startIndex + cacheSize > size)
                endIndex = size;
            else
                endIndex = startIndex + cacheSize;

            IntStream.range(startIndex, endIndex)
                    .forEach(i -> cache[count.getAndIncrement()] = hits.scoreDocs[i]);

            if (cache.length <= 0)
                return;

            hierarchyBrowse(cache, searcher, 3);
//            categorize(cache, searcher, "com.solarwatt", 2);
//            addResultsAsItems(cache, searcher);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void expandNode(Object ItemId) {
        Item item = this.getItem(ItemId);
        String value = (String) item.getItemProperty("package").getValue();
        hierarchy.stream().filter(h -> h.startsWith(value))
                .forEach(h ->
                        {
                            int hsize = h.split("\\.").length;
                            int lvl = value.split("\\.").length;
                            if (hsize <= lvl)
                                return;
                            Object id = addItem();
                            Item buf = this.getItem(id);
                            String pref = String.join(".", Arrays.asList(h.split("\\.")).subList(0, value.split("\\.").length + 1));
                            Property propbuf = buf.getItemProperty("package");
                            setParent(id, ItemId);
                            propbuf.setValue(pref);
                        }
                );
    }

    public void collapseNode(Object ItemId) {
        System.out.println(getChildren(ItemId));
//        getChildren(ItemId).forEach(i -> {
//            try {
//                removeItem(i);
//            } catch (NullPointerException e) {
//                System.out.println("Cannot remove" + (String) i);
//            }
//        });
    }


    private void hierarchyBrowse(ScoreDoc[] results, IndexSearcher searcher, int lvl) {

        if (hierarchy.isEmpty()) {
            IntStream.range(0, results.length).forEach(i -> {
                try {
                    Document doc = searcher.doc(results[i].doc);
                    String key = doc.getField("package").stringValue();
                    hierarchy.add(key);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        hierarchy.stream()
                .map(h -> String.join(".", Arrays.asList(h.split("\\.")).subList(0, lvl)))
                .collect(Collectors.toSet())
                .stream()
                .forEach(d -> {
                            Object id = addItem();
                            Item buf = this.getItem(id);
                            Property propbuf = buf.getItemProperty("package");
                            propbuf.setValue(d);
                        }
                );


    }

    private void categorize(ScoreDoc[] results, IndexSearcher searcher, String dom, int lvl) {
//        Map<String, List<Document>> hierarchy = new TreeMap<>();

        Set<String> hierarchy = new TreeSet<>();

        List resultId = IntStream.range(0, results.length)
                .filter(i -> {
                    try {
                        Document doc = searcher.doc(results[i].doc);
                        String key = doc.getField("package").stringValue();
                        if (key.startsWith(dom)) {
                            return true;
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    return false;
                })
                .boxed()
                .collect(Collectors.toList());

        IntStream.range(0, results.length).forEach(i -> {
            try {
                Document doc = searcher.doc(results[i].doc);
                String key = doc.getField("package").stringValue();
                hierarchy.add(key);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        System.out.println(resultId);

    }

    public void addItemsByCategory(Map<String, List<Document>> items) {
        items.forEach((s, lst) -> {
        });
    }

    private void addResultsAsItems(ScoreDoc[] results, IndexSearcher searcher) throws IOException {
        for (int i = 0; i < results.length; i++) {
            Document hitDoc = searcher.doc(results[i].doc);
            Item buf = this.getItem(addItem());
            for (IndexableField field : hitDoc.getFields()) {
                Property propbuf = buf.getItemProperty(field.name());
                propbuf.setValue(field.stringValue());
            }
        }
    }


    @Override
    public Collection<Object> getContainerPropertyIds() {
        return propertyIds;
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        return getSortablePropertyIds();
    }

    /**
     * Empty the container
     */
    public void clear() {
        cache = null;
        size = 0;
    }

    public void close() {
        try {
            reader.close();
            reader = null;
            searcher = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        clear();
    }

}
