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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "print_speed_material_density")
public class PrintSpeedMaterialDensity extends AbstractEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @PositiveOrZero
    @Max(300)
    private Integer densityNoMore = 0;

    @NotNull
    @PositiveOrZero
    @Max(300)
    private Integer speed = 0;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "type_of_material", nullable = false )
    private TypeOfMaterial typeOfMaterial;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "print_mashine", nullable = false )
    private PrintMashine printMashine;

}