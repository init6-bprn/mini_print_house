package ru.bprn.printhouse.views.material.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.PrintMashine;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class PrintingMaterials extends AbstractMaterials{

    private Double wideOfOneClick = 0d;

    @ManyToOne(fetch = FetchType.EAGER)
    private PrintMashine printMashine;

    @Override
    public String toString(){return name;}

    @PrePersist
    @PreUpdate
    private void initSearchStr() {
        this.searchStr = this.name+", ЦПМ (принтер): "+ printMashine.getName();
    }

}
