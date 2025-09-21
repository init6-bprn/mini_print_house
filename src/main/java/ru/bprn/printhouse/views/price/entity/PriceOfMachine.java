package ru.bprn.printhouse.views.price.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;

@Entity
@Table(name = "price_of_machine")
@Getter
@Setter
@NoArgsConstructor
public class PriceOfMachine extends AbstractPrice {

    @ManyToOne(fetch = FetchType.LAZY)
    private AbstractMachine machine;
}