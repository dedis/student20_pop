package com.github.dedis.student20_pop.utility.json;

import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.OpenRollCall;
import com.google.gson.*;
import java.lang.reflect.Type;

/** Json serializer and deserializer for the CreateRollCall message */
public class JsonOpenRollCallSerializer
        implements JsonSerializer<OpenRollCall>, JsonDeserializer<OpenRollCall> {


    private final Gson internalGson = new Gson();

    @Override
    public OpenRollCall deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        OpenRollCall temp = internalGson.fromJson(json, OpenRollCall.class);

        return new OpenRollCall(
                temp.getUpdateId(),
                temp.getOpens(),
                temp.getOpenedAt(),
                temp.getAction());
    }

    @Override
    public JsonElement serialize(
            OpenRollCall src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = internalGson.toJsonTree(src, OpenRollCall.class).getAsJsonObject();
        return object;
    }
}