package ru.bprn.printhouse.views.operation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import ru.bprn.printhouse.views.operation.service.OperationVariableService;
import ru.bprn.printhouse.views.templates.entity.Variable;

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
    private boolean haveMachine = true;

    // Работа
    private boolean haveAction = true;

    // Материал
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<AbstractMaterials> listOfMaterials = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private AbstractMaterials defaultMaterial;
    private boolean haveMaterial = true;


    @JdbcTypeCode(SqlTypes.JSON)
    private List<Variable> variables = new LinkedList<>();

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

    public void initializeVariables(OperationVariableService variableService) {
        this.setVariables(variableService.getPredefinedVariables());
    }

    @JsonIgnore
    public String getMachineTimeFormula() {
        return getVariableValueAsString("machineTimeFormula").orElse("");
    }

    @JsonIgnore
    public String getActionFormula() {
        return getVariableValueAsString("actionFormula").orElse("");
    }

    @JsonIgnore
    public String getMaterialFormula() {
        return getVariableValueAsString("materialFormula").orElse("");
    }

    @JsonIgnore
    public String getOperationWasteFormula() {
        return getVariableValueAsString("operationWasteFormula").orElse("0");
    }

    @JsonIgnore
    public String getSetupWasteFormula() {
        return getVariableValueAsString("setupWasteFormula").orElse("0");
    }

    private Optional<String> getVariableValueAsString(String key) {
        if (getVariables() == null) return Optional.empty();
        return getVariables().stream().filter(v -> key.equals(v.getKey())).map(Variable::getValue).findFirst();
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
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
