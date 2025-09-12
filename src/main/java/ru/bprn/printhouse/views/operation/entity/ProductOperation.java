package ru.bprn.printhouse.views.operation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.templates.ProductOperationEditor;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ProductOperation {
    // Сущность ProductOperation, как мы обсуждали

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        // Ссылка на вашу справочную Operation
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "operation_id")
        @ToString.Exclude
        private Operation operation;

        // Порядок выполнения операций
        private Integer sequence;

        // Специфичные для этой связки параметры, которые переопределяют или дополняют Operation
        private double effectiveWasteFactor; // Фактический процент брака для этой операции в продукте

        @ManyToOne(fetch = FetchType.EAGER) // Выбранный материал для этой операции
        private AbstractMaterials selectedMaterial;

        private Set<AbstractMaterials> alternativeMaterials = new HashSet<>();

        @Lob
        private String customMachineTimeFormula; // Если нужно переопределить формулу из Operation
        @Lob
        private String customActionFormula;
        @Lob
        private String customMaterialFormula;

        @JdbcTypeCode(SqlTypes.JSON)
        private List<Variable> customVariables = new ArrayList<>(); // Если нужно добавить/переопределить переменные

        // Ссылка на Product
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "abstract_product_id")
        @ToString.Exclude
        private AbstractProductType product;

        private boolean switchOff = false;
        
        public ProductOperation(Operation operation) {
                this.operation = operation;
                this.sequence = 0; // Default value
                this.effectiveWasteFactor = 0.0; // Default value
                this.switchOff = operation.isSwitchOff();
                this.customMachineTimeFormula = operation.getMachineTimeFormula();
                this.customActionFormula = operation.getActionFormula();
                this.customMaterialFormula = operation.getMaterialFormula();
                this.selectedMaterial = operation.getDefaultMaterial();
                this.customVariables = new ArrayList<>(operation.getVariables());
        }

        // Getters, Setters, etc.

        @Override
        public final boolean equals(Object object) {
                if (this == object) return true;
                if (object == null) return false;
                Class<?> oEffectiveClass = object instanceof HibernateProxy ? ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass() : object.getClass();
                Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
                if (thisEffectiveClass != oEffectiveClass) return false;
                ProductOperation that = (ProductOperation) object;
                return getId() != null && Objects.equals(getId(), that.getId());
        }

        @Override
        public final int hashCode() {
                return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
        }
}
