package ru.bprn.printhouse.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode @ToString @NoArgsConstructor
@Entity
@Table(name = "size_of_print_leaf")
public class SizeOfPrintLeaf extends AbstractEntity {

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @PositiveOrZero
    private int length;

    @NotNull
    @PositiveOrZero
    private int width;

    //@ManyToMany(fetch = FetchType.EAGER)
    //@Fetch(FetchMode.JOIN)
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "sizeOfPrintLeaves")
    private Set<PrintMashine> printMashineSet = new HashSet<>();

    private void addPrintMashine(PrintMashine pMashibe){
        this.printMashineSet.add(pMashibe);
        pMashibe.getSizeOfPrintLeaves().add(this);
    }

    private void removePrintMashine(PrintMashine pMashibe){
        this.printMashineSet.remove(pMashibe);
        pMashibe.getSizeOfPrintLeaves().remove(this);
    }

}