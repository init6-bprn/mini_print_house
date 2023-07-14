package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Шаблоны работ")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplateView extends VerticalLayout {
    public TemplateView(){
        super();
    }
}
