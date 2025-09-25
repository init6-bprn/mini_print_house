package ru.bprn.printhouse.views.about;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
//@RouteAlias(value = "", layout = MainLayout.class)

@AnonymousAllowed
public class AboutView extends VerticalLayout {


    public AboutView() {


    }

}
