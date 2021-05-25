package com.github.dedis.student20_pop.model;

import static org.junit.Assert.assertArrayEquals;

import androidx.core.util.Pair;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.StringJoiner;
import javax.crypto.ShortBufferException;
import org.junit.Test;

public class WalletTest {

  @Test
  public void importSeedAndExportSeedAreCoherent()
      throws NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {

    String Lao_ID = "1234123412341234";
    String Roll_Call_ID = "1234123412341234";

    Wallet hdw1 = new Wallet();
    String[] exp_str = hdw1.exportSeed();
    StringJoiner joiner = new StringJoiner(" ");
    for(String i: exp_str) joiner.add(i);

    Pair<byte[], byte[]> res1 =  hdw1.findKeyPair(Lao_ID,Roll_Call_ID);

    Wallet hdw2 = new Wallet();
    hdw2.importSeed(joiner.toString(), new HashMap<>());
    Pair<byte[], byte[]> res2 =  hdw2.findKeyPair(Lao_ID,Roll_Call_ID);

    assertArrayEquals(res1.first, res2.first);
    assertArrayEquals(res1.second, res2.second);
  }
}
