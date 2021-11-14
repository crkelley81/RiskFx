package riskfx.ui;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javafx.css.PseudoClass;

class TerritorySkinTest {

	@Test
	void selectSetsPseudoClass() {
		// GIVEN 
		final TerritorySkin skin = new TerritorySkin("test", "Test");
		
		// WHEN
		skin.select();
		
		// THEN 
		Assertions.assertThat(skin.getPseudoClassStates()).contains(PseudoClass.getPseudoClass("selected"));
		
		// WHEN 
		skin.deselect();
		
		Assertions.assertThat(skin.getPseudoClassStates()).doesNotContain(PseudoClass.getPseudoClass("selected"));

	}

}
