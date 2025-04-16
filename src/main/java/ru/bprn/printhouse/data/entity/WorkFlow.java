package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@ToString
//@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "workflow")
public class WorkFlow {
    @Id
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bleed", nullable = false)
    private Gap bleed;

    @Column(columnDefinition = "mediumtext")
    private String strJSON = "";

    @PositiveOrZero
    private Integer quantityOfPrintLeaves = 1;

    @PositiveOrZero
    private Integer quantityOfProduct = 0;

    @Positive
    private int rowsOnLeaf = 1;

    @Positive
    private int columnsOnLeaf = 1;

    @Positive
    private int quantityProductionsOnLeaf = 1;

    @PositiveOrZero
    private Double printSizeX = .0;

    @PositiveOrZero
    private Double printSizeY = .0;

    @PositiveOrZero
    private Double printAreaX = .0;

    @PositiveOrZero
    private Double printAreaY = .0;

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
        WorkFlow workFlow = (WorkFlow) o;
        return getId() != null && Objects.equals(getId(), workFlow.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}