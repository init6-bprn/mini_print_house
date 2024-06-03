package ru.bprn.printhouse.views.template;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.service.PrintMashineService;

@Component
@Route("Calc")
public class CalcView extends VerticalLayout {

    private final PrintMashineService printMashineService;

    @Autowired
    public CalcView(PrintMashineService printMashineService) {
        this.printMashineService = printMashineService;

        // Создание компонентов интерфейса
        //ComboBox<PrintMashine> equipment = new ComboBox<>("Оборудование");
        // Привязка данных к ComboBox
        //equipment.setItems(this.printMashineService.findAll());

        ComboBox<String> printFormat = new ComboBox<>("Формат печати");
        printFormat.setItems("SRA3");

        ComboBox<String> colorMode = new ComboBox<>("Цветность");
        colorMode.setItems("4+0");

        ComboBox<String> material = new ComboBox<>("Материал/Товар/Сувенир");
        material.setItems("Бумага мелованная");

        NumberField grammage = new NumberField("Граммаж:");
        grammage.setValue(300.0);

        ComboBox<String> layoutComponent = new ComboBox<>("Макет Компонента:");
        layoutComponent.setItems("A4");

        NumberField width = new NumberField("Ширина, мм:");
        width.setValue(210.0);

        NumberField height = new NumberField("Высота, мм:");
        height.setValue(297.0);

        NumberField bleed = new NumberField("Вылет, мм:");
        bleed.setValue(2.0);

        TextField sheetCount = new TextField("Кол-во изделий: 2");

        TextField totalCount = new TextField("Всего изделий: 2");

        TextField calculationTime = new TextField("Время расчета: 24.05.24 - 09:35");

        // Настройки раскладки листов
        Checkbox manual = new Checkbox("Вручную");
        manual.setValue(true);

        TextField sheets = new TextField("ЛИСТАЖ-МЕТРАЖ-КОЛИЧЕСТВО");
        sheets.setValue("1,000 листов");

        Checkbox automatic = new Checkbox("Автомат.");

        NumberField overage = new NumberField("Припадка (Т):");
        overage.setValue(0.0);

        // Организация компонентов в макете
        HorizontalLayout topRow = new HorizontalLayout(/*equipment,*/ printFormat, colorMode);
        HorizontalLayout secondRow = new HorizontalLayout(material, grammage);
        HorizontalLayout thirdRow = new HorizontalLayout(layoutComponent, width, height, bleed);
        HorizontalLayout fourthRow = new HorizontalLayout(sheetCount, totalCount, calculationTime);
        HorizontalLayout fifthRow = new HorizontalLayout(manual, sheets, automatic, overage);

        // Добавление компонентов на главный макет
        add(topRow, secondRow, thirdRow, fourthRow, fifthRow);
    }
}
