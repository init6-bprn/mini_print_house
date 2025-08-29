package ru.bprn.printhouse.views.operation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class ProductOperation {
    // Сущность ProductOperation, как мы обсуждали

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        // Ссылка на вашу справочную Operation
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "operation_id")
        private Operation operation;

        private Integer sequence;

        // Специфичные для этой связки параметры, которые переопределяют или дополняют Operation
        private double effectiveWasteFactor; // Фактический процент брака для этой операции в продукте

        @ManyToOne(fetch = FetchType.EAGER) // Выбранный материал для этой операции
        private AbstractMaterials selectedMaterial;

        @Lob
        private String customMachineTimeFormula; // Если нужно переопределить формулу из Operation
        @Lob
        private String customActionFormula;
        @Lob
        private String customMaterialFormula;

        @JdbcTypeCode(SqlTypes.JSON)
        private List<Variable> customVariables = new LinkedList<>(); // Если нужно добавить/переопределить переменные

        // Ссылка на Product
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "abstract_product_id")
        private AbstractProductType product;

        // Getters, Setters, etc.
}
