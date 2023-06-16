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

    @NotEmpty
    @PositiveOrZero
    private Integer sizeX = 0;

    @NotEmpty
    @PositiveOrZero
    private Integer sizeY = 0;

    @NotEmpty
    @Positive
    private Float bleed = 2f;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "material")
    private Material material;

    @NotEmpty
    @Positive
    private Integer quantity = 1;

    @NotEmpty
    @Positive
    private Integer quantityOfLeaves = 1;

    @OneToMany
    private List<AbstractWork> abstractWorkList = new LinkedList<>();

    @Embedded
    private Gap gap;

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


}