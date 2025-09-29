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

import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.CalculationPhase;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.operation.service.OperationVariableService;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "operation")
public class Operation{
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

    // Материал
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<AbstractMaterials> listOfMaterials = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private AbstractMaterials defaultMaterial;


    @JdbcTypeCode(SqlTypes.JSON)
    private List<Variable> variables = new LinkedList<>();

    @ManyToOne
    private Formulas machineTimeFormulaTemplate;
    @Column(columnDefinition = "TEXT")
    private String machineTimeExpression = "";

    @ManyToOne
    private Formulas actionTimeFormulaTemplate;
    @Column(columnDefinition = "TEXT")
    private String actionTimeExpression = "";

    @ManyToOne
    private Formulas materialAmountFormulaTemplate;
    @Column(columnDefinition = "TEXT")
    private String materialAmountExpression = "";

    @ManyToOne
    private Formulas wasteFormulaTemplate;
    @Column(columnDefinition = "TEXT")
    private String wasteExpression = "";

    @ManyToOne
    private Formulas setupFormulaTemplate;
    @Column(columnDefinition = "TEXT")
    private String setupExpression = "";

    /**
     * Внутренний record для удобной передачи информации о формуле в сервис расчета.
     */
    public record FormulaInfo(String expression, CalculationPhase phase, int priority) {}

    @JsonIgnore
    public FormulaInfo getMachineTimeFormulaInfo() {
        return createFormulaInfo(machineTimeExpression, machineTimeFormulaTemplate, CalculationPhase.TECHNICAL_CALCULATION);
    }

    @JsonIgnore
    public FormulaInfo getActionTimeFormulaInfo() {
        return createFormulaInfo(actionTimeExpression, actionTimeFormulaTemplate, CalculationPhase.TECHNICAL_CALCULATION);
    }

    @JsonIgnore
    public FormulaInfo getMaterialAmountFormulaInfo() {
        return createFormulaInfo(materialAmountExpression, materialAmountFormulaTemplate, CalculationPhase.TECHNICAL_CALCULATION);
    }

    @JsonIgnore
    public FormulaInfo getWasteFormulaInfo() {
        return createFormulaInfo(wasteExpression, wasteFormulaTemplate, CalculationPhase.WASTE_CALCULATION);
    }

    @JsonIgnore
    public FormulaInfo getSetupFormulaInfo() {
        return createFormulaInfo(setupExpression, setupFormulaTemplate, CalculationPhase.WASTE_CALCULATION);
    }

    /**
     * Хелпер для создания FormulaInfo. Если шаблон не задан, используются значения по умолчанию.
     */
    private FormulaInfo createFormulaInfo(String expression, Formulas template, CalculationPhase defaultPhase) {
        if (expression == null || expression.isBlank()) {
            return null; // Если выражения нет, то и информации о формуле нет
        }
        if (template != null) {
            return new FormulaInfo(expression, template.getPhase(), template.getPriority());
        } else {
            // Возвращаем с фазой по умолчанию и средним приоритетом, если шаблон не выбран
            return new FormulaInfo(expression, defaultPhase, 10);
        }
    }

    public void initializeVariables(OperationVariableService variableService) {
        this.setVariables(variableService.getPredefinedVariables());
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
