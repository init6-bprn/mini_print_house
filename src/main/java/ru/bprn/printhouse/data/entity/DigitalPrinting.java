package ru.bprn.printhouse.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DigitalPrinting {

    private Long id;

    private PrintMashine printMashine;

    private QuantityColors quantityColorsCover;

    private QuantityColors quantityColorsBack;

    private ImposeCase imposeCase;
}
