package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "size_of_print_leaf")
public class SizeOfPrintLeaf{

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @PositiveOrZero
    private int length;

    @NotNull
    @PositiveOrZero
    private int width;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "sizeOfPrintLeaves")
    private Set<PrintMashine> printMashineSet = new HashSet<>();

    private void addToPrintMashineSet(PrintMashine pMashibe){
        this.printMashineSet.add(pMashibe);
        pMashibe.getSizeOfPrintLeaves().add(this);
    }

    private void removeFromPrintMashineSet(PrintMashine pMashibe){
        this.printMashineSet.remove(pMashibe);
        pMashibe.getSizeOfPrintLeaves().remove(this);
    }

}