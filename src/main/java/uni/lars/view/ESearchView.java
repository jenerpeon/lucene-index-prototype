package uni.lars.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Created by uni on 8/19/16.
 */
public class ESearchView extends Panel implements View{

    Label heading = new Label("Hallo Erweiterte Suche");
    private final VerticalLayout root;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }

    public ESearchView(){

        addStyleName(ValoTheme.PANEL_BORDERLESS);
        setSizeUndefined();
        root = new VerticalLayout();
        root.setMargin(true);

        heading.setWidth("300px");
        root.addComponent(heading);

        root.setSizeUndefined();

        setContent(root);
    }
}
