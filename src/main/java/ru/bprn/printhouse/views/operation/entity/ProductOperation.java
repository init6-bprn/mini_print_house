package ru.bprn.printhouse.views.operation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

        private String name;

        // Ссылка на вашу справочную Operation
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "operation_id")
        @ToString.Exclude
        private Operation operation;

        // Порядок выполнения операций
        private Integer sequence;

        @ManyToOne(fetch = FetchType.EAGER) // Выбранный материал для этой операции
        private AbstractMaterials selectedMaterial;

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
                this.name = ""; // Изначально имя пустое, чтобы пользователь задал его в редакторе
                this.sequence = 0; // Default value
                this.switchOff = operation.isSwitchOff();
                this.selectedMaterial = operation.getDefaultMaterial();
                // Выполняем глубокое копирование переменных
                this.customVariables = operation.getVariables().stream()
                        .map(Variable::new) // Используем конструктор копирования Variable
                        .collect(Collectors.toList());
        }

        public ProductOperation(ProductOperation original) {
                this.operation = original.getOperation(); // Ссылка на тот же шаблон операции
                this.name = original.getName();
                this.sequence = original.getSequence();
                this.selectedMaterial = original.getSelectedMaterial();
                this.customVariables = original.getCustomVariables().stream().map(Variable::new).collect(Collectors.toList());
                this.switchOff = original.isSwitchOff();
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
