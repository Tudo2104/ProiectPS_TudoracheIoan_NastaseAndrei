package com.example.demo.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String suit;

    @Column(name = "card_rank", nullable = false)
    private String rank;


    @Column(nullable = false)
    private Integer value;

    @ManyToOne
    @JoinColumn(name = "hand_id")
    @JsonIgnore
    private Hand hand;

}
