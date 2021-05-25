package com.github.dedis.student20_pop.model.stellar;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

/**
  <a href="https://github.com/stellar/java-stellar-sdk/blob/master/src/main/java/org/stellar/sdk/SLIP10.java">GITHUB FILE</a>.
  <a href="https://github.com/stellar/java-stellar-sdk/commit/a63d97970aff6121637235944be9c4ec48aa4fa2">GITHUB COMMIT</a>.
 **/
public final class SLIP10 {

  private SLIP10() {
  }

  private static final String HMAC_SHA_512_ALGORITHM = "HmacSHA512";

  /**
   * Derives only the private key for ED25519 in the manor defined in
   * <a href="https://github.com/satoshilabs/slips/blob/master/slip-0010.md">SLIP-0010</a>.
   *
   * @param seed    Seed, the BIP0039 output.
   * @param indexes an array of indexes that define the path. E.g. for m/1'/2'/3', pass 1, 2, 3.
   *                As with Ed25519 non-hardened child indexes are not supported, this function treats all indexes
   *                as hardened.
   * @return Private key.
   * @throws NoSuchAlgorithmException If it cannot find the HmacSHA512 algorithm by name.
   * @throws ShortBufferException     Occurrence not expected.
   * @throws InvalidKeyException      Occurrence not expected.
   */
  public static byte[] deriveEd25519PrivateKey(final byte[] seed, final int... indexes)
      throws NoSuchAlgorithmException, ShortBufferException, InvalidKeyException {

    final byte[] iMacSha = new byte[64];
    final Mac mac = Mac.getInstance(HMAC_SHA_512_ALGORITHM);

    // I = HMAC-SHA512(Key = bytes("ed25519 seed"), Data = seed)
    mac.init(new SecretKeySpec("ed25519 seed".getBytes(StandardCharsets.UTF_8),
        HMAC_SHA_512_ALGORITHM));
    mac.update(seed);
    mac.doFinal(iMacSha, 0);

    for (int i : indexes) {
      // Key = Ir
      mac.init(new SecretKeySpec(iMacSha, 32, 32, HMAC_SHA_512_ALGORITHM));
      // Data = 0x00
      mac.update((byte) 0x00);
      // Data += Il
      mac.update(iMacSha, 0, 32);
      // Data += ser32(i')
      mac.update((byte) (i >> 24 | 0x80));
      mac.update((byte) (i >> 16));
      mac.update((byte) (i >> 8));
      mac.update((byte) i);
      // Write to I
      mac.doFinal(iMacSha, 0);
    }

    final byte[] ilMacSha = new byte[32];
    // copy head 32 bytes of I into Il
    System.arraycopy(iMacSha, 0, ilMacSha, 0, 32);
    return ilMacSha;
  }
}