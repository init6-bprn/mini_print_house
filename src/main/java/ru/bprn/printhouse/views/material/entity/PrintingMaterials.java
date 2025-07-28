package ru.bprn.printhouse.views.material.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;

import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class PrintingMaterials extends AbstractMaterials{

    @Positive
    private Integer sizeOfClick = 1;

    @Override
    public String toString(){return name;}

    @PrePersist
    @PreUpdate
    private void initSearchStr() {
        String s = "";
        if (abstractMachines!=null)
            s = abstractMachines.stream().map(AbstractMachine::getName).collect(Collectors.joining());
        this.searchStr = this.name+", ЦПМ (принтер): "+ s;
    }

}
