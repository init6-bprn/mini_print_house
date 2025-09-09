package ru.bprn.printhouse.views.dictionary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.crudui.crud.CrudListener;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import java.util.List;

public abstract class AbstractDictionaryView<T> extends VerticalLayout {

    protected GridCrud<T> crud;
    protected DefaultCrudFormFactory<T> formFactory;

    public AbstractDictionaryView(Class<T> entityClass, CrudListener<T> crudListener, String[] gridProperties, String[] formProperties) {
        // 1. Создаем фабрику для формы
        formFactory = new DefaultCrudFormFactory<>(entityClass) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                // Растягиваем первое поле на 2 колонки для лучшего вида, если оно есть
                if (!fields.isEmpty()) {
                    Component firstField = (Component) fields.get(0);
                    formLayout.setColspan(firstField, 2);
                }
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties(formProperties);

        // 2. Позволяем подклассам настраивать фабрику
        configureFormFactory(formFactory);

        // 3. Создаем CRUD компонент
        crud = new GridCrud<>(entityClass, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns(gridProperties);
        crud.setCrudListener(crudListener);

        // 4. Настраиваем layout
        setSizeFull();
        add(crud);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");
    }

    /**
     * Метод-крючок для дочерних классов, чтобы настраивать фабрику форм.
     * Например, для добавления ComboBoxProvider.
     * @param formFactory фабрика для настройки.
     */
    protected void configureFormFactory(DefaultCrudFormFactory<T> formFactory) {
        // По умолчанию ничего не делает. Переопределяется в подклассах.
    }
}