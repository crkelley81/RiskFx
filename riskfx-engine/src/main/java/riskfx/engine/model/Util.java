package riskfx.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class Util {

	public static String toId(final String name) {
		return name.toLowerCase().replace(" ", "-");
	}

	public static void autoAssignAll(final Map map, final List<Player> turnOrder) {
		final List<Territory> territories = new ArrayList<>(map.territories());
		Collections.shuffle(territories);
		
		for (int i = 0; i < territories.size(); i++) {
			final Player p = turnOrder.get(i % turnOrder.size());
			territories.get(i).setOwner(p);
		}
		
	}

	public static void autoPlaceAll(Map map, List<Player> turnOrder, int troopsToDeploy) {
		for (Player p : turnOrder) {
			List<Territory> t = map.territories().stream().filter(t2 -> t2.isOwnedBy(p)).collect(Collectors.toList());
			new Random().ints(0, t.size())
				.limit(troopsToDeploy)
				.mapToObj(t::get)
				.forEach(terr -> terr.setArmies(terr.getArmies() + 1));
		}
	}

}
