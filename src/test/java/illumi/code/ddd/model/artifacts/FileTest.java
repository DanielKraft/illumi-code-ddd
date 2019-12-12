package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

@SuppressWarnings("CheckStyle")
class FileTest {

  @Test
  void getUMLVisibilityOfPublic() {
    final String expected = "+";

    final String result = File.getUMLVisibility("public");

    assertEquals(expected, result);
  }

  @Test
  void getUMLVisibilityOfPrivate() {
    final String expected = "-";

    final String result = File.getUMLVisibility("private");

    assertEquals(expected, result);
  }

  @Test
  void getUMLVisibilityOfProtected() {
    final String expected = "#";

    final String result = File.getUMLVisibility("protected");

    assertEquals(expected, result);
  }

  @Test
  void getUMLVisibilityOfPackagePublic() {
    final String expected = "~";

    final String result = File.getUMLVisibility("");

    assertEquals(expected, result);
  }

  @Test
  void getUMLVisibilityOfNull() {
    final String result = File.getUMLVisibility(null);

    assertNull(result);
  }
}