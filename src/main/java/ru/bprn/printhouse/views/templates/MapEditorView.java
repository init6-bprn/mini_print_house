package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class MapEditorView extends VerticalLayout {
    @Getter
    @Setter
    private Map<String, Double> map = new LinkedHashMap<>();

    private List<Map.Entry<String, Double>> entryList = new ArrayList<>();
    private Grid<Map.Entry<String, Double>> grid = new Grid<>();

    public MapEditorView() {
        entryList.addAll(map.entrySet());

        configureGrid();
        add(grid, createAddButton(), createSaveButton());
    }

    private void configureGrid() {
        grid.setItems(entryList);

        // Колонка ключа
        grid.addColumn(Map.Entry::getKey).setHeader("Ключ").setEditorComponent(new TextField());

        // Колонка значения
        grid.addColumn(Map.Entry::getValue).setHeader("Значение").setEditorComponent(new NumberField());

        // Удаление строки
        grid.addComponentColumn(entry -> {
            Button deleteButton = new Button("Удалить", e -> {
                entryList.remove(entry);
                grid.setItems(entryList);
            });
            return deleteButton;
        }).setHeader("Действие");


        // Включаем редактор
        Editor<Map.Entry<String, Double>> editor = grid.getEditor();
        editor.setBuffered(true);

        Binder<Map.Entry<String, Double>> binder = new Binder<>();
        TextField keyField = new TextField();
        NumberField valueField = new NumberField();

        binder.forField(keyField)
                .bind(Map.Entry::getKey, (entry, value) -> {
                    entryList.remove(entry);
                    entryList.add(new AbstractMap.SimpleEntry<>(value, entry.getValue()));
                });

        binder.forField(valueField)
                .bind(Map.Entry::getValue, Map.Entry::setValue);

        editor.setBinder(binder);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private Button createAddButton() {
        return new Button("Добавить", e -> {
            entryList.add(new AbstractMap.SimpleEntry<>("Новый ключ", 0.0));
            grid.setItems(entryList);
        });
    }

    private Button createSaveButton() {
        return new Button("Сохранить", e -> {
            map.clear();
            for (Map.Entry<String, Double> entry : entryList) {
                map.put(entry.getKey(), entry.getValue());
            }
            Notification.show("Сохранено!");
        });
    }

}
