package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

import java.util.Set;

@Data
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

    @ManyToMany(mappedBy = "sizeOfPrintLeaves")
    private Set<PrintMashine> printMashineSet;

    public SizeOfPrintLeaf() {
    }

}