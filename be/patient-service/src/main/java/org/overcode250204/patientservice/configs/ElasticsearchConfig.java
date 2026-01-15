//package org.overcode250204.patientservice.configs;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import lombok.extern.slf4j.Slf4j;
//import org.overcode250204.patientservice.utils.LongToInstantConverterUtils;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.convert.ReadingConverter;
//import com.fasterxml.jackson.core.type.TypeReference;
//import org.springframework.data.convert.WritingConverter;
//import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Map;
//
//@Slf4j
//@Configuration
//public class ElasticsearchConfig {
//
//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        return mapper;
//    }
//
//
//    @Bean
//    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
//        return new ElasticsearchCustomConversions(Arrays.asList(
//                new StringToMapConverter(),
//                new MapToStringConverter(),
//                new LongToInstantConverterUtils()
//        ));
//    }
//
//    @ReadingConverter
//    static class MapToStringConverter implements Converter<Map<String, Object>, String> {
//        private final ObjectMapper mapper = new ObjectMapper();
//        @Override
//        public String convert(Map<String, Object> source) {
//            try {
//                return mapper.writeValueAsString(source);
//            } catch (IOException e) {
//                return null;
//            }
//        }
//    }
//
//    @WritingConverter
//    static class StringToMapConverter implements Converter<String, Map<String, Object>> {
//        private final ObjectMapper mapper = new ObjectMapper();
//        @Override
//        public Map<String, Object> convert(String source) {
//            try {
//                return mapper.readValue(source, new TypeReference<Map<String, Object>>() {});
//            } catch (IOException e) {
//                return null;
//            }
//        }
//    }
//
//
//}
