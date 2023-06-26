package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.bprn.printhouse.data.AbstractWork;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (callSuper = true)
@ToString
@Table(name = "works_for_print")
@Entity
public class WorksForPrint extends AbstractWork {

    private int duplex = 0;

    private Double priceOfOneOperation = 0d;

    private Integer totalQuantity = 1;

    @ManyToOne
    @JoinColumn(name = "quantity_colors_id")
    private  QuantityColors quantityColors;

    @ManyToOne
    @JoinColumn(name = "print_mashine_id")
    private PrintMashine printMashine;

    private Integer columns_ = 1;

    private Integer rows_ = 1;

    @Override
    public Integer calculateTime() {
        return null;
    }

    @Override
    public Double calculateCost() {
        return null;
    }

}