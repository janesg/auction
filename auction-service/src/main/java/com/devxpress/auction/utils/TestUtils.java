package com.devxpress.auction.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hamcrest.Matcher;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static org.hamcrest.Matchers.hasProperty;

public final class TestUtils {

    private TestUtils() {}

    public static String mapToJson(Object obj) throws JsonProcessingException {
        return getDateTimeAwareObjectMapper().writeValueAsString(obj);
    }

    public static <T> T mapFromJson(String json, Class<T> clazz) throws IOException {
        return getDateTimeAwareObjectMapper().readValue(json, clazz);
    }

    // Required for java.time serialization to JSON
    public static MappingJackson2HttpMessageConverter getJacksonDateTimeConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(getDateTimeAwareObjectMapper());
        return converter;
    }

    public static <T> Matcher<T> hasGraph(String graphPath, Matcher<T> matcher) {

        List<String> properties = Arrays.asList(graphPath.split("\\."));
        ListIterator<String> iterator =
                properties.listIterator(properties.size());

        Matcher<T> ret = matcher;
        while (iterator.hasPrevious()) {
            ret = hasProperty(iterator.previous(), ret);
        }

        return ret;
    }

    private static ObjectMapper getDateTimeAwareObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
