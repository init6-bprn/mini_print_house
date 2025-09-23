package ru.bprn.printhouse.views.machine.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import ru.bprn.printhouse.views.machine.service.MachineVariableService;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;
import ru.bprn.printhouse.views.price.entity.PriceOfMachine;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "machine_type")
@Getter
@Setter
@Entity
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrintingMaterials.class, name = "printingMaterials"),
})
public abstract class AbstractMachine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    protected UUID id;

    @NotBlank
    protected String name = "Название устройства";

    protected String searchStr;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "abstractMachines", targetEntity = AbstractMaterials.class)
    private Set<AbstractMaterials> abstractMaterials = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Variable> variables = new LinkedList<>();

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceOfMachine> priceHistory = new ArrayList<>();

    public void initializeVariables(MachineVariableService service) {
        if (this.variables == null || this.variables.isEmpty()) {
            this.variables = service.getVariablesFor(this.getClass()).stream()
                    .map(Variable::new) // Создаем копии, чтобы избежать изменения оригиналов
                    .collect(Collectors.toList());
        }
    }


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AbstractMachine that = (AbstractMachine) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
