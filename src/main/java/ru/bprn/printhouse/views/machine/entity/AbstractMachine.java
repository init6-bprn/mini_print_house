package ru.bprn.printhouse.views.machine.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "machine_type")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    @PositiveOrZero
    protected Integer price = 0;

    protected String searchStr;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "abstractMachines", targetEntity = AbstractMaterials.class)
    private Set<AbstractMaterials> abstractMaterials = new HashSet<>();

}
