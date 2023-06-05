package ru.bprn.printhouse.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;
import ru.bprn.printhouse.components.appnav.AppNav;
import ru.bprn.printhouse.components.appnav.AppNavItem;
import ru.bprn.printhouse.data.entity.User;
import ru.bprn.printhouse.security.AuthenticatedUser;
import ru.bprn.printhouse.views.about.AboutView;
import ru.bprn.printhouse.views.dictionary.QuantityColorsDictionary;
import ru.bprn.printhouse.views.dictionary.SizeOfPrintLeafDictionary;
import ru.bprn.printhouse.views.dictionary.TypeOfMaterialDictionary;
import ru.bprn.printhouse.views.dictionary.TypeOfPrinterDictionary;
import ru.bprn.printhouse.views.machine.printmashine.PrintersView;
import ru.bprn.printhouse.views.machine.printmashine.PrintSpeedMaterialDensityView;

import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
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

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();

        var dick = new AppNavItem("Словари");
        if (accessChecker.hasAccess(QuantityColorsDictionary.class))
            dick.addItem(new AppNavItem("Количество цветов", QuantityColorsDictionary.class, LineAwesomeIcon.EDIT_SOLID.create()));

        if (accessChecker.hasAccess(TypeOfPrinterDictionary.class))
            dick.addItem(new AppNavItem("Тип принтера", TypeOfPrinterDictionary.class, LineAwesomeIcon.EDIT_SOLID.create()));

        if (accessChecker.hasAccess(TypeOfMaterialDictionary.class))
            dick.addItem(new AppNavItem("Тип материала", TypeOfMaterialDictionary.class, LineAwesomeIcon.EDIT_SOLID.create()));

        if (accessChecker.hasAccess(SizeOfPrintLeafDictionary.class))
            dick.addItem(new AppNavItem("Размер печатного листа", SizeOfPrintLeafDictionary.class, LineAwesomeIcon.EDIT_SOLID.create()));

        nav.addItem(dick);

        dick =new AppNavItem("Оборудование");
        if (accessChecker.hasAccess(PrintersView.class))
            dick.addItem(new AppNavItem("ЦПМ", PrintersView.class, LineAwesomeIcon.GLOBE_SOLID.create()));

        if (accessChecker.hasAccess(PrintSpeedMaterialDensityView.class))
            dick.addItem(new AppNavItem("Скорость-плотность ЦПМ", PrintSpeedMaterialDensityView.class, LineAwesomeIcon.GLOBE_SOLID.create()));
        nav.addItem(dick);

        if (accessChecker.hasAccess(AboutView.class))
            nav.addItem(new AppNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create()));

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

