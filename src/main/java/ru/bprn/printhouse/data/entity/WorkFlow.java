package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Table(name = "workflow")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = WorkFlow.class)
public class WorkFlow {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    private String name = "Стандартное название";

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "standart_size", nullable = false)
    private StandartSize standartSize;

    @Positive
    private Double sizeX = 1.0;

    @Positive
    private Double sizeY = 1.0;
/*
    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "material", nullable = false)
    private Material material;
*/
    @PositiveOrZero
    private Integer quantityOfPrintLeaves = 1;

    @Positive
    private int quantityOfLeaves = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bleed", nullable = false)
    private Gap bleed;
/*
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "impose_case", nullable = false)
    private ImposeCase imposeCase;
*/
    @Column(columnDefinition = "mediumtext")
    private String strJSON = "";

    @PositiveOrZero
    private Integer quantityOfProduct = 0;

    @Positive
    private int listRows = 1;

    @Positive
    private int listColumns = 1;

    @Positive
    private int quantityProductionsOnLeaf = 1;

    @NotBlank
    private String orientation = "Автоматически";

    @PositiveOrZero
    private int leftGap = 0;

    @PositiveOrZero
    private int rightGap = 0;

    @PositiveOrZero
    private int topGap = 0;

    @PositiveOrZero
    private int bottomGap = 0;

    @PositiveOrZero
    private Double printSizeX = .0;

    @PositiveOrZero
    private Double printSizeY = .0;

    @PositiveOrZero
    private double fullProductSizeX = .0;

    @PositiveOrZero
    private double fullProductSizeY = .0;

}