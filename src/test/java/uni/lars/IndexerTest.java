package uni.lars;

import com.google.common.annotations.VisibleForTesting;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.lucene.document.Document;
import org.junit.Before;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class IndexerTest
        extends TestCase {
    Indexer indexer;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public IndexerTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(IndexerTest.class);
    }


    /**
     * Rigourous Test :-)
     */
//    @Before
//    public void testInitIndexer(){
//        indexer = new Indexer();
//        List<Document> lst = new ArrayList<>();
//        File file = new File("JsonDocuments");
//        JsonDocReader p = new JsonDocReader();
//        assert(p!=null);
//        assert(file.isDirectory());
//        assertTrue(file.listFiles().length > 0);
//
//        System.out.println(file);
//
//
//        indexer.retrieveJsonDocuments(file.getAbsoluteFile(), p);
//
//        assertTrue(indexer!=null);
//
//    }
    public void testRetrieveJsonDocuments() throws Exception {
        indexer = new Indexer();
        List<Document> lst = new ArrayList<>();
        File file = new File("JsonDocuments");
        JsonDocReader p = new JsonDocReader();
        assert (p != null);
        assert (file.isDirectory());
        assertTrue(file.listFiles().length > 0);

        System.out.println(file);


        try {
            lst = indexer.retrieveJsonDocuments(file.getAbsoluteFile(), p);
        } catch (Exception e) {
            System.out.println(e);
            assert (false);
        }
        assertFalse("Retrieved empty document list", lst.isEmpty());

    }

    public void testClean() throws Exception {
        assert (true);

    }
}
