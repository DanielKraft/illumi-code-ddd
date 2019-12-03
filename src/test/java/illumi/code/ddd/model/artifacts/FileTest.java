package illumi.code.ddd.model.artifacts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        final String expected = "~";

        final String result = File.getUMLVisibility(null);

        assertEquals(expected, result);
    }
}