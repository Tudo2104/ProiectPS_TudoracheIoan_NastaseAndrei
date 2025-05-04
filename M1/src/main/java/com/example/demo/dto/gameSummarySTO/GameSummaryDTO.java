package com.example.demo.dto.gameSummarySTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameSummaryDTO {
    private Long id;
    private Double betValue;
    private String status;
}

