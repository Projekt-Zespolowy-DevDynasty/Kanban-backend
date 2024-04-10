package projektzespolowy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RowWithAllCardsDTO {
    private Long id;
    private String name;
    private int position;
    private List<CardDTO> cardsInRow;

    public static RowWithAllCardsDTO from(RowWithAllCards rowWithAllCards) {
        RowWithAllCardsDTO dto = new RowWithAllCardsDTO();
        dto.setId(rowWithAllCards.getId());
        dto.setName(rowWithAllCards.getName());
        dto.setPosition(rowWithAllCards.getPosition());

        if (rowWithAllCards.getCardsinrow() != null) {
            List<CardDTO> cardsInRow = rowWithAllCards.getCardsinrow().stream()
                    .map(CardDTO::from)
                    .collect(Collectors.toList());
            dto.setCardsInRow(cardsInRow);
        }

        return dto;
    }
}
