package game.CardGame.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
public class CardController {
    @GetMapping("/drawHand")
    public ArrayList<Integer> drawHand() {
        Random rand = new Random();
        ArrayList<Integer> cardList = new ArrayList<Integer>();
        for(int i = 0; i < 5; i++) {
            cardList.add(rand.nextInt(51));
        }
        return cardList;
    }
}
