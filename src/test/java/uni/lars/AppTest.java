package uni.lars;

import com.vaadin.data.util.IndexedContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.lucene.document.Document;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by lars on 8/14/16.
 */
public class AppTest extends TestCase{


    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testDocumentsToContainer(){
        Indexer indexer = new Indexer();
        List<Document> docs = indexer.retrieveJsonDocuments(new File("JsonDocuments").getAbsoluteFile(), new JsonDocReader());

        App app = new App();
        assertTrue("app.class initialization breaks", app!=null);

//        IndexedContainer container = app.DocumentsToContainer(docs);
//        assertTrue("container empty", container.size()>0);
    }

}