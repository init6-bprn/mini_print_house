package ru.bprn.printhouse.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;
import ru.bprn.printhouse.data.entity.User;
import ru.bprn.printhouse.security.AuthenticatedUser;
import ru.bprn.printhouse.views.material.PrintSheetMaterialView;
import ru.bprn.printhouse.views.operation.OperationView;
import ru.bprn.printhouse.views.operation.TypeOfOperationView;
import ru.bprn.printhouse.views.dictionary.*;
import ru.bprn.printhouse.views.machine.cutters.PaperCuttersView;
import ru.bprn.printhouse.views.machine.printers.DigitalPrintingMachineView;
import ru.bprn.printhouse.views.machine.printers.PrintSpeedMaterialDensityView;
import ru.bprn.printhouse.views.machine.printers.PrintersView;
import ru.bprn.printhouse.views.material.MaterialView;
import ru.bprn.printhouse.views.material.PrintingMaterialView;
import ru.bprn.printhouse.views.products.ProductsView;
import ru.bprn.printhouse.views.templates.TemplatesView;
import ru.bprn.printhouse.views.templates.WorkFlowView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
@PageTitle("Главный экран приложения")
@Route("")
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private H2 viewTitle;
    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Микротипография");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {

       SideNav nav = new SideNav();

        var navGroup = new SideNavItem("Словари");
        if (accessChecker.hasAccess(QuantityColorsDictionary.class))
            navGroup.addItem(new SideNavItem("Количество цветов", QuantityColorsDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(TypeOfPrinterDictionary.class))
            navGroup.addItem(new SideNavItem("Тип принтера", TypeOfPrinterDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(TypeOfMaterialDictionary.class))
            navGroup.addItem(new SideNavItem("Тип материала", TypeOfMaterialDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(ThicknessDictionary.class))
            navGroup.addItem(new SideNavItem("Плотность материала", ThicknessDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(SizeOfPrintLeafDictionary.class))
            navGroup.addItem(new SideNavItem("Размер печатного листа", SizeOfPrintLeafDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(StandartSizeDictionary.class))
            navGroup.addItem(new SideNavItem("Размер изделия", StandartSizeDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(GapDictionary.class))
            navGroup.addItem(new SideNavItem("Отступы", GapDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(ImposeCaseDictionary.class))
            navGroup.addItem(new SideNavItem("Спуск полос", ImposeCaseDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(VariablesForMainWorksDictionary.class))
            navGroup.addItem(new SideNavItem("Переменные для работ", VariablesForMainWorksDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(PriceOfMaterialDictionary.class))
            navGroup.addItem(new SideNavItem("Стоимость материалов", PriceOfMaterialDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(TimeOfDigitalPrintOperationDictionary.class))
            navGroup.addItem(new SideNavItem("Скорость печати на разных материалах", TimeOfDigitalPrintOperationDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(FormulasDictionary.class))
            navGroup.addItem(new SideNavItem("Словарь формул устройств", FormulasDictionary.class, VaadinIcon.RECORDS.create()));

        nav.addItem(navGroup);

        navGroup = new SideNavItem("Оборудование");
        if (accessChecker.hasAccess(PrintersView.class))
            navGroup.addItem(new SideNavItem("ЦПМ", PrintersView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(DigitalPrintingMachineView.class))
            navGroup.addItem(new SideNavItem("Листовые принтеры", DigitalPrintingMachineView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(PaperCuttersView.class))
            navGroup.addItem(new SideNavItem("Резаки", PaperCuttersView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(PrintSpeedMaterialDensityView.class))
            navGroup.addItem(new SideNavItem("Скорость-плотность ЦПМ", PrintSpeedMaterialDensityView.class, LineAwesomeIcon.GLOBE_SOLID.create()));
        nav.addItem(navGroup);

        navGroup = new SideNavItem("Материалы");
        if (accessChecker.hasAccess(MaterialView.class))
            navGroup.addItem(new SideNavItem("Бумага для цифры", MaterialView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(CostOfPrintSizeLeafAndColorDictionary.class))
            navGroup.addItem(new SideNavItem("Стоимость отпечатка", CostOfPrintSizeLeafAndColorDictionary.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(PrintingMaterialView.class))
            navGroup.addItem(new SideNavItem("Краски для принтеров", PrintingMaterialView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(PrintSheetMaterialView.class))
            navGroup.addItem(new SideNavItem("Бумага листовая", PrintSheetMaterialView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        nav.addItem(navGroup);

        navGroup = new SideNavItem("Шаблоны");

        if (accessChecker.hasAccess(TemplatesView.class))
            navGroup.addItem(new SideNavItem("Новый редактор шаблонов", TemplatesView.class, LineAwesomeIcon.FILE.create()));

        if (accessChecker.hasAccess(WorkFlowView.class))
            navGroup.addItem(new SideNavItem("WorkFlows", WorkFlowView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        nav.addItem(navGroup);

        navGroup = new SideNavItem("Продукты");
        //if (accessChecker.hasAccess(TemplateView.class))
        //    dick.addItem(new SideNavItem("Шаблоны работ", TemplateView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(ProductsView.class))
            navGroup.addItem(new SideNavItem("Products", ProductsView.class, LineAwesomeIcon.GLOBE_SOLID.create()));
        nav.addItem(navGroup);

        navGroup = new SideNavItem("Работы");

        if (accessChecker.hasAccess(TypeOfOperationView.class))
            navGroup.addItem(new SideNavItem("Типы работ", TypeOfOperationView.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(OperationView.class))
            navGroup.addItem(new SideNavItem("Дополнительные работы", OperationView.class, LineAwesomeIcon.GLOBE_SOLID.create()));
        nav.addItem(navGroup);

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            byte[] profilePictureData = user.getProfilePicture();
            if (profilePictureData != null) {
                com.vaadin.flow.function.SerializableSupplier<InputStream> imageSupplier = () -> new ByteArrayInputStream(profilePictureData);

            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }


        return layout;
    }
/*
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

 */

}
