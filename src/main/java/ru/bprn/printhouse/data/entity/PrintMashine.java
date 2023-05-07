package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;
import jakarta.validation.constraints.*;

@Data
@Entity
public class PrintMashine extends AbstractEntity {

    @NotNull
    @NotEmpty
    private String name = "Какой-то принтер";

    private Integer maxPrintAreaX;

    private Integer maxPrintAreaY;

    private Integer cost;

    private Integer click;

    private Integer madeClick;

    @NotNull
    @NotEmpty
    private Float priceOfCMYKClick = 0f;

    @NotNull
    @NotEmpty
    private Float priceOfBlackClick = 0f;

    @NotNull
    @NotEmpty
    private Float priceOfSpotClick = 0f;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "type_of_printer", nullable = false )
    private TypeOfPrinter typeOfPrinter;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "quantity_colors_id", nullable = false)
    private QuantityColors quantityColors;


    public PrintMashine() {
    }

    public PrintMashine(String name) {
        this.name = name;
    }

}
