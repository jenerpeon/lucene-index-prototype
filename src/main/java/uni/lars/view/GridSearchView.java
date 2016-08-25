package uni.lars.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import uni.lars.Utils.Indexer;
import uni.lars.Utils.Timeit;
import uni.lars.lucenecontainer.CustomLuceneContainer;


/**
 * Created by uni on 8/19/16.
 */
public class GridSearchView extends Panel implements View {

    private final TextField filterText = new TextField();
    private final VerticalLayout root = new VerticalLayout();
    private final CustomLuceneContainer lucene = new CustomLuceneContainer("/tmp/index");
    private final Button searchBtn = new Button("Search");
    private final Button indexBtn = new Button("Index");
    private final Grid grid = new Grid();
    private final Label elapsedSearch = new Label();
    private final Label elapsedIndex = new Label();
    private final HorizontalLayout toolbar = new HorizontalLayout();
    private final HorizontalLayout main = new HorizontalLayout();

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        lucene.search("com*", null, false);
    }

    public GridSearchView() {
        buildLayout();
        setContent();
    }

    private void buildLayout() {
        CssLayout filtering = new CssLayout();
        filtering.addComponents(filterText, searchBtn);
        filtering.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        elapsedSearch.setVisible(false);
        elapsedIndex.setVisible(false);

        grid.addStyleName(ValoTheme.PANEL_BORDERLESS);

        filtering.setWidthUndefined();
        indexBtn.setWidthUndefined();

        toolbar.addComponents(filtering, elapsedSearch, elapsedIndex, indexBtn);
        toolbar.setWidth("100%");
        toolbar.setComponentAlignment(elapsedSearch, Alignment.TOP_CENTER);
        toolbar.setComponentAlignment(filtering, Alignment.TOP_LEFT);
        toolbar.setComponentAlignment(elapsedIndex, Alignment.TOP_RIGHT);
        toolbar.setComponentAlignment(indexBtn, Alignment.TOP_RIGHT);
        toolbar.setSizeUndefined();
        toolbar.setSpacing(true);

        main.addComponent(grid);
        main.setSpacing(true);
        main.setSizeFull();
        grid.setSizeFull();
        main.setExpandRatio(grid, 1);

//
//        HorizontalLayout logging = new HorizontalLayout();
//        logging.setSpacing(true);
//        logging.setSizeFull();
//        logging.setExpandRatio(null,1);



        root.addComponents(toolbar, main);
        root.setExpandRatio(main, 1.0f);

        root.setMargin(true);
        root.setSpacing(true);
        root.setSizeFull();
        setContent(root);
        setSizeFull();

    }

    private void setContent() {

        filterText.setInputPrompt("filter by name...");
        grid.setContainerDataSource(lucene);

        grid.removeAllColumns();
        grid.addColumn("package");
        grid.addColumn("parents");

        searchBtn.addClickListener(e -> {
            if (filterText.getValue() == null) {
                return;
            }
            double time = Timeit.code(() -> lucene.search(filterText.getValue() + "*", "package", false));

            elapsedSearch.setValue("Time ellapsed: " + time + "seconds \n" + lucene.size() + " items listed");
            elapsedSearch.setVisible(true);

            //trigger container refresh (known vaadin bug)
            grid.setEditorEnabled(true);
            grid.setEditorEnabled(false);
        });

    indexBtn.addClickListener(e -> {
        Timeit.code(() -> Indexer.getInstance().reindex("/tmp/JsonDocuments"));
    });


    }


}
