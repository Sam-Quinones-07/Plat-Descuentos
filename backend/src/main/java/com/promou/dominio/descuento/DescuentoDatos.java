package com.promou.dominio.descuento;

import java.time.LocalDate;

/** Datos de entrada de un descuento; los nombres coinciden con el tipo Discount del front. */
public record DescuentoDatos(String benefit, String conditions, LocalDate startDate, LocalDate endDate,
                             String code, String location, String website) {
}
