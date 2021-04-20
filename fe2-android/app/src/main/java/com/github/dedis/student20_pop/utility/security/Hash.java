package com.github.dedis.student20_pop.utility.security;

import android.util.Log;
import com.github.dedis.student20_pop.exceptions.PoPException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** SHA256 Hashing Class */
public class Hash {

  public static final String TAG = Hash.class.getSimpleName();

  /**
   * Hash some objects using SHA256. Concatenate the object's string representation following the
   * protocol's directive. Then hash the obtained string
   *
   * @param strs : the strings to hash
   * @return the hashed data or null if failed to hash
   * @throws IllegalArgumentException if the data is null or empty
   * @throws PoPException if SHA-256 MessageDigest is unavailable
   */
  public static String hash(String... strs) {
    if (strs == null || strs.length == 0) {
      throw new IllegalArgumentException("cannot hash an empty/null array");
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      for (String str : strs) {
        if (str == null || str.length() == 0) {
          throw new IllegalArgumentException("cannot hash an empty/null string");
        }
        String length = Integer.toString(str.length());
        digest.update(length.getBytes(StandardCharsets.UTF_8));
        digest.update(str.getBytes(StandardCharsets.UTF_8));
      }
      byte[] digestBuf = digest.digest();
      return Base64.getEncoder().encodeToString(digestBuf);
    } catch (NoSuchAlgorithmException e) {
      Log.e(TAG, "failed to hash", e);
      throw new PoPException("failed to retrieve SHA-256 instance", e);
    }
  }

  private static String esc(String input) {
    return input.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
