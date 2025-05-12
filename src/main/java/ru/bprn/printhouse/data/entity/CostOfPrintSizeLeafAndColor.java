package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cost_of_print_size_leaf_and_color")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
public class CostOfPrintSizeLeafAndColor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne (fetch = FetchType.EAGER)
    private PrintMashine printMashine;

    @ManyToOne (fetch = FetchType.EAGER)
    private SizeOfPrintLeaf sizeOfPrintLeaf;

    @ManyToOne (fetch = FetchType.EAGER)
    private QuantityColors quantityColors;

    //цена в копейках (минимальная единица)
    private int cost;
}
