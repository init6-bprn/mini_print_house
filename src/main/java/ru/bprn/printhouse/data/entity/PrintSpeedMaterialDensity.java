package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.bprn.printhouse.data.AbstractEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "print_speed_material_density")
public class PrintSpeedMaterialDensity extends AbstractEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Thickness thickness;

    @ManyToOne(fetch = FetchType.EAGER)
    private SizeOfPrintLeaf sizeOfPrintLeaf;

    @NotNull
    @PositiveOrZero
    @Max(3600)
    private int timeOfOperation = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    private PrintMashine printMashine;

}