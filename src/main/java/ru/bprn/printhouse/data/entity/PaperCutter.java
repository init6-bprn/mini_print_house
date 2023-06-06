package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "paper_cutters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (onlyExplicitlyIncluded = true)

public class PaperCutter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @PositiveOrZero
    private Integer maxSizeX=0;

    @NotNull
    @PositiveOrZero
    private Integer maxSizeY=0;

    @NotNull
    @PositiveOrZero
    private Integer maxSizeZ=0;

    @NotNull
    @PositiveOrZero
    private Integer gap=10;

    @NotNull
    @PositiveOrZero
    private Integer costOfCutter=0;

    @NotNull
    @PositiveOrZero
    private Integer cuts=0;

    @NotNull
    @PositiveOrZero
    private Integer madeOfCuts=0;

    @NotNull
    @PositiveOrZero
    private Integer costOfKnifeSharpening=0;

    @NotNull
    @PositiveOrZero
    private Integer cutsToSharpening=0;

    @NotNull
    @PositiveOrZero
    private Integer madeOfCutsBeforeSharpening=0;

    @NotNull
    @PositiveOrZero
    private Float finalCostOfCut=0f;


}