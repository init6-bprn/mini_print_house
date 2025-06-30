package ru.bprn.printhouse.views.material.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.PrintMashine;

//@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class PrintingMaterials extends AbstractMaterials{

    private float wideOfOneClick = 0f;

    @OneToMany(fetch = FetchType.EAGER)
    private PrintMashine printMashine;

    @Override
    public String toString(){return name;}

}
