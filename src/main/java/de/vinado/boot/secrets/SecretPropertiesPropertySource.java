package de.vinado.boot.secrets;

import lombok.NonNull;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link MapPropertySource} containing secret properties.
 *
 * @author Vincent Nadoll
 */
public class SecretPropertiesPropertySource extends MapPropertySource {

    public static final String NAME = "secretProperties";

    public SecretPropertiesPropertySource(Map<String, Object> source) {
        super(NAME, source);
    }

    /**
     * Merges the given source with existing <em>secretProperties</em> and adds them to the end of sources.
     *
     * @param source  the map to be merged
     * @param sources the collection of property sources to add the source to
     */
    public static void merge(Map<String, Object> source, @NonNull MutablePropertySources sources) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }

        Map<String, Object> resultingSource = new HashMap<>();
        SecretPropertiesPropertySource propertySource = new SecretPropertiesPropertySource(resultingSource);
        if (sources.contains(NAME)) {
            mergeIfPossible(source, sources, resultingSource);
            sources.replace(NAME, propertySource);
        } else {
            resultingSource.putAll(source);
            sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource);
        }
    }

    @SuppressWarnings("unchecked")
    private static void mergeIfPossible(Map<String, Object> source, MutablePropertySources sources,
                                        Map<String, Object> resultingSource) {
        PropertySource<?> existingSource = sources.get(NAME);
        Object underlyingSource = existingSource.getSource();
        if (underlyingSource instanceof Map) {
            resultingSource.putAll((Map<String, Object>) underlyingSource);
        }
        resultingSource.putAll(source);
    }
}
