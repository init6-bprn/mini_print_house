package ru.bprn.printhouse.annotation;

import com.vaadin.flow.component.icon.VaadinIcon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuItem {
    String name();
    VaadinIcon icon() default VaadinIcon.QUESTION_CIRCLE;
    String context() default "default";
    String description() default "Menu Item";
}
