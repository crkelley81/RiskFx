package riskfx.mapeditor.io.domination;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import riskfx.mapeditor.TestHelper;
import riskfx.mapeditor.model.MapSkin;

class DominationMapImporterTest {
	
	private DominationMapImporter importer = new DominationMapImporter();
	
	@Test
	void canImportBigeurope() throws IOException {
//		System.err.println(bigeuropePath());
		final boolean canImport = importer.canImport(TestHelper.bigeuropePath());
		Assertions.assertTrue(canImport);
	}

	@Test void importBigEurope() throws IOException {
		// GIVEN 
		final DominationMapImporter importer = new DominationMapImporter();
		
		// WHEN 
		final MapSkin skin = importer.importMap(TestHelper.bigeuropePath());
		
		// THEN 
		Assertions.assertNotNull(skin);
		
		final DominationExt ext = skin.as(DominationExt.class).get();
		Assertions.assertEquals("bigeurope_pic.jpg", ext.getPicImageFileName());
		Assertions.assertEquals("bigeurope_map.gif", ext.getMapImageFileName());
		
		Assertions.assertNotNull(skin.getBackgroundImage());
		Assertions.assertEquals(180, skin.territories().size());
		
		assertTerritory(skin, 0, 1, "s1", 66, 28);
		assertTerritory(skin, 93, 94, "t11", 731, 377);
		

		
	}

	public static void assertTerritory(final MapSkin skin, int i,  int index, String string, int j, int k) {
		final TerritorySkin ts = skin.territories().get(i);
		final DominationTerritoryExt ext = ts.as(DominationTerritoryExt.class).get();
		
		Assertions.assertEquals(string, ts.getId());
//		Assertions.assertEquals(string, ts.getName());
		Assertions.assertEquals(index, ext.getColorIndex());
//		Assertions.
	}

}
