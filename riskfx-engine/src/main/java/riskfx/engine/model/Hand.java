package riskfx.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 
 * @author christopher
 *
 */
public final class Hand {
	private final List<Card> cards = new ArrayList<>();
	
	private long wildcardCount = 0;
	private long cardTypesCount = 0;
	private long maxNonWildcardCount = 0;
	
	public final void issue(final Card... cards) {
		for (Card c : cards) {
			if (this.cards.contains(c)) throw new IllegalArgumentException();
			this.cards.add(c);
		}
		
		updateCounts();
	}
	
	private void updateCounts() {
		final java.util.Map<Card.Type, Long> cardTypeMaps = this.cards.stream()
				.collect(Collectors.toMap(Card::type, c -> (long) 1, (a, b) -> a + b));
		this.wildcardCount = Optional.ofNullable(cardTypeMaps.get(Card.Type.WILDCARD)).orElse((long) 0);
		this.cardTypesCount = cardTypeMaps.keySet().size();
		
	}
	
	public final boolean canTurnInCards() {
		return cards.size() >= 3 && 
				(
						cardTypesCount >= 3 
						);
	}
	
	public final boolean mustTurnInCards() {
		return cards.size() >= 5;
	}
	
	public final long numberOfCards() {
		return this.cards.size();
	}

	public final List<Card> cards() {
		return Collections.unmodifiableList(cards);
	}
}
