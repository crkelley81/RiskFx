package riskfx.engine.network;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.Optional;

import riskfx.engine.game.GameState;
import riskfx.engine.model.Territory;
import riskfx.util.role.Identifiable;

class GameData implements Externalizable {

	private static enum Types {
		TERRITORY(Territory.class);

		private Class<Territory> type;

		Types(Class<Territory> type) {
			this.type = Objects.requireNonNull(type);
		}

		Object resolveIn(String id, GameState state) {
			return state.lookupTerritory(id);
		}
	}
	
	public static boolean canSerialize(Object obj) {
		if (obj == null) return false;
		return resolveType(Objects.requireNonNull(obj).getClass()).isPresent();
	}

	static Optional<Types> resolveType(final Class<?> clazz) {
		for (Types t : Types.values()) {
			if (Objects.equals(t.type, clazz)) {
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}
	
	private Types type;
	private String id;
	
	public GameData(Object obj) {
		this.type = resolveType(Objects.requireNonNull(obj).getClass()).get();
		this.id = ((Identifiable) obj).getId();
	}
	
	public GameData() {
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(type.ordinal());
		out.writeUTF(id);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = Types.values()[in.readInt()];
		id = in.readUTF();
	}

	public Object resolveIn(GameState state) {
		return type.resolveIn(id, state);
	}

	

}
