package projektzespolowy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projektzespolowy.models.Card;
import projektzespolowy.repository.CardRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {
    private CardRepository cardRepository;
    @Autowired
    public CardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public List<Card> getAllCards() {
        List<Card> karty = cardRepository.findAll();
        Card kartaToDo = cardRepository.findByName("To do").orElseGet(() -> cardRepository.save(new Card("To do", Integer.MAX_VALUE, 0)));
        Card kartaDone = cardRepository.findByName("Done").orElseGet(() -> cardRepository.save(new Card("Done", Integer.MAX_VALUE, 1)));

        List<Card> pomocniczaLista = new ArrayList<>();
        pomocniczaLista.add(kartaToDo);
        for (Card card : karty) {
            if (!card.getName().equals("To do") && !card.getName().equals("Done")) {
                pomocniczaLista.add(card);
            }
        }
        pomocniczaLista.add(kartaDone);


        pomocniczaLista.sort(Comparator.comparingInt(Card::getPosition));

        return pomocniczaLista;
    }

}
