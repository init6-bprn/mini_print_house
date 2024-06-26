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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;
import ru.bprn.printhouse.data.entity.User;
import ru.bprn.printhouse.security.AuthenticatedUser;
import ru.bprn.printhouse.views.about.AboutView;
import ru.bprn.printhouse.views.dictionary.*;
import ru.bprn.printhouse.views.machine.cutters.PaperCuttersView;
import ru.bprn.printhouse.views.machine.printers.PrintSpeedMaterialDensityView;
import ru.bprn.printhouse.views.machine.printers.PrintersView;
import ru.bprn.printhouse.views.material.MaterialView;
import ru.bprn.printhouse.views.template.DigitalPrintTemplateView;

import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
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
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
       SideNav nav = new SideNav();

        var dick = new SideNavItem("Словари");
        if (accessChecker.hasAccess(QuantityColorsDictionary.class))
            dick.addItem(new SideNavItem("Количество цветов", QuantityColorsDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(TypeOfPrinterDictionary.class))
            dick.addItem(new SideNavItem("Тип принтера", TypeOfPrinterDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(TypeOfMaterialDictionary.class))
            dick.addItem(new SideNavItem("Тип материала", TypeOfMaterialDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(ThicknessDictionary.class))
            dick.addItem(new SideNavItem("Плотность материала", ThicknessDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(SizeOfPrintLeafDictionary.class))
            dick.addItem(new SideNavItem("Размер печатного листа", SizeOfPrintLeafDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(StandartSizeDictionary.class))
            dick.addItem(new SideNavItem("Размер изделия", StandartSizeDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(GapDictionary.class))
            dick.addItem(new SideNavItem("Отступы", GapDictionary.class, VaadinIcon.RECORDS.create()));

        if (accessChecker.hasAccess(ImposeCaseDictionary.class))
            dick.addItem(new SideNavItem("Спуск полос", ImposeCaseDictionary.class, VaadinIcon.RECORDS.create()));

        nav.addItem(dick);

        dick = new SideNavItem("Оборудование");
        if (accessChecker.hasAccess(PrintersView.class))
            dick.addItem(new SideNavItem("ЦПМ", PrintersView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(PaperCuttersView.class))
            dick.addItem(new SideNavItem("Резаки", PaperCuttersView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(PrintSpeedMaterialDensityView.class))
            dick.addItem(new SideNavItem("Скорость-плотность ЦПМ", PrintSpeedMaterialDensityView.class, LineAwesomeIcon.GLOBE_SOLID.create()));
        nav.addItem(dick);

        dick = new SideNavItem("Материалы");
        if (accessChecker.hasAccess(MaterialView.class))
            dick.addItem(new SideNavItem("Бумага для цифры", MaterialView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(MaterialView.class))
            dick.addItem(new SideNavItem("Стоимость отпечатка", CostOfPrintSizeLeafAndColorDictionary.class, LineAwesomeIcon.GLOBE_SOLID.create()));
        nav.addItem(dick);

        dick = new SideNavItem("Шаблоны");
        //if (accessChecker.hasAccess(TemplateView.class))
        //    dick.addItem(new SideNavItem("Шаблоны работ", TemplateView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(DigitalPrintTemplateView.class))
            dick.addItem(new SideNavItem("Шаблоны печати", DigitalPrintTemplateView.class, LineAwesomeIcon.GLOBE_SOLID.create()));
        nav.addItem(dick);

        if (accessChecker.hasAccess(AboutView.class))
            nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create()));

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
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

