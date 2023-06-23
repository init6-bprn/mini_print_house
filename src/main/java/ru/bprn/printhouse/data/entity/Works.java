package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.bprn.printhouse.data.AbstractWork;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@DiscriminatorValue("P")
@Table(name = "works_for_print")
public class Works extends AbstractWork {

    private Boolean duplex = false;

    private Double priceOfOneOperation = 0d;

    private Integer totalQuantity = 1;

    @ManyToOne
    @JoinColumn(name = "quantity_colors_id")
    private  QuantityColors quantityColors;

    @ManyToOne
    @JoinColumn(name = "print_mashine_id")
    private PrintMashine printMashine;

    private Integer column = 1;

    private Integer row = 1;

    @Override
    public Integer calculateTime() {
        return null;
    }

    @Override
    public Double calculateCost() {
        return null;
    }
}