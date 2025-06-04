package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.service.ChainsService;

public class ChainEditor extends VerticalLayout {
    public ChainEditor(Chains chain, ChainsService chainsService){
        super();
        setSizeFull();
    }
}
