package ru.bprn.printhouse.data.entity;

import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

import javax.persistence.*;

@Data
@Entity
public class PrintMashine extends AbstractEntity {

    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "type_of_printer", nullable = false )
    private TypeOfPrinter typeOfPrinter;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "quantity_colors_id", nullable = false)
    private QuantityColors quantityColors;


    public PrintMashine() {
    }

}
