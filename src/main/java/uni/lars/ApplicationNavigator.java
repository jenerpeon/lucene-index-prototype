package uni.lars;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;
import uni.lars.view.ESearchView;
import uni.lars.view.GridSearchView;
import uni.lars.view.TreeSearchView;

/**
 * Created by uni on 8/19/16.
 */
@SuppressWarnings("serial")
public class ApplicationNavigator extends Navigator {
    public static String SEARCHVIEW = "search";
    public static String TREESEARCHVIEW = "treesearch";
    public static String ESEARCHVIEW = "esearch";
    public static String LOGSVIEW = "logs";
    public static String CONNVIEW = "conn";
    public static String XMPPVIEW = "xmpp";

    public ApplicationNavigator(final ComponentContainer container) {
        super(UI.getCurrent(), container);


        initViewChangeListener();
        initViewProviders();
        this.addView("", new TreeSearchView());
        this.addView(SEARCHVIEW, new GridSearchView());
        this.addView(ESEARCHVIEW, new ESearchView());
    }

    private void initViewChangeListener() {

    }

    private void initViewProviders() {
        addProvider(new ClassBasedViewProvider(SEARCHVIEW, GridSearchView.class));
        addProvider(new ClassBasedViewProvider(ESEARCHVIEW, ESearchView.class));
        addProvider(new ClassBasedViewProvider(TREESEARCHVIEW, TreeSearchView.class));
    }
}
