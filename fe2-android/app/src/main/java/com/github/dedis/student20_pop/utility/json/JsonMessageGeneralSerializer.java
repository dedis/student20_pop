package com.github.dedis.student20_pop.utility.json;


import android.util.Log;

import com.github.dedis.student20_pop.model.network.method.message.MessageGeneral;
import com.github.dedis.student20_pop.model.network.method.message.PublicKeySignaturePair;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.subtle.Ed25519Verify;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

public class JsonMessageGeneralSerializer
        implements JsonSerializer<MessageGeneral>, JsonDeserializer<MessageGeneral> {
  private final String SIG = "signature";
  @Override
  public MessageGeneral deserialize(
          JsonElement json, Type typeOfT, JsonDeserializationContext context)
          throws JsonParseException {
    JsonObject root = json.getAsJsonObject();

    byte[] messageId = root.get("message_id").getAsString().getBytes(StandardCharsets.UTF_8);
    byte[] dataBuf = Base64.getUrlDecoder().decode(root.get("data").getAsString());
    byte[] sender = Base64.getUrlDecoder().decode(root.get("sender").getAsString());
    byte[] signature = Base64.getUrlDecoder().decode(root.get(SIG).getAsString());

    PublicKeyVerify verifier = new Ed25519Verify(sender);
    try {
      verifier.verify(signature, dataBuf);
    } catch (GeneralSecurityException e) {
      throw new JsonParseException("failed to verify signature on data", e);
    }

    List<PublicKeySignaturePair> witnessSignatures = new ArrayList<>();
    JsonArray arr = root.get("witness_signatures").getAsJsonArray();
    Iterator<JsonElement> it = arr.iterator();
    while(it.hasNext()){
      JsonElement element = it.next();
      String witness = element.getAsJsonObject().get("witness").getAsString();
      String sig = element.getAsJsonObject().get(SIG).getAsString();
      witnessSignatures.add(new PublicKeySignaturePair(Base64.getUrlDecoder().decode(witness), Base64.getUrlDecoder().decode(sig)));
    }

    JsonElement dataElement = JsonParser.parseString(new String(dataBuf));
    Data data = context.deserialize(dataElement, Data.class);

    return new MessageGeneral(sender, dataBuf, data, signature, messageId, witnessSignatures);
  }

  @Override
  public JsonElement serialize(
          MessageGeneral src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject result = new JsonObject();

    result.addProperty("message_id", src.getMessageId());
    result.addProperty("sender", src.getSender());
    result.addProperty(SIG, src.getSignature());

    result.addProperty("data", src.getDataEncoded());

    JsonArray jsonArray = new JsonArray();
    for(PublicKeySignaturePair element:src.getWitnessSignatures()){
      JsonObject sigObj = new JsonObject();
      sigObj.addProperty("witness", element.getWitnessEncoded());
      sigObj.addProperty(SIG, element.getSignatureEncoded());
      jsonArray.add(sigObj);
    }
    result.add("witness_signatures", jsonArray);
    Log.d("JSON", result.toString());

    return result;
  }
}
