package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.data.AbstractWork;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("P")
@Table(name = "works_for_print")
public class Works extends AbstractWork {

    private Boolean duplex = false;

    private Double priceOfOneOperation = 0d;

    private Integer totalQuantity = 1;

    @ManyToOne
    @JoinColumn(name = "quantity_colors_id")
    private  QuantityColors quantityColors;

    @ManyToOne
    @JoinColumn(name = "print_mashine_id")
    private PrintMashine printMashine;

    private Integer column = 1;

    private Integer row = 1;

}