package riskfx.engine;

import riskfx.engine.ai.DoesNothingAi;
import riskfx.engine.game.GamePlayer;

public enum StandardPlayerTypes implements PlayerType {
	NONE() {
		@Override
		public String getDisplayName() {
			return "None";
		}

		@Override
		public boolean isHuman() {
			return false;
		}

		@Override
		public boolean isComputer() {
			return false;
		}

		@Override
		public boolean isNone() {
			return true;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public GamePlayer newGamePlayerWithName(String name) {
			// TODO Auto-generated method stub
			return null;
		}
	}, DOES_NOTHING_AI() {

		@Override
		public String getDisplayName() {
			return "Does Nothing AI";
		}

		@Override
		public boolean isHuman() {
			return false;
		}

		@Override
		public boolean isComputer() {
			return true;
		}

		@Override
		public boolean isNone() {
			return false;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public GamePlayer newGamePlayerWithName(String name) {
			return new DoesNothingAi();
		}
		
	}, 
	
	HUMAN() {

		@Override
		public String getDisplayName() {
			return "Human";
		}

		@Override
		public boolean isHuman() {
			return true;
		}

		@Override
		public boolean isComputer() {
			return false;
		}

		@Override
		public boolean isNone() {
			return false;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public GamePlayer newGamePlayerWithName(String name) {
			// TODO Auto-generated method stub
			return null;
		}
		}
	; 


}