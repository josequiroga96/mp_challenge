package com.mp.challenge.components.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mp.challenge.models.Item;
import com.mp.challenge.models.collections.ItemCollection;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ItemCollectionDeserializer
 * <p>
 * Custom JSON deserializer for ItemCollection that reads the correct JSON structure
 * with items as an object where keys are UUIDs and values are Item objects.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 18/10/2025
 */
public class ItemCollectionDeserializer extends JsonDeserializer<ItemCollection> {

    @Override
    public ItemCollection deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
        JsonNode itemsNode = rootNode.get("items");
        
        if (itemsNode == null || !itemsNode.isObject()) {
            return new ItemCollection();
        }
        
        Map<UUID, Item> items = new HashMap<>();
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        
        itemsNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = itemsNode.get(fieldName);
            try {
                UUID id = UUID.fromString(fieldName);
                Item item = objectMapper.treeToValue(fieldValue, Item.class);
                items.put(id, item);
            } catch (IllegalArgumentException | JsonProcessingException e) {
                // Skip invalid UUID keys or items that can't be deserialized
            }
        });
        
        ItemCollection collection = new ItemCollection();
        items.values().forEach(collection::addItem);
        return collection;
    }
}
