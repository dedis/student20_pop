package com.github.dedis.student20_pop.model.network.method.message;

import android.util.Base64;
import android.util.Log;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.model.network.method.message.data.message.WitnessMessage;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.subtle.Ed25519Verify;
import com.google.gson.Gson;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Container of a high level message.
 *
 * <p>It is encapsulated inside low level messages
 */
public final class MessageGeneral {

  private final String TAG = MessageGeneral.class.getSimpleName();

  private final byte[] sender;

  private final byte[] dataBuf;

  private Data data;

  private byte[] signature;

  private byte[] messageId;

  private List<PublicKeySignaturePair> witnessSignatures;

  private PublicKeyVerify verifier;

  public MessageGeneral(byte[] sender, Data data, PublicKeySign signer, Gson gson) {
    this.sender = sender;
    this.data = data;
    Log.d(TAG, gson.toJson(data, Data.class));
    this.dataBuf = gson.toJson(data, Data.class).getBytes();
    this.verifier = new Ed25519Verify(sender);

    generateSignature(signer);
    generateId();
  }

  public MessageGeneral(
      byte[] sender,
      Data data,
      List<PublicKeySignaturePair> witnessSignatures,
      PublicKeySign signer,
      Gson gson) {
    this(sender, data, signer, gson);
    this.witnessSignatures = witnessSignatures;
  }

  public MessageGeneral(
      byte[] sender,
      byte[] dataBuf,
      Data data,
      byte[] signature,
      byte[] messageId,
      List<PublicKeySignaturePair> witnessSignatures) {
    this.sender = sender;
    this.messageId = messageId;
    this.dataBuf = dataBuf;
    this.signature = signature;
    this.witnessSignatures = witnessSignatures;
    this.data = data;
    this.verifier = new Ed25519Verify(sender);
  }

  private void generateSignature(PublicKeySign signer) {
    try {
      this.signature = signer.sign(this.dataBuf);
    } catch (GeneralSecurityException e) {
      Log.d(TAG, "failed to generate signature", e);
    }
  }

  private void generateId() {
    try {
      MessageDigest hasher = MessageDigest.getInstance("SHA-256");

      hasher.update(this.dataBuf);
      hasher.update(this.signature);

      this.messageId = hasher.digest();
    } catch (NoSuchAlgorithmException e) {
      Log.d(TAG, "failed to generate id", e);
    }
  }

  public String getMessageId() {
    return Base64.encodeToString(this.messageId, Base64.NO_WRAP);
  }

  public String getSender() {
    return Base64.encodeToString(this.sender, Base64.NO_WRAP);
  }

  public String getSignature() {
    return Base64.encodeToString(this.signature, Base64.NO_WRAP);
  }

  public List<PublicKeySignaturePair> getWitnessSignatures() {
    return this.witnessSignatures;
  }

  public Data getData() {
    return data;
  }

  public String getDataEncoded() {
    return Base64.encodeToString(this.dataBuf, Base64.NO_WRAP);
  }

  public boolean verify() {
    try {
      verifier.verify(signature, dataBuf);

      if (data instanceof WitnessMessage) {
        WitnessMessage witnessMessage = (WitnessMessage) data;

        byte[] signatureBuf = Base64.decode(witnessMessage.getSignature(), Base64.NO_WRAP);
        byte[] messageIdBuf = Base64.decode(witnessMessage.getMessageId(), Base64.NO_WRAP);

        verifier.verify(signatureBuf, messageIdBuf);
      }

      return true;
    } catch (GeneralSecurityException e) {
      Log.d(TAG, "failed to verify signature", e);
      return false;
    }
  }
}
