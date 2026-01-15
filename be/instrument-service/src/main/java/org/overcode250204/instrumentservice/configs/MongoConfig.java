package org.overcode250204.instrumentservice.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        Converter<JsonNode, Document> jsonNodeToDocument = new Converter<>() {
            @Override
            public Document convert(JsonNode source) {
                return Document.parse(source.toString());
            }
        };

        Converter<Document, JsonNode> documentToJsonNode = new Converter<>() {
            @Override
            public JsonNode convert(Document source) {
                return objectMapper.convertValue(source, JsonNode.class);
            }
        };

        return new MongoCustomConversions(List.of(jsonNodeToDocument, documentToJsonNode));
    }
}