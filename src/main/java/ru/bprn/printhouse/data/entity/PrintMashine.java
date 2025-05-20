package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode (onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = PrintMashine.class)
public class PrintMashine {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @PositiveOrZero
    private Integer maxPrintAreaX;

    @NotNull
    @PositiveOrZero
    private Integer maxPrintAreaY;

    //SET FOREIGN_KEY_CHECKS=0;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gap")
    private Gap gap;

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

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
            name = "mashines_colors",
            joinColumns = @JoinColumn(name = "print_mashine_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "quantity_colors_id", referencedColumnName = "id"))
    private Set<QuantityColors> quantityColors = new HashSet<>();

    @NotNull
    @PositiveOrZero
    private Float finalCostOfCmykClick;

    @NotNull
    @PositiveOrZero
    private Float finalCostOfBlackClick;

    @NotNull
    @PositiveOrZero
    private Float finalCostOfSpotClick;

    @ManyToMany(fetch = FetchType.EAGER )
    @JoinTable(
            name = "mashines_leafs",
            joinColumns = @JoinColumn(name = "print_mashine_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "size_of_print_leaf_id", referencedColumnName = "id"))
    private Set<SizeOfPrintLeaf> sizeOfPrintLeaves = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "printers", targetEntity = Material.class)
    private Set<Material> materials = new HashSet<>();

    private Boolean hasDuplex = false;

    @NotNull
    @PositiveOrZero
    private Integer testLeaves = 0;



    @Override
    public String toString(){
            return getName();
    }

 }
