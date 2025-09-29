package ru.bprn.printhouse.views.products.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

@Entity
@Table(name = "price_of_material")
@Getter
@Setter
@NoArgsConstructor
public class PriceOfMaterial extends AbstractPrice {

    @ManyToOne(fetch = FetchType.LAZY)
    private AbstractMaterials material;
}