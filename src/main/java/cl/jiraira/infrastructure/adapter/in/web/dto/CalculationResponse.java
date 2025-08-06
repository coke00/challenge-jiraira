package cl.jiraira.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CalculationResponse {

    @JsonProperty("result")
    private Double result;

    @JsonProperty("percentage")
    private Double percentage;

    @JsonProperty("details")
    private String details;
}
