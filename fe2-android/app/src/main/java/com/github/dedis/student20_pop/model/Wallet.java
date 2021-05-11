package com.github.dedis.student20_pop.model;

import android.util.Log;
import androidx.core.util.Pair;
import com.github.dedis.student20_pop.model.stellar.SLIP10;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.MnemonicValidator;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.crypto.ShortBufferException;
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

  private static final Wallet instance = new Wallet();
  public static Wallet getInstance() {
    return instance;
  }

  /**
   * Class constructor, initialize the wallet with a new random seed.
   */
  public Wallet() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = random.generateSeed(64);
    seed = bytes;
    Log.d(TAG, "Wallet initialized with a new random seed: " + Utils.bytesToHex(seed));
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
    Log.d(TAG, "New seed initialized: " + Utils.bytesToHex(this.seed));
  }

  /**
   * Method that allow generate a different key for each path that you give.
   *
   * @param path a String path of the form: m/i/j/k/... where i,j,k,.. are 31-bit integer.
   * @return a Pair<byte[], byte[]> representing the keys pair: first=private_key; second=public_key.
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws ShortBufferException
   */
  public Pair<byte[], byte[]> generateKeyFromPath(String path)
      throws NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {
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
    byte[] privateKey = SLIP10.deriveEd25519PrivateKey(seed, pathValueInt);
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
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws ShortBufferException
   */
  public Pair<byte[], byte[]> findKeyPair(String laoID, String rollCallID)
      throws NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {
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
    byte[] byteString = Base64.getDecoder().decode(string);

    // create 31-bit index path
    StringJoiner joiner = new StringJoiner("/");
    int i = 0;
    while(i < byteString.length){
      int res = 0;
      for(int j=i; j<i+3 && j< byteString.length; j++){
        int buffer =  byteString[j];
        buffer = buffer & 0xFF; // "extract" the first 8 bit
        res = (buffer << j%3*8) | res;
      }
      i+= 3;
      joiner.add(Integer.toString(res));
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
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws ShortBufferException
   */
  public Pair<byte[], byte[]> recoverKey(String laoID, String rollCallID,
      List<byte[]> rollCallTokens)
      throws NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {

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
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws ShortBufferException
   */
  public Map<Pair<String, String>, Pair<byte[], byte[]>> recoverAllKeys(String seed,
      Map<Pair<String, String>, List<byte[]>>  knowsLaosRollCalls)
      throws NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {
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
   * @return an array of words: mnemonic sentence representing the seed for the wallet.
   */
  public String[] exportSeed(){

    SecureRandom random = new SecureRandom();
    byte[] entropy = random.generateSeed(Words.TWELVE.byteLength());

    StringBuilder sb = new StringBuilder();
    MnemonicGenerator generator = new MnemonicGenerator(English.INSTANCE);
    generator.createMnemonic(entropy, sb::append);

    String[] words = sb.toString().split(" ");
    Log.d(TAG,"the array of word generated:" + Arrays.toString(words));

    StringJoiner joiner = new StringJoiner(" ");
    for(String i: words) joiner.add(i);
    seed = new SeedCalculator().calculateSeed(joiner.toString(), "");
    Log.d(TAG, "ExportSeed: new seed initialized: " + Utils.bytesToHex(seed));

    return words;
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
      Map<Pair<String, String>, List<byte[]>>  knowsLaosRollCalls) {
    if (words == null) {
      throw new IllegalArgumentException("Unable to find recover tokens from a null param");
    }
    try {
      MnemonicValidator
          .ofWordList(English.INSTANCE)
          .validate(words);
      seed = new SeedCalculator().calculateSeed(words, "");
      Log.d(TAG, "ImportSeed: new seed: " + Utils.bytesToHex(seed));
      return recoverAllKeys(Utils.bytesToHex(seed), knowsLaosRollCalls);

    } catch (Exception e) {
      Log.d(TAG,"Unable to import words:" + e.getMessage());
      return null;
    }
  }

}
