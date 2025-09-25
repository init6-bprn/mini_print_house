package ru.bprn.printhouse.views;

import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;
import ru.bprn.printhouse.data.entity.User;
import ru.bprn.printhouse.security.AuthenticatedUser;
import ru.bprn.printhouse.views.dictionary.*;
import ru.bprn.printhouse.views.machine.printers.DigitalPrintingMachineView;
import ru.bprn.printhouse.views.material.PrintSheetMaterialView;
import ru.bprn.printhouse.views.price.PriceOfMachineView;
import ru.bprn.printhouse.views.price.PriceOfMaterialView;
import ru.bprn.printhouse.views.material.PrintingMaterialView;
import ru.bprn.printhouse.views.operation.OperationView;
import ru.bprn.printhouse.views.operation.TypeOfOperationView;
import ru.bprn.printhouse.views.products.ProductCatalogView;
import ru.bprn.printhouse.views.templates.TemplatesView;

import java.io.ByteArrayInputStream;
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

        SideNavItem dictionariesSection = new SideNavItem("Словари");
        addNavItemIfAccessible(dictionariesSection, "Тип принтера", TypeOfPrinterDictionary.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(dictionariesSection, "Тип материала", TypeOfMaterialDictionary.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(dictionariesSection, "Плотность материала", ThicknessDictionary.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(dictionariesSection, "Размер изделия", StandartSizeDictionary.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(dictionariesSection, "Переменные для работ", VariablesForMainWorksDictionary.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(dictionariesSection, "Цены на материалы", PriceOfMaterialView.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(dictionariesSection, "Цены на оборудование", PriceOfMachineView.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(dictionariesSection, "Словарь формул устройств", FormulasDictionary.class, VaadinIcon.RECORDS.create());
        if (dictionariesSection.getItems().stream().findAny().isPresent()) {
            nav.addItem(dictionariesSection);
        }

        SideNavItem equipmentSection = new SideNavItem("Оборудование");
        addNavItemIfAccessible(equipmentSection, "Листовые принтеры", DigitalPrintingMachineView.class, LineAwesomeIcon.GLOBE_SOLID.create());
        if (equipmentSection.getItems().stream().findAny().isPresent()) {
            nav.addItem(equipmentSection);
        }

        SideNavItem materialsSection = new SideNavItem("Материалы");
        addNavItemIfAccessible(materialsSection, "Краски для принтеров", PrintingMaterialView.class, LineAwesomeIcon.GLOBE_SOLID.create());
        addNavItemIfAccessible(materialsSection, "Бумага листовая", PrintSheetMaterialView.class, LineAwesomeIcon.GLOBE_SOLID.create());
        if (materialsSection.getItems().stream().findAny().isPresent()) {
            nav.addItem(materialsSection);
        }

        SideNavItem templatesSection = new SideNavItem("Шаблоны");
        addNavItemIfAccessible(templatesSection, "Редактор шаблонов", TemplatesView.class, LineAwesomeIcon.FILE.create());
        if (templatesSection.getItems().stream().findAny().isPresent()) {
            nav.addItem(templatesSection);
        }

        SideNavItem productsSection = new SideNavItem("Продукты");
        addNavItemIfAccessible(productsSection, "Каталог продукции", ProductCatalogView.class, LineAwesomeIcon.BOOK_SOLID.create());
        if (productsSection.getItems().stream().findAny().isPresent()) {
            nav.addItem(productsSection);
        }

        SideNavItem operationsSection = new SideNavItem("Работы");
        addNavItemIfAccessible(operationsSection, "Типы работ", TypeOfOperationView.class, VaadinIcon.RECORDS.create());
        addNavItemIfAccessible(operationsSection, "Дополнительные работы", OperationView.class, LineAwesomeIcon.GLOBE_SOLID.create());
        if (operationsSection.getItems().stream().findAny().isPresent()) {
            nav.addItem(operationsSection);
        }

        return nav;
    }

    private void addNavItemIfAccessible(SideNavItem parent, String label, Class<? extends Component> view, Component icon) {
        if (accessChecker.hasAccess(view)) {
            parent.addItem(new SideNavItem(label, view, icon));
        }
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            if (user.getProfilePicture() != null) {
                // Этот конструктор StreamResource не является устаревшим.
                // Он принимает InputStreamFactory (лямбда-выражение), что является текущим рекомендуемым API.
                StreamResource imageResource = new StreamResource("profile-pic",
                        () -> new ByteArrayInputStream(user.getProfilePicture()));
                avatar.setImageResource(imageResource);
            }
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

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
