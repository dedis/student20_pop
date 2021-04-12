package com.github.dedis.student20_pop.utility.json;

import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CloseRollCall;
import com.google.gson.*;
import java.lang.reflect.Type;

/** Json serializer and deserializer for the CreateRollCall message */
public class JsonCloseRollCallSerializer
        implements JsonSerializer<CloseRollCall>, JsonDeserializer<CloseRollCall> {


    private final Gson internalGson = new Gson();

    @Override
    public CloseRollCall deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        CloseRollCall temp = internalGson.fromJson(json, CloseRollCall.class);

        return new CloseRollCall(
                temp.getUpdateId(),
                temp.getCloses(),
                temp.getClosedAt(),
                temp.getAttendees());
    }

    @Override
    public JsonElement serialize(
            CloseRollCall src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = internalGson.toJsonTree(src, CloseRollCall.class).getAsJsonObject();
        return object;
    }
}