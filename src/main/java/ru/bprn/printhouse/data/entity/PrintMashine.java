package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;
import jakarta.validation.constraints.*;

import java.util.Set;

@Data
@Entity
public class PrintMashine extends AbstractEntity {

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @PositiveOrZero
    private Integer maxPrintAreaX;

    @NotNull
    @PositiveOrZero
    private Integer maxPrintAreaY;

    @NotNull
    @PositiveOrZero
    private Integer cost;

    @NotNull
    @PositiveOrZero
    private Integer clicks;

    @NotNull
    @PositiveOrZero
    private Integer madeOfClicks;

    @NotNull
    @PositiveOrZero
    private Float priceOfCmykClick = 0f;

    @NotNull
    @PositiveOrZero
    private Float priceOfBlackClick = 0f;

    @NotNull
    @PositiveOrZero
    private Float priceOfSpotClick = 0f;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "type_of_printer", nullable = false )
    private TypeOfPrinter typeOfPrinter;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "quantity_colors_id", nullable = false)
    private QuantityColors quantityColors;

    @NotNull
    @PositiveOrZero
    private Float finalCostOfCmykClick;

    @NotNull
    @PositiveOrZero
    private Float finalCostOfBlackClick;

    @NotNull
    @PositiveOrZero
    private Float finalCostOfSpotClick;

    @ManyToMany
    @JoinTable(
            name = "print_leafs_jt",
            joinColumns = @JoinColumn(name = "print_mashine_id"),
            inverseJoinColumns = @JoinColumn(name = "size_of_print_leaf"))
    private Set<SizeOfPrintLeaf> sizeOfPrintLeaves;


    public PrintMashine() {
        this.name = "Какой-то принтер";
    }

    public PrintMashine(String name) {
        this.name = name;
    }

    public String toString (){
        return this.name;
    }

}
