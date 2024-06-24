package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.bprn.printhouse.data.AbstractWork;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Table(name = "template")
public class Template{
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    private String name = "";

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "standart_size")
    private StandartSize standartSize;

    @NotEmpty
    @PositiveOrZero
    private Double sizeX = 0d;

    @NotEmpty
    @PositiveOrZero
    private Double sizeY = 0d;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "material", nullable = false)
    private Material material;

    @NotEmpty
    @Positive
    private Integer quantityOfLeaves = 1;
/*
    @OneToMany
    private List<AbstractWork> abstractWorkList = new LinkedList<>();
*/
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gap", nullable = false)
    private Gap gap;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "impose_case", nullable = false)
    private ImposeCase imposeCase;

/*
    public Integer calculateTime() {
        return 0;

    }

    public Double calculateCost() {
        Double price = 0d;
        for (AbstractWork aw: abstractWorkList) {
            price += aw.calculateCost();
        };
        return price;
    }

    private void setName() {
        if ((material!=null)&(standartSize!=null)&(gap!=null))
             name = standartSize.getName()+" - "+material.getName()+" - "+material.getThickness().toString()+"г. - "+material.getSizeOfPrintLeaf()+" - "+gap.getName();
        else name = "";
    }
*/

}