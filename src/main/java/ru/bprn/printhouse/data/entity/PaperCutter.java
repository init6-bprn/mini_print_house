package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Entity
@Table(name = "paper_cutters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (onlyExplicitlyIncluded = true)
@Getter
@Setter

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
    private int maxSizeX=0;

    @NotNull
    @PositiveOrZero
    private int maxSizeY=0;

    @NotNull
    @PositiveOrZero
    private int maxSizeZ=0;

    @NotNull
    @PositiveOrZero
    private int minSize = 35;

}