package ru.bprn.printhouse.views.templates.entity;

import com.vaadin.flow.component.icon.VaadinIcon;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import ru.bprn.printhouse.annotation.MenuItem;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@MenuItem(name = "Печать на холсте", icon = VaadinIcon.PRINT, context = "template", description = "")
public class Templates{

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    protected Long id;

    @NotBlank
    protected String name = "Введите название";

    private String description = "Описание изделия";

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Variable> variables = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "templates_product",
            joinColumns = @JoinColumn(name = "templates_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<AbstractProductType> productTypes = new HashSet<>();

    public void initializeVariables(TemplateVariableService variableService) {
        this.setVariables(variableService.getVariablesFor(this.getClass()));
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Templates workFlow = (Templates) o;
        return getId() != null && Objects.equals(getId(), workFlow.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {return "шаблон "+this.name;}
}