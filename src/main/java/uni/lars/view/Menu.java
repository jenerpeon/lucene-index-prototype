package uni.lars.view;

import com.vaadin.ui.UI;
import uni.lars.ApplicationNavigator;
//import uni.lars.ApplicationNavigator;


/**
 * Created by uni on 8/19/16.
 */
public class Menu extends MenuDesign {
    //    protected Button indexButton;
//    protected Button searchButton;
//    protected Button esearchButton;
//    protected Button conButton;
//    protected Button xmppButton;
//    ApplicationNavigator nav;

    public Menu() {
        setWidth("300px");

//        String host = getUI().getPage().getLocation().getHost();
//        hostLabel.setValue(host);

//       indexButton.addClickListener(e -> ne
//            Long elapsed = Indexer.reindex("/tmp/JsonDocuments");
//            elapsedIndex.setValue("Indexing took: " + elapsed + "ms");//+i+" Documents were inserted" );
//            elapsedIndex.setVisible(true);
//        });
        searchButton.addClickListener(e -> UI.getCurrent().getNavigator().navigateTo(ApplicationNavigator.SEARCHVIEW));
        searchButton.addClickListener(e -> UI.getCurrent().getNavigator().navigateTo(ApplicationNavigator.TREESEARCHVIEW));
        esearchButton.addClickListener(e -> UI.getCurrent().getNavigator().navigateTo(ApplicationNavigator.ESEARCHVIEW));

    }


}
