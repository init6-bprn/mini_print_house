package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (onlyExplicitlyIncluded = true)
@ToString (onlyExplicitlyIncluded = true)
@Getter
@Setter

@Entity
public class DigitalPrintTemplate{
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    @NotEmpty
    @ToString.Include
    private String name = "Name";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gap")
    private Gap gap;

    @NotNull
    @Positive
    private Integer quantity = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cover")
    private QuantityColors coverQuantityColors;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "back")
    private QuantityColors backQuantityColors;


    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
            name = "digital_print_template_material",
            joinColumns = @JoinColumn(name = "material_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "digital_print_template_id", referencedColumnName = "id"))
    private Set<Material> material = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "print_mashine")
    private PrintMashine printMashine;

    @NotNull
    @Positive
    private Integer rowsOnLeaf = 1;

    @NotNull
    @Positive
    private Integer columnsOnLeaf = 1;

    @NotNull
    @Positive
    private Integer quantityOfPrintLeaves = 1;

}