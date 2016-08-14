package uni.lars;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.vaadin.lucenecontainer.LuceneContainer;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

@Theme("mytheme")
@Widgetset("my.vaadin.app.MyAppWidgetset")
public class App extends UI {

    private Grid grid = new Grid();
    private TextField filterText = new TextField();
    private Indexer indexer = new Indexer(true);
    private Searcher searcher = new Searcher();
    private static String TAGS = "tags";
    private static String PAR = "parents";
    private static String GUID = "guids";
    private static String ACL = "acls";
    private static String PKG = "package";
    private LuceneContainer lucene;
    IndexedContainer currentView;


    public IndexedContainer DocumentsToContainer(List<Document> docs) {
        IndexedContainer container = new IndexedContainer();
        List<String> properties = new ArrayList<>();
        for (Document doc : docs) {
//            Item item = new PropertysetItem();
            List<String> lst = new ArrayList<>();
            for (IndexableField field : doc.getFields()) {
//                lst.add(field.stringValue());
//                item.addItemProperty(field.name(), new ObjectProperty<String>(field.stringValue()));
                lst.add(field.stringValue());
            }
            grid.addRow(lst.get(0), lst.get(1), lst.get(2), lst.get(3), lst.get(4));
//            container.addItem(item);
        }
        return container;
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        this.lucene = new LuceneContainer("/tmp/index");
        grid.setContainerDataSource(lucene);

//        Searcher searcher = new Searcher();
        final VerticalLayout layout = new VerticalLayout();

        filterText.setInputPrompt("filter by name...");
        filterText.addTextChangeListener(e -> {
            if (e.getText() == "")
                return;
            System.out.println(searcher.lookup(e.getText()));
            grid.setVisible(true);
            currentView = DocumentsToContainer(searcher.lookup(e.getText()));
            if (currentView.size() <= 0)
                return;

        });

        Button clearFilterTextBtn = new Button(FontAwesome.TIMES);
        clearFilterTextBtn.setDescription("Clear the current filter");
        clearFilterTextBtn.addClickListener(e -> {
            filterText.clear();
//			updateList();
        });

        CssLayout filtering = new CssLayout();
        filtering.addComponents(filterText, clearFilterTextBtn);
        filtering.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        Button addCustomerBtn = new Button("new Index");
        addCustomerBtn.addClickListener(e -> {
            grid.select(null);
//			form.setCustomer(new Customer());
        });

        HorizontalLayout toolbar = new HorizontalLayout(filtering, addCustomerBtn);
        toolbar.setSpacing(true);

        grid.setColumns(TAGS, ACL, PAR, PKG, GUID);

        HorizontalLayout main = new HorizontalLayout(grid);
        main.setSpacing(true);
        main.setSizeFull();
        grid.setSizeFull();
        main.setExpandRatio(grid, 1);

        layout.addComponents(toolbar, main);

//		updateList();

        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

//		form.setVisible(false);

        grid.addSelectionListener(event -> {
            if (event.getSelected().isEmpty()) {
//				form.setVisible(false);
            } else {
//				Customer customer = (Customer) event.getSelected().iterator().next();
//				form.setCustomer(customer);
            }
        });

    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = App.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
//
//    public static void main(String[] args) throws Exception {
//
//
//        Measure ms0 = new Measure("Program execution");
//        ms0.start();
//        //
//        Indexer index = new Indexer(true);
//        ////            StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
//        String res = ms0.end();
////            index.clean();
//        System.out.println(res);
//
//
//        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
//        server.createContext("/test", new
//
//                MyHandler()
//
//        );
//        server.setExecutor(null); // creates a default executor
//        server.start();
//
//    }
//
//    static class MyHandler implements HttpHandler {
//        //        @Override
//
//        public void handle(HttpExchange t) throws IOException {
//
//            String response = "Indexing took: Milliseconds";
//            t.sendResponseHeaders(200, response.length());
//            OutputStream os = t.getResponseBody();
//            os.write(response.getBytes());
//            os.close();
//        }
//    }


//
//        try {
//            final AccountManagement accountManagement = (AccountManagement) providerRegistry.retrieve(AccountManagement.class);
//            if(!accountManagement.verifyAccountExists(EntityImpl.parse("user1@myembeddedjabber.com"))) {
//                accountManagement.addUser(EntityImpl.parse("user1@myembeddedjabber.com"), "password1");
//            }
//        } catch (EntityFormatException e) {
//            System.out.println("break1");
//            e.printStackTrace();
//        } catch (AccountCreationException e) {
//            System.out.println("break2");
//            e.printStackTrace();
//        } catch (Exception e){
//            System.out.println("break3");
//        }
//
//        XMPPServer server = new XMPPServer("myembeddedjabber.com");
//        server.addEndpoint(new TCPEndpoint());
//        server.setStorageProviderRegistry(providerRegistry);
//
//        server.setTLSCertificateInfo(new File("src/main/config/bogus_mina_tls.cert"), "boguspw");
//
//        try {
//            server.start();
//            System.out.println("server is running...");
//        } catch (Exception e) {
//            System.out.println("break3");
//            e.printStackTrace();
//        }

//        TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
//                .hostname("localhost")
//                .port(5222)
//                .build();
//
//        BoshConnectionConfiguration boshConfiguration = BoshConnectionConfiguration.builder()
//                .hostname("domain")
//                .port(5280)
//                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("hostname", 312)))
//                .path("http-bind/")
//                .build();
//
//        XmppClient xmppClient = XmppClient.create("domain", tcpConfiguration, boshConfiguration);

//
//
//        Measure ms2 = new Measure("lookup");
//        ms2.start();
//        ms2.end();
//
//        Measure ms3 = new Measure("cleanup");
//        ms3.start();
//        index.clean();
//        ms3.end();
//
//        ms0.end();
//}

