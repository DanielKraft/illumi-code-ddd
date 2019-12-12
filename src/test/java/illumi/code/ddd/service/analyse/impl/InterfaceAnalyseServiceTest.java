package illumi.code.ddd.service.analyse.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Interface;

import org.junit.jupiter.api.Test;

class InterfaceAnalyseServiceTest {

  @Test
  @SuppressWarnings("CheckStyle")
  void testSetTypeOfJPA() {
    Interface item = new Interface("JpaTest", "de.test.JpaTest");

    InterfaceAnalyseService service = new InterfaceAnalyseService(item);
    service.setType();

    assertEquals(DDDType.INFRASTRUCTURE, item.getType());
  }

  @Test
  @SuppressWarnings("CheckStyle")
  void testSetTypeOfCRUD() {
    Interface item = new Interface("CrudTest", "de.test.CrudTest");

    InterfaceAnalyseService service = new InterfaceAnalyseService(item);
    service.setType();

    assertEquals(DDDType.INFRASTRUCTURE, item.getType());
  }

  @Test
  void testSetTypeWithUnknownType() {
    Interface item = new Interface("Test", "de.test.Test");

    InterfaceAnalyseService service = new InterfaceAnalyseService(item);
    service.setType();

    assertEquals(DDDType.SERVICE, item.getType());
  }
}
