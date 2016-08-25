package uni.lars;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import uni.lars.view.MainView;

/**
 *
 */
@Theme("mytheme")
public class App extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final MainView design = new MainView();
        setContent(design);

        // This replaces a proper backend just to simulate how the view behaves
        // with dynamic content
//        for (int i = 0; i < 10; i++) {
//            design.messageList.addComponent(new Message());
//        }
    }


    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = App.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
