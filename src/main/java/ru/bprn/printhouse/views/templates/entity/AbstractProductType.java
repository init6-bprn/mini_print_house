package ru.bprn.printhouse.views.templates.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;

import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "product_type", length = 100)
@Getter
@Setter
public class AbstractProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    protected UUID id;

    @NotBlank
    protected String name = "Название изделия";

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Variable> variables = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sequence")
    private List<ProductOperation> productOperations = new ArrayList<>();

    /**
     * Инициализирует список переменных для данного типа продукта, используя сервис.
     * Этот метод должен вызываться сразу после создания экземпляра.
     */
    public void initializeVariables(ProductTypeVariableService variableService) {
        this.setVariables(variableService.getVariablesFor(this.getClass()));
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AbstractProductType that = (AbstractProductType) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
