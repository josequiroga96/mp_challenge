package com.mp.challenge.components.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mp.challenge.models.Item;
import com.mp.challenge.models.collections.ItemCollection;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * ItemCollectionSerializer
 * <p>
 * Custom JSON serializer for ItemCollection that maintains the correct JSON structure
 * with items as an object where keys are UUIDs and values are Item objects.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 18/10/2025
 */
public class ItemCollectionSerializer extends JsonSerializer<ItemCollection> {

    @Override
    public void serialize(ItemCollection itemCollection, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("items");
        jsonGenerator.writeStartObject();
        
        Map<UUID, Item> items = itemCollection.getItemsMap();
        for (Map.Entry<UUID, Item> entry : items.entrySet()) {
            jsonGenerator.writeFieldName(entry.getKey().toString());
            jsonGenerator.writeObject(entry.getValue());
        }
        
        jsonGenerator.writeEndObject();
        jsonGenerator.writeEndObject();
    }
}
