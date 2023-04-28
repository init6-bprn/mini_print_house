package ru.bprn.printhouse.views.dictionary;


import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import org.vaadin.crudui.crud.impl.GridCrud;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.QuantityColorsService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Словарь количества цыетов")
@Route(value = "quantity_colors_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class QuantityColorsDictionary extends VerticalLayout{

        public QuantityColorsDictionary(QuantityColorsService qcService) {

            setSpacing(false);

            H2 header = new H2("This place intentionally left empty");
            header.addClassNames(Margin.Top.XLARGE, Margin.Bottom.MEDIUM);
            add(header);
            add(new Paragraph("It’s a place where you can grow your own UI 🤗"));

            GridCrud<QuantityColors> crud = new GridCrud<>(QuantityColors.class);
            crud.getGrid().setColumnReorderingAllowed(true);

            this.add(crud);

            crud.setOperations(
                    () -> qcService.findAll(),
                    user -> qcService.save(user),
                    user -> qcService.save(user),
                    user -> qcService.delete(user)
            );

            setSizeFull();
            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.START);
            getStyle().set("text-align", "center");
        }


}
