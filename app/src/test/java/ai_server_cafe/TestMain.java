package ai_server_cafe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestMain {
    @Test void appHasAGreeting() {
        Main classUnderTest = new Main();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
