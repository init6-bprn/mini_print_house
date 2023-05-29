package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.bprn.printhouse.data.AbstractEntity;

import java.util.HashSet;
import java.util.Set;

 @Data @EqualsAndHashCode
 @ToString

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

    @ManyToMany(fetch = FetchType.EAGER, cascade = {
            CascadeType.MERGE
    })
    //@Fetch(FetchMode.JOIN)
    @JoinTable(
            name = "mashines_leafs",
            joinColumns = @JoinColumn(name = "print_mashine_id"),
            inverseJoinColumns = @JoinColumn(name = "size_of_print_leaf_id"))
    private Set<SizeOfPrintLeaf> sizeOfPrintLeaves = new HashSet<>();

    private void addSizeOfPrintLeaf(SizeOfPrintLeaf prnLeaf){
        this.sizeOfPrintLeaves.add(prnLeaf);
        prnLeaf.getPrintMashineSet().add(this);
    }

    private void removeSizeOfPrintLeaf(SizeOfPrintLeaf prnLeaf){
        this.sizeOfPrintLeaves.remove(prnLeaf);
        prnLeaf.getPrintMashineSet().remove(this);
    }

    public PrintMashine() {
        this.name = "Same printer";
    }

    public PrintMashine(String name) {
        this.name = name;
    }

    public String toString (){
        return this.name;
    }

}
