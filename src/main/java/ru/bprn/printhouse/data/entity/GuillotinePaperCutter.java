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
@Table(name = "guillotine_paper_cutter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (onlyExplicitlyIncluded = true)

public class GuillotinePaperCutter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @PositiveOrZero
    private Integer maxPrintAreaX=0;

    @NotNull
    @PositiveOrZero
    private Integer maxPrintAreaY=0;

    @NotNull
    @PositiveOrZero
    private Integer gap=10;

    @NotNull
    @PositiveOrZero
    private Integer costOfCutter=0;

    @NotNull
    @PositiveOrZero
    private Integer cuts;

    @NotNull
    @PositiveOrZero
    private Integer madeOfCuts;

    @NotNull
    @PositiveOrZero
    private Integer costOfKnifeSharpening;

    @NotNull
    @PositiveOrZero
    private Integer cutsToSharpening;

    @NotNull
    @PositiveOrZero
    private Integer madeOfCutsBeforeSharpening;

    @NotNull
    @PositiveOrZero
    private Float finalCostOfCut;


}