package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import illumi.code.ddd.model.DDDType;

public class InterfaceAnalyseTest {
	
	@Test
	public void testSetTypeOfJPA() {
		Interface item = new Interface("JpaTest", "de.test.JpaTest");
		
		item.setType();
		
		assertEquals(DDDType.INFRASTRUCTUR, item.getType());
	}
	
	@Test
	public void testSetTypeOfCRUD() {
		Interface item = new Interface("CrudTest", "de.test.CrudTest");
		
		item.setType();
		
		assertEquals(DDDType.INFRASTRUCTUR, item.getType());
	}
	
	@Test
	public void testSetTypeWithUnknownType() {
		Interface item = new Interface("Test", "de.test.Test");
		
		item.setType();
		
		assertEquals(DDDType.SERVICE, item.getType());
	}
}
