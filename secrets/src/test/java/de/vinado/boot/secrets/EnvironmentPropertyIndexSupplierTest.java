package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static de.vinado.boot.secrets.TestUtils.fileUriFromClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class EnvironmentPropertyIndexSupplierTest {

    private ConfigurableEnvironment environment;
    private Map<String, String> index;

    @BeforeEach
    void setUp() {
        environment = spy(new StandardEnvironment());
    }

    // CHECKSTYLE.OFF: LineLength
    @Test
    void initializingNullArguments_shouldThrowException() {
        DeferredLogFactory logFactory = Supplier::get;

        assertThrows(IllegalArgumentException.class, () -> new EnvironmentPropertyIndexSupplier(logFactory, null));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentPropertyIndexSupplier(null, environment));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentPropertyIndexSupplier(null, null));

        assertThrows(IllegalArgumentException.class, () -> new EnvironmentPropertyIndexSupplier(logFactory, null, null));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentPropertyIndexSupplier(null, environment, null));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentPropertyIndexSupplier(null, null, "_FILE"));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentPropertyIndexSupplier(null, null, null));
    }
    // CHECKSTYLE.ON: LineLength

    @Test
    void suffixedEnvironmentProperty_shouldBeIndexed() {
        addProperty("SPRING_DATASOURCE_USERNAME_FILE", "classpath:spring.datasource.username");

        createIndex("_FILE");

        assertEntry("spring.datasource.username", "classpath:spring.datasource.username");
    }

    @Test
    void notSuffixedEnvironmentProperty_shouldNotBeIndexed_whenSuffixIsConfigured() {
        addProperty("SPRING_DATASOURCE_PASSWORD", fileUriFromClasspath("spring_datasource_password"));

        createIndex("_FILE");

        assertFalse(index.containsKey("spring.datasource.filename"));
    }

    @Test
    void suffixedEnvironmentProperty_shouldBeIndexed_whenSuffixIsNotConfigured() {
        addProperty("SPRING_MAIL_HOST_FILE", "classpath:spring_mail_host");

        createIndex("");

        assertEntry("spring.mail.host.file", "classpath:spring_mail_host");
    }

    @Test
    void nullProperty_shouldNotBeIndexed() {
        addProperty("NULL_VARIABLE_FILE", null);

        createIndex("_FILE");

        assertFalse(index.containsKey("null.variable"));
    }

    @Test
    void emptyProperty_shouldNotBeIndexed() {
        addProperty("EMPTY_SECRET_FILE", "");

        createIndex("_FILE");

        assertFalse(index.containsKey("empty.secret"));
    }

    @Test
    void configuredSuffix_shouldIndexNothing() {
        environment = spy(new TestEnvironment());

        createIndex("_FILE");

        assertNotNull(index);
        assertEquals(0, index.size());
    }

    @Test
    void emptySuffix_shouldIndexEverything() {
        createIndex("");

        assertNotNull(index);
        assertTrue(index.size() > 0);
    }

    private void addProperty(String key, String value) {
        Map<String, Object> systemProperties = new HashMap<>(environment.getSystemEnvironment());
        systemProperties.put(key, value);
        when(environment.getSystemEnvironment()).thenReturn(systemProperties);
        when(environment.getProperty(key)).thenReturn(value);
    }

    private void createIndex(String suffix) {
        EnvironmentPropertyIndexSupplier supplier =
            new EnvironmentPropertyIndexSupplier(Supplier::get, environment, suffix);
        index = supplier.get();
    }

    private void assertEntry(String key, String value) {
        assertTrue(index.containsKey(key));
        assertEquals(value, index.get(key));
    }


    private static class TestEnvironment extends AbstractEnvironment {
    }
}
