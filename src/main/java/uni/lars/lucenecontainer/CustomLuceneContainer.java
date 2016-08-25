package uni.lars.lucenecontainer;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
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
import org.apache.lucene.util.Version;
import uni.lars.Utils.Indexer;
import uni.lars.Utils.Searcher;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by uni on 8/17/16.
 */
public class CustomLuceneContainer extends IndexedContainer implements Container.Indexed {

    public static final Version LUCENE_VERSION = Version.LUCENE_6_1_0;
    public static final String DEFAULT_UNIQUE_DOC_ID = "UNIQUE_DOC_IDFR";
    private static String IndexLocation = "/tmp/index";
    private String uniqueDocIdentifier = CustomLuceneContainer.DEFAULT_UNIQUE_DOC_ID;
    private static final long serialVersionUID = -7098827954488865887L;


    private Indexer indexer = Indexer.getInstance();
    private IndexReader reader;
    private IndexSearcher searcher;
    private Searcher customSearcher = new Searcher();
    private Directory index;
    private String indexPath;
    private ScoreDoc[] cache;
    private int size;
    private int indexOfFirstCachedDoc;

    private Map<Object, Field> fields;
    private String queryString, sortField;
    private Collection<Object> propertyIds;
    private Collection<ObjectProperty> properties;
    private boolean reverse;

    private int pageLength = 100;

    /**
     * Opens the index from directory and loads field names
     */
    public CustomLuceneContainer(String directory) {
        super();
        fields = new HashMap<>();
        try {
            BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
            // open the index and load field names
            indexPath = directory;
            index = FSDirectory.open(new File(IndexLocation).toPath());
            reader = DirectoryReader.open(index);

//             field names of first document -> see Multifieldqueryparser
            Document doc = reader.document(0);
            propertyIds = doc.getFields().stream().map(IndexableField::name).collect(Collectors.toList());

            for (Object n : propertyIds) {
                this.addContainerProperty(n, String.class, null);
                fields.put(n, new Field((String) n, "", TextField.TYPE_STORED));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Long search(String queryString, String sortField, boolean reverse) {
        search(queryString);
        return search(queryString, sortField, reverse, 0);
    }

    public Long search(String queryString){
        customSearcher.lookup(queryString);
        return new Long(0);
    }

    private Long search(String queryString, String sortField, boolean reverse,
                              int startIndex) {

        Long start, end;
        try {
            this.removeAllItems();
            start = System.currentTimeMillis();
            this.queryString = queryString;
            this.sortField = sortField;
            this.reverse = reverse;
            int cacheSize = pageLength;
            if (reader != null) {
                close();
            }
            cache = new ScoreDoc[cacheSize];
            QueryParser parser = new MultiFieldQueryParser(fields.keySet().toArray(new String[fields.size()]),
                    new StandardAnalyzer());

            parser.setAllowLeadingWildcard(true);
            Query query = parser.parse(queryString);
            query.toString();
            Sort sort = null;
            if (sortField != null && !sortField.isEmpty()) {
                sort = new Sort(new SortField(sortField, SortField.Type.STRING,
                        reverse));
            } else {
                sort = null;
            }
            reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);

            int ind = 0;
            int searchSize = startIndex + (cacheSize / 2);
            int firstIndex = startIndex - (cacheSize / 2);
            TopDocs hits = null;
            if (sort == null) {
                hits = searcher.search(query, searchSize);
            } else {
                hits = searcher.search(query, searchSize);
            }
            size = hits.totalHits;
            indexOfFirstCachedDoc = Math.max(0, firstIndex);
            for (int i = Math.max(0, firstIndex); i < Math
                    .min(searchSize, size); i++) {
                cache[ind] = hits.scoreDocs[i];
                ++ind;
            }
            if (ind < cache.length - 1) {
                // truncate nulls away
                cache = Arrays.copyOf(cache, ind);
            }
            addResultsAsItems(cache, searcher);
            end = System.currentTimeMillis();
//            searcher.close();
            return end-start;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Long(0);
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
