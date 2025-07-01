package ru.bprn.printhouse.views.additionalWorks.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.HasAction;
import ru.bprn.printhouse.data.entity.HasMaterials;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.Material;

import java.util.Set;
import java.util.UUID;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "additional_works_beans")
@EqualsAndHashCode
public class AdditionalWorksBean implements HasAction, HasMaterials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    private String name = "Additional Work";

    @ManyToOne(fetch = FetchType.EAGER)
    private TypeOfWorks typeOfWorks;

    @ManyToOne(fetch = FetchType.EAGER)
    private Formulas actionFormula;

    private boolean haveAction = true;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<AbstractMaterials> listOfMaterials;

    @ManyToOne(fetch = FetchType.EAGER)
    private AbstractMaterials defaultMaterial;

    @ManyToOne(fetch = FetchType.EAGER)
    private Formulas materialFormula;

    private boolean haveMaterial = true;


    @Override
    public Formulas getActionFormula() {
        return actionFormula;
    }

    @Override
    public void setActionFormula(Formulas formula) {
        this.actionFormula = formula;
    }

    @Override
    @JsonIgnore
    public String getActionName() {
        return name;
    }

    @Override
    public boolean haveAction() {
        return haveAction;
    }

    @Override
    public Set<AbstractMaterials> getListOfMaterials() {
        return listOfMaterials;
    }

    @Override
    public AbstractMaterials getDefaultMaterial() {
        return defaultMaterial;
    }

    @Override
    public Formulas getMaterialFormula() {
        return materialFormula;
    }

    @Override
    public boolean haveMaterials() {
        return haveMaterial;
    }
}
