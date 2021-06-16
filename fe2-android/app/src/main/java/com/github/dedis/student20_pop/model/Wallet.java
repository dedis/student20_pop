package com.github.dedis.student20_pop.model;

import android.content.Context;
import android.util.Log;
import androidx.core.util.Pair;
import com.github.dedis.student20_pop.model.stellar.SLIP10;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AesGcmKeyManager;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.MnemonicValidator;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import net.i2p.crypto.eddsa.Utils;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

/**
 * This class represent a wallet that will enable users to store their PoP tokens with reasonable,
 * realistic security and usability.
 */
public class Wallet {

  private static final String TAG = Wallet.class.getSimpleName();
  private static final int PURPOSE =  888;
  private static final int ACCOUNT =  0;
  private byte[] seed;
  private Aead aead;
  private boolean isSetup = false;

  private static final Wallet instance = new Wallet();

  public static Wallet getInstance() {
    return instance;
  }

  /**
   * Class constructor, initialize the wallet with a new random seed.
   */
  public Wallet() {
    setRandomSeed();
  }

  /**
   * Method to overwrite the seed of the current wallet with a new seed.
   *
   * @param seed
   */
  public void initialize(String seed){
    if (seed == null) {
      throw new IllegalArgumentException("Unable to init seed from a null param!");
    }
    this.seed = Utils.hexToBytes(seed);
    isSetup = true;
    Log.d(TAG, "New seed initialized: " + Utils.bytesToHex(this.seed));
  }
  /**
   * Method to init the AndroidKeysetManager
   *
   * @param applicationContext
   */
  public void initKeysManager(Context applicationContext)
      throws IOException, GeneralSecurityException {
    AesGcmKeyManager.register(true);
    AeadConfig.register();
    AndroidKeysetManager keysetManager =
        new AndroidKeysetManager.Builder()
            .withSharedPref(applicationContext, "POP_KEYSET_2",  "POP_KEYSET_SP_2")
            .withKeyTemplate(AesGcmKeyManager.rawAes256GcmTemplate())
            .withMasterKeyUri("android-keystore://POP_MASTER_KEY_2")
            .build();
    aead = keysetManager.getKeysetHandle().getPrimitive(Aead.class);
  }

  /**
   * Method that allow generate a different key for each path that you give.
   *
   * @param path a String path of the form: m/i/j/k/... where i,j,k,.. are 31-bit integer.
   * @return a Pair<byte[], byte[]> representing the keys pair: first=private_key; second=public_key.
   * @throws GeneralSecurityException
   */
  public Pair<byte[], byte[]> generateKeyFromPath(String path)
      throws GeneralSecurityException {
    if (path == null) {
      throw new IllegalArgumentException("Unable to find keys from a null path!");
    }
    //split the path string
    List<String> pathValue = new ArrayList<>(Arrays.asList(path.split("/")));
    Log.d(TAG, "Path decomposed: " + pathValue);

    pathValue.remove(0); //remove the first element (m)

    //convert the path string in an array of int
    int[] pathValueInt = pathValue
        .stream()
        .map(Integer::parseInt)
        .mapToInt(Integer::intValue).toArray();

    // derive private and public key
    byte[] privateKey = SLIP10.deriveEd25519PrivateKey(aead.decrypt(seed, new byte[0]), pathValueInt);
    Ed25519PrivateKeyParameters prK = new Ed25519PrivateKeyParameters(privateKey, 0);
    Ed25519PublicKeyParameters puK = prK.generatePublicKey();
    byte[] publicKey = puK.getEncoded();

    return new Pair<>(privateKey, publicKey);
  }

  /**
   * Method that allows generate keys from the ID of the LAO and the ID of the RollCall.
   *
   * @param laoID a String.
   * @param rollCallID a String.
   * @return a Pair<byte[], byte[]> representing the keys pair.
   * @throws GeneralSecurityException
   */
  public Pair<byte[], byte[]> findKeyPair(String laoID, String rollCallID)
      throws GeneralSecurityException {
    if (laoID == null || rollCallID == null) {
      throw new IllegalArgumentException("Unable to find keys from a null param");
    }
    //Generate the string path
    StringJoiner joiner = new StringJoiner("/");
    joiner.add("m");
    joiner.add(Integer.toString(PURPOSE));
    joiner.add(Integer.toString(ACCOUNT));
    joiner.add(convertStringToPath(laoID));
    joiner.add(convertStringToPath(rollCallID));
    String res = joiner.toString();

    Log.d(TAG, "Generated path: " + res);

    return generateKeyFromPath(res);
  }

  /*
   This method allow to take a 256-bit string, and split it in many 24-bit or less string.
   So, we convert first the string in an byte array, and we iterate on it taking 3 element (byte)
   each time concatenate them and append to our result string.
   (string of the format: 3-byte/3-byte/... )
   */
  private String convertStringToPath(String string){
    // extract byte form string
    byte[] byteString = Base64.getUrlDecoder().decode(string);
    int remainder = byteString.length % 3;

    // create 31-bit index path
    StringJoiner joiner = new StringJoiner("/");
    int i;
    for (i = 0; i + 3 <= byteString.length; i += 3) {
      String path = Integer.toString(byteString[i] & 0xFF)
          .concat(Integer.toString(byteString[i+1] & 0xFF))
          .concat(Integer.toString(byteString[i+2] & 0xFF));

      joiner.add(path);
    }
    if (remainder == 1) {
      joiner.add(Integer.toString(byteString[i] & 0xFF));
    } else if (remainder == 2) {
      joiner.add(Integer.toString(byteString[i] & 0xFF)
          .concat(Integer.toString(byteString[i+1] & 0xFF)));
    }
    return joiner.toString();
  }

  /**
   * Method that allows recover key pair, if the user has participated in that roll-call event.
   *
   * @param laoID a String.
   * @param rollCallID a String.
   * @param rollCallTokens a List<byte[]> representing the list of public keys present
   *                         on roll-call’s results.
   * @return the key pair Pair<byte[], byte[]> (PoP token) if the user as in that roll-call
   * participated else null.
   * @throws GeneralSecurityException
   */
  public Pair<byte[], byte[]> recoverKey(String laoID, String rollCallID,
      List<byte[]> rollCallTokens)
      throws GeneralSecurityException {

    if (laoID == null || rollCallID == null) {
      throw new IllegalArgumentException("Unable to find keys from a null param");
    }

    Pair<byte[], byte[]> keyPairFind = findKeyPair(laoID,rollCallID);
    for(byte[] public_key : rollCallTokens){
      if(Arrays.equals(keyPairFind.second, public_key)){
        return keyPairFind;
      }
    }
    return null;
  }

  /**
   * Method that allows recover recover all the key pairs when the master secret is imported
   * initially, by iterating all the historical events of LAO.
   *
   * @param seed the master secret String
   * @param knowsLaosRollCalls a Map<Pair<String, String>, List<byte[]>> of keys known Lao_ID
   *                              and Roll_call_ID and values representing the list of public keys
   *                              present on roll-call’s results.
   * @return a Map<Pair<String, String>, Pair<byte[], byte[]>> of the recover key pairs
   * associated to each Lao and roll-call IDs.
   * @throws GeneralSecurityException
   */
  public Map<Pair<String, String>, Pair<byte[], byte[]>> recoverAllKeys(String seed,
      Map<Pair<String, String>, List<byte[]>>  knowsLaosRollCalls)
      throws GeneralSecurityException {
    if (knowsLaosRollCalls == null) {
      throw new IllegalArgumentException("Unable to find recover keys from a null param");
    }

    initialize(seed);

    Map<Pair<String, String>, Pair<byte[], byte[]>> result = new HashMap<>();
    for (Map.Entry<Pair<String, String>, List<byte[]>> entry : knowsLaosRollCalls.entrySet()) {
      String laoID = entry.getKey().first;
      String rollCallID = entry.getKey().second;
      Pair<byte[], byte[]> recoverKey = recoverKey(laoID,  rollCallID, entry.getValue());
      if(recoverKey != null){
        result.put(new Pair<>(laoID, rollCallID), recoverKey);
      }
    }
    return result;
  }

  /**
   * Method that encode the seed into a form that is easier for humans to securely back-up
   * and retrieve.
   *
   * @return an array of words: mnemonic sentence representing the seed for the wallet in case that
   * the key set manager is not init return a empty array.
   * @throws GeneralSecurityException
   */
  public String[] exportSeed() throws GeneralSecurityException {
    if(aead != null) {
      SecureRandom random = new SecureRandom();
      byte[] entropy = random.generateSeed(Words.TWELVE.byteLength());

      StringBuilder sb = new StringBuilder();
      MnemonicGenerator generator = new MnemonicGenerator(English.INSTANCE);
      generator.createMnemonic(entropy, sb::append);

      String[] words = sb.toString().split(" ");
      Log.d(TAG, "the array of word generated:" + Arrays.toString(words));

      StringJoiner joiner = new StringJoiner(" ");
      for (String i : words)
        joiner.add(i);
      seed = aead.encrypt(new SeedCalculator().calculateSeed(joiner.toString(), ""),
          new byte[0]);
      Log.d(TAG, "ExportSeed: new seed initialized: " + Utils.bytesToHex(seed));

      return words;
    } else {
      Log.d(TAG, "key set manager not init!");
      return new String[0];
    }
  }

  /**
   * Method that allow import mnemonic seed.
   *
   * @param words a String.
   * @param knowsLaosRollCalls a Map<Pair<String, String>, List<byte[]>> of keys known Lao_ID
   *                              and Roll_call_ID and values representing the list of public keys
   *                              present on roll-call’s results.
   * @return a Map<Pair<String, String>, Pair<byte[], byte[]>> of the recover key pairs
   *         associated to each Lao and roll-call IDs or null in case of error.
   */
  public Map<Pair<String, String>, Pair<byte[], byte[]>> importSeed(String words,
      Map<Pair<String, String>, List<byte[]>>  knowsLaosRollCalls){
    if (words == null) {
      throw new IllegalArgumentException("Unable to find recover tokens from a null param");
    }
    if(aead != null) {
      try {
        MnemonicValidator
            .ofWordList(English.INSTANCE)
            .validate(words);
        seed = aead.encrypt(new SeedCalculator().calculateSeed(words, ""), new byte[0]);
        Log.d(TAG, "ImportSeed: new seed: " + Utils.bytesToHex(seed));
        return recoverAllKeys(Utils.bytesToHex(seed), knowsLaosRollCalls);

      } catch (Exception e) {
        Log.d(TAG, "Unable to import words:" + e.getMessage());
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Determine whether wallet has been initialized
   * @return true if wallet has been set up, false otherwise
   */
  public boolean isSetUp(){
    return isSetup;
  }

  /**
   * Logout the wallet by replacing the seed by a random one
   */
  public void logout(){
    setRandomSeed();
  }

  /**
   * Utility function to initialize the wallet with a new random seed.
   */
  private void setRandomSeed(){
    SecureRandom random = new SecureRandom();
    byte[] bytes = random.generateSeed(64);
    seed = bytes;
    isSetup = false;
    Log.d(TAG, "Wallet initialized with a new random seed: " + Utils.bytesToHex(seed));
  }
}
