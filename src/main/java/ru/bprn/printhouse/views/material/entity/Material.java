package ru.bprn.printhouse.views.material.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "material")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Material.class)
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    private String article = "";

    @NotNull
    @NotEmpty
    private String name = "Paper";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "size_of_print_leaf", nullable = false)
    private SizeOfPrintLeaf sizeOfPrintLeaf;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_of_material", nullable = false )
    private TypeOfMaterial typeOfMaterial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "thickness", nullable = false )
    private Thickness thickness;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "material_printer",
            joinColumns = @JoinColumn(name = "print_mashine_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "material_id", referencedColumnName = "id"))
    private Set<PrintMashine> printers = new HashSet<>();

    private String measureString = "";
/*
    @OneToOne(mappedBy = "material")
    private PriceOfMaterial priceOfMaterial;
*/
    private Double priceOfLeaf = 0d;

    private int quantity = 0;

    public String toString(){return this.thickness+" "+this.sizeOfPrintLeaf.toString()+" "+this.name;}
}