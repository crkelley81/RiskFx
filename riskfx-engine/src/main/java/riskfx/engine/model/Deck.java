package riskfx.engine.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public final class Deck {

	public static Builder from(final Map map) {
		return new Builder(map);
	}
	
	private final Deque<Card> deck;
	private final List<Card> discarded;
	
	private Deck(final Stream<Card> stream) {
		final List<Card> cards = stream.collect(Collectors.toList());
		Collections.shuffle(cards);
		this.deck = new ArrayDeque<>(cards);
		this.discarded = new ArrayList<>();
	}
	
	public final Card deal() {
		if (deck.isEmpty()) shuffle();
		return deck.pollFirst();
	}
	
	public final void discard(final Card card) {
		discarded.add(card);
	}
	
	public final void shuffle() {
		final List<Card> cards = new ArrayList<>(deck);
		cards.addAll(discarded);
		Collections.shuffle(cards);
		
		deck.clear();
		discarded.clear();
		
		deck.addAll(cards);
	}
	
	public static class Builder {
		private Map map;
		private long wildcards = 0;
		
		private Builder(final Map map) {
			this.map = map;
		}
		
		public final Deck build() {
			
			Stream<Card> cards = Stream.concat(
					map.territories().stream().map(t -> cardWithRandomType(t)),
					LongStream.range(0, wildcards).map(i -> i + 1).mapToObj(i -> Card.wildcard(i))
					);
			
			Deck deck = new Deck(cards);
			deck.shuffle();
			return deck;
		}
	}

	private static Card cardWithRandomType(Territory t) {
		final Random random = new Random();
		final int index = random.nextInt(0, 3);
		return Card.territoryCard(t, Card.Type.values()[index]);
	}
}
