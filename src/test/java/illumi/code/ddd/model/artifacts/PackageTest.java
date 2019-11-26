package illumi.code.ddd.model.artifacts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PackageTest {
    private Package module;

    @BeforeEach
    void initialize() {
        module = new Package("domain", "de.test.domain");

    }

    @Test
    void testAddContains() {
        module.addContains(new Class("Class", "de.test.domain.Class"));

        assertEquals(1, module.getContains().size());
    }

    @Test
    void testAddExistingContains() {
        Class c = new Class("Class", "de.test.domain.Class");
        module.addContains(c);
        module.addContains(c);

        assertEquals(1, module.getContains().size());
    }
}
