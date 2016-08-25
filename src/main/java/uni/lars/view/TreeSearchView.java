package uni.lars.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import uni.lars.Utils.Indexer;
import uni.lars.Utils.Timeit;
import uni.lars.lucenecontainer.CustomLuceneContainer;
import uni.lars.lucenecontainer.CustomLuceneHierarchicalContainer;

/**
 * Created by lars on 8/22/16.
 */
public class TreeSearchView extends Panel implements View {

    private final TextField filterText = new TextField();
    private final TreeTable tree = new TreeTable();
    private final VerticalLayout root = new VerticalLayout();
    private final CustomLuceneHierarchicalContainer lucene = new CustomLuceneHierarchicalContainer("/tmp/index");
    private final Button searchBtn = new Button("Search");
    private final Button indexBtn = new Button("Index");
    private final Label elapsedSearch = new Label();
    private final Label elapsedIndex = new Label();
    private final HorizontalLayout toolbar = new HorizontalLayout();
    private final HorizontalLayout main = new HorizontalLayout();

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        lucene.search("com*", null, false);
    }

    public TreeSearchView() {
        buildLayout();
        setContent();
    }

    private void buildLayout() {
        CssLayout filtering = new CssLayout();
        filtering.addComponents(filterText, searchBtn);
        filtering.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        elapsedSearch.setVisible(false);
        elapsedIndex.setVisible(false);

        tree.addStyleName(ValoTheme.PANEL_BORDERLESS);

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

        main.addComponent(tree);
        main.setSpacing(true);
        main.setSizeFull();
        tree.setSizeFull();
        main.setExpandRatio(tree, 1);

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
        tree.setContainerDataSource(lucene);

        tree.setVisibleColumns("package", "parents");

        tree.addItemClickListener(e-> System.out.println(e.getItemId()));

        tree.addExpandListener(e -> {
            lucene.expandNode(e.getItemId());
        });

        filterText.setTextChangeTimeout(1);
        filterText.addTextChangeListener(e -> {
             if (filterText.getValue() == null) {
                return;
            }
            double time = Timeit.code(() -> lucene.search("*"+e.getText() + "*", "package", false));

            elapsedSearch.setValue("Time ellapsed: " + time + "seconds \n" + lucene.size() + " items listed");
            elapsedSearch.setVisible(true);
        });

        tree.addCollapseListener(e ->
            lucene.collapseNode(e.getItemId())
        );

        searchBtn.addClickListener(e -> {
            if (filterText.getValue() == null) {
                return;
            }
            double time = Timeit.code(() -> lucene.search(filterText.getValue() + "*", "package", false));

            elapsedSearch.setValue("Time ellapsed: " + time + "seconds \n" + lucene.size() + " items listed");
            elapsedSearch.setVisible(true);
        });

        indexBtn.addClickListener(e -> {
            Timeit.code(() -> Indexer.getInstance().reindex("/tmp/JsonDocuments"));
        });


    }


}
