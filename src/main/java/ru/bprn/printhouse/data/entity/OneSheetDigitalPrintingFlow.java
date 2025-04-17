package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.Set;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "one_leaf_digital_print_flow")
public class OneSheetDigitalPrintingFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    private String name = "Стандартное название";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "standart_size", nullable = false)
    private StandartSize standartSize;

    @Positive
    private Double productSizeX = 1.0;

    @Positive
    private Double productSizeY = 1.0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bleed", nullable = false)
    private Gap bleed;

    @ManyToOne
    @JoinColumn(name = "print_mashine_id")
    private PrintMashine printMashine;

    @ManyToOne
    @JoinColumn(name = "quantity_colors_cover_id")
    private QuantityColors quantityColorsCover;

    @ManyToOne
    @JoinColumn(name = "quantity_colors_back_id")
    private QuantityColors quantityColorsBack;

    private Integer quantityOfExtraLeaves = 0;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formulas formula;

    private Set<Material> materials;

    @ManyToOne
    @JoinColumn(name = "default_material_id")
    private Material defaultMaterial;

    @ManyToOne
    @JoinColumn(name = "margins_id")
    private Gap margins;

    private String description;

    @ManyToOne
    @JoinColumn(name = "material_formula_id")
    private Formulas materialFormula;

    @JsonIgnoreProperties
    private int fullSizeX = 10000;

    @JsonIgnoreProperties
    private int fullSizeY = 10000;

    @NotBlank
    private String orientation = "Автоматически";

    @Column(columnDefinition = "mediumtext")
    private String additionalWorkStrJSON = "";

    @PositiveOrZero
    private Integer quantityOfProduct = 0;

    @Positive
    private int rowsOnSheet = 1;

    @Positive
    private int columnsOnSheet = 1;

    @Positive
    private int quantityProductionsOnSheet = 1;

    @PositiveOrZero
    private Integer quantityOfPrintSheets = 1;

    @PositiveOrZero
    private Double printSheetSizeX = .0;

    @PositiveOrZero
    private Double printSheetSizeY = .0;

    @PositiveOrZero
    private Double printAreaSizeX = .0;

    @PositiveOrZero
    private Double printAreaSizeY = .0;

    @PositiveOrZero
    private double fullProductSizeX = .0;

    @PositiveOrZero
    private double fullProductSizeY = .0;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ru.bprn.printhouse.data.entity.WorkFlow workFlow = (ru.bprn.printhouse.data.entity.WorkFlow) o;
        return getId() != null && Objects.equals(getId(), workFlow.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}
