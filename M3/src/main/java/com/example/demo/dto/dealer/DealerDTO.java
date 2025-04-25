package com.example.demo.dto.dealer;

import com.example.demo.dto.hand.HandDTO;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerDTO {
    private Long id;
    private HandDTO hand;
}
