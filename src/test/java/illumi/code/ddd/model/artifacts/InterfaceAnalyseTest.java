package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import illumi.code.ddd.model.DDDType;

class InterfaceAnalyseTest {
	
	@Test
	void testSetTypeOfJPA() {
		Interface item = new Interface("JpaTest", "de.test.JpaTest");
		
		item.setType();
		
		assertEquals(DDDType.INFRASTRUCTUR, item.getType());
	}
	
	@Test
	void testSetTypeOfCRUD() {
		Interface item = new Interface("CrudTest", "de.test.CrudTest");
		
		item.setType();
		
		assertEquals(DDDType.INFRASTRUCTUR, item.getType());
	}
	
	@Test
	void testSetTypeWithUnknownType() {
		Interface item = new Interface("Test", "de.test.Test");
		
		item.setType();
		
		assertEquals(DDDType.SERVICE, item.getType());
	}
}
