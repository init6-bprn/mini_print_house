package ru.bprn.printhouse.views.operation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.templates.entity.HasMateria;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "operation")
public class Operation implements HasMateria {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotBlank
    private String name = "Additional Work";

    private String description = "";

    @ManyToOne(fetch = FetchType.EAGER)
    private TypeOfOperation typeOfOperation;

    // Работа может быть убрана из расчета?
    private boolean switchOff = false;

    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> parentClassSet = new HashSet<>();

    // Используемое оборудование
    @ManyToOne(fetch = FetchType.EAGER)
    private AbstractMachine abstractMachine = null;
    private String machineTimeFormula = "";
    private boolean haveMachine = true;

    // Работа
    private String actionFormula = "";
    private boolean haveAction = true;

    // Материал
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<AbstractMaterials> listOfMaterials = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private AbstractMaterials defaultMaterial;
    private String materialFormula = "";
    private boolean haveMaterial = true;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Double> variables = new HashMap<>();

    @Transient
    @Override
    public AbstractMaterials getDefaultMat() {
        return null;
    }

    @Transient
    @Override
    public Set<AbstractMaterials> getSelectedMat() {
        return Set.of();
    }

    @Transient
    @Override
    public String getMatFormula() {
        return "";
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Operation operation = (Operation) o;
        return getId() != null && Objects.equals(getId(), operation.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
