package com.bcadaval.esloveno.rest.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FieldSchemaDTO {
    private String name;
    private String label;
    private String inputType;
    private String placeholder;
    private List<OptionDTO> options;
}

