package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Table(name = "workflow")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = WorkFlow.class)
public class WorkFlow {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    private String name = "auto";

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "standart_size", nullable = false)
    private StandartSize standartSize;

    @PositiveOrZero
    private Double sizeX = 0d;

    @PositiveOrZero
    private Double sizeY = 0d;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "material", nullable = false)
    private Material material;

    @Positive
    private Integer quantityOfPrintLeaves = 1;

    @Positive
    private  int quantityOfLeaves = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bleed", nullable = false)
    private Gap bleed;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gap", nullable = false)
    private Gap gap;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "impose_case", nullable = false)
    private ImposeCase imposeCase;

    @Column(columnDefinition = "mediumtext")
    private String strJSON = "";

    @PositiveOrZero
    private Integer quantityOfProduct = 0;

    @Positive
    private int listRows = 1;

    @Positive
    private int listColumns = 1;

    @Positive
    private int quantityProductionsOnLeaf = 1;

    @NotBlank
    private String orientation = "Автоматически";



    public void setName(String name) {
        if (name.equals("auto")) {
            if ((this.getStandartSize() != null) & (this.getMaterial() != null) & (this.getGap() != null)) {
                this.name = this.getStandartSize().getName() + " - " + this.getMaterial().getName() + " - " +
                        this.getMaterial().getThickness().toString() + "г. - " + this.getMaterial().getSizeOfPrintLeaf() +
                        " - " + this.getGap().getName();
            }
            else this.name = name;
        }
        else this.name = name;
    }
}