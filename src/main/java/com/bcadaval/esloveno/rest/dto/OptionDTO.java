package com.bcadaval.esloveno.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionDTO {
    private String value;
    private String label;
}

