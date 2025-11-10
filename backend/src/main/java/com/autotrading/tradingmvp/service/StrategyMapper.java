package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.dto.StrategyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class StrategyMapper {

    private final ObjectMapper objectMapper;

    public StrategyMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize strategy parameters", e);
        }
    }

    public JsonNode toNode(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize strategy parameters", e);
        }
    }

    public StrategyResponse toResponse(StrategyDefinition definition) {
        JsonNode params = definition.getParametersJson() == null
                ? objectMapper.createObjectNode()
                : toNode(definition.getParametersJson());
        return new StrategyResponse(
                definition.getId(),
                definition.getName(),
                definition.getDescription(),
                definition.getAccount().getId(),
                definition.getInstrument().getId(),
                definition.getType(),
                definition.getStatus(),
                definition.getCron(),
                definition.getStartAt(),
                definition.getEndAt(),
                definition.getLastRunAt(),
                definition.getCreatedAt(),
                definition.getUpdatedAt(),
                params
        );
    }

    public JsonNode emptyParameters() {
        return objectMapper.createObjectNode();
    }
}
