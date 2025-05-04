package com.example.demo.service;

import com.example.demo.builder.CardBuilder;
import com.example.demo.builder.DealerBuilder;
import com.example.demo.builder.GameBuilder;
import com.example.demo.builder.HandBuilder;
import com.example.demo.dto.game.GameDTO;
import com.example.demo.entity.Card;
import com.example.demo.entity.Dealer;
import com.example.demo.entity.Game;
import com.example.demo.entity.Hand;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final CardRepository cardRepository;
    private final List<String> suits = Arrays.asList("hearts", "diamonds", "clubs", "spades");
    private final List<String> ranks = Arrays.asList(
            "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "jack", "queen", "king", "ace"
    );

    private final Random random = new Random();

    public ResponseEntity<?> startGame(GameDTO gameDTO) {
        if (gameDTO == null || gameDTO.getUserId() == null || gameDTO.getBetValue() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid game request");
        }

        Hand playerHand = HandBuilder.generateEmptyHand();
        Hand dealerHand = HandBuilder.generateEmptyHand();
        Dealer dealer = DealerBuilder.generateDealer(dealerHand);

        Game game = GameBuilder.generateNewGame(gameDTO.getUserId(), playerHand, dealer, gameDTO.getBetValue());

        dealInitialCards(playerHand, dealerHand);

        Game savedGame = gameRepository.save(game);
        GameDTO savedDTO = GameBuilder.generateDTOFromEntity(savedGame);

        return ResponseEntity.ok(savedDTO);
    }

    public ResponseEntity<?> hit(Long gameId) {
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if (optionalGame.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        Game game = optionalGame.get();
        if (!game.getStatus().equals("IN_PROGRESS")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game is not in progress");
        }

        Card newCard = drawCard();


        newCard.setHand(game.getPlayerHand());
        game.getPlayerHand().getCards().add(newCard);


        cardRepository.save(newCard);

        updateHandStatus(game.getPlayerHand());

        if (game.getPlayerHand().getIsBust().equals(Boolean.TRUE)) {
            game.setStatus("PLAYER_LOST");
        }

        Game savedGame = gameRepository.save(game);
        GameDTO savedDTO = GameBuilder.generateDTOFromEntity(savedGame);
        return ResponseEntity.ok(savedDTO);
    }



    public ResponseEntity<?> stand(Long gameId) {
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if (optionalGame.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        Game game = optionalGame.get();
        if (!game.getStatus().equals("IN_PROGRESS")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game is not in progress");
        }

        Hand dealerHand = game.getDealer().getHand();

        while (dealerHand.getTotalValue() < 17) {
            Card card = drawCard();
            card.setHand(dealerHand);

            cardRepository.save(card);

            dealerHand.getCards().add(card);
            updateHandStatus(dealerHand);
        }

        determineWinner(game);

        adjustBalanceAfterGame(game);

        Game updatedGame = gameRepository.save(game);
        GameDTO updatedDTO = GameBuilder.generateDTOFromEntity(updatedGame);
        return ResponseEntity.ok(updatedDTO);
    }


    public ResponseEntity<?> getGameStatus(Long gameId) {
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if (optionalGame.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        Game game = optionalGame.get();
        GameDTO gameDTO = GameBuilder.generateDTOFromEntity(game);
        return ResponseEntity.ok(gameDTO);
    }


    private void dealInitialCards(Hand playerHand, Hand dealerHand) {
        Card playerCard1 = drawCard();
        Card playerCard2 = drawCard();
        Card dealerCard1 = drawCard();
        Card dealerCard2 = drawCard();

        playerCard1.setHand(playerHand);
        playerCard2.setHand(playerHand);
        dealerCard1.setHand(dealerHand);
        dealerCard2.setHand(dealerHand);

        playerHand.getCards().add(playerCard1);
        playerHand.getCards().add(playerCard2);
        dealerHand.getCards().add(dealerCard1);
        dealerHand.getCards().add(dealerCard2);

        updateHandStatus(playerHand);
        updateHandStatus(dealerHand);
    }

    private Card drawCard() {
        String suit = suits.get(random.nextInt(suits.size()));
        String rank = ranks.get(random.nextInt(ranks.size()));
        int value = calculateCardValue(rank);
        return CardBuilder.generateCard(suit, rank, value);
    }

    private int calculateCardValue(String rank) {
        switch (rank) {
            case "2": return 2;
            case "3": return 3;
            case "4": return 4;
            case "5": return 5;
            case "6": return 6;
            case "7": return 7;
            case "8": return 8;
            case "9": return 9;
            case "10":
            case "jack":
            case "queen":
            case "king": return 10;
            case "ace": return 11;
            default: throw new IllegalArgumentException("Invalid card rank: " + rank);
        }
    }

    private void updateHandStatus(Hand hand) {
        List<Card> cards = hand.getCards();
        int totalValue = 0;
        int aceCount = 0;

        for (Card card : cards) {
            if ("ace".equals(card.getRank())) {
                aceCount++;
                totalValue += 11;
            } else {
                totalValue += card.getValue();
            }
        }

        while (totalValue > 21 && aceCount > 0) {
            totalValue -= 10;
            aceCount--;
        }

        hand.setTotalValue(totalValue);

        boolean bust = totalValue > 21;
        boolean blackjack = cards.size() == 2 && totalValue == 21;

        hand.setIsBust(bust);
        hand.setIsBlackjack(blackjack);
    }


    private void determineWinner(Game game) {
        Hand playerHand = game.getPlayerHand();
        Hand dealerHand = game.getDealer().getHand();

        if (playerHand.getIsBust()) {
            game.setStatus("PLAYER_LOST");
        } else if (dealerHand.getIsBust()) {
            game.setStatus("PLAYER_WON");
        } else {
            int playerValue = playerHand.getTotalValue();
            int dealerValue = dealerHand.getTotalValue();

            if (playerValue > dealerValue) {
                game.setStatus("PLAYER_WON");
            } else if (playerValue < dealerValue) {
                game.setStatus("PLAYER_LOST");
            } else {
                game.setStatus("DRAW");
            }
        }
    }

    private Double adjustBalanceAfterGame(Game game) {
        if (game.getBetValue() == null) {
            return 0.0;
        }

        Double betValue = game.getBetValue();

        if ("PLAYER_WON".equals(game.getStatus())) {
            return betValue * 2;
        }

        if ("PLAYER_LOST".equals(game.getStatus())) {
            return -betValue;
        }

        if ("DRAW".equals(game.getStatus())) {
            return 0.0;
        }

        return 0.0;
    }

    public ResponseEntity<?> getHistoryByUserId(Long userId) {
        List<Game> games = gameRepository.findByUserId(userId);

        if (games.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No game history found for user");
        }

        List<GameDTO> gameDTOs = games.stream()
                .map(GameBuilder::generateDTOFromEntity)
                .toList();

        return ResponseEntity.ok(gameDTOs);
    }

}
