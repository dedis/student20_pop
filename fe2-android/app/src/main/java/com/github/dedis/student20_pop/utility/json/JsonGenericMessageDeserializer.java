package com.github.dedis.student20_pop.utility.json;

import android.util.Log;

import com.github.dedis.student20_pop.model.data.LAORepository;
import com.github.dedis.student20_pop.model.network.GenericMessage;
import com.github.dedis.student20_pop.model.network.answer.Answer;
import com.github.dedis.student20_pop.model.network.method.Message;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/** Json deserializer for the generic messages */
public class JsonGenericMessageDeserializer implements JsonDeserializer<GenericMessage> {

  private static final String METHOD = "method";
  private static final String TAG = JsonGenericMessageDeserializer.class.getSimpleName();

  @Override
  public GenericMessage deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (json.getAsJsonObject().has(METHOD))  {
      Log.d(TAG,"Deserializing generic message with METHOD ");
      return context.deserialize(json, Message.class);
            }
    else {
      Log.d(TAG,"Deserializing generic message without METHOD ");
      return context.deserialize(json, Answer.class);
    }
  }
}
