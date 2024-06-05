package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @ToString.Include
    private String name = "";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gap")
    private Gap gap;

    @NotEmpty
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

    @NotEmpty
    @Positive
    private Integer rowsOnLeaf = 1;

    @NotEmpty
    @Positive
    private Integer columnsOnLeaf = 1;

    @NotEmpty
    @Positive
    private Integer quantityOfPrintLeaves = 1;

}