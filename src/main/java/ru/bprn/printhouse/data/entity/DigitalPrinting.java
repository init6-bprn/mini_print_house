package ru.bprn.printhouse.data.entity;

import lombok.*;

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
