package com.github.dedis.student20_pop;

import com.github.dedis.student20_pop.model.Wallet;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;
public class WalletTest {
    @Test
    public void simpleTest() throws NoSuchAlgorithmException {
        Wallet w = new Wallet();
        w.GenerateKeyPair( "lao1", "RollCall1");
        w.FindKeyPair("lao1", "RollCall1");
        w.GenerateKeyPair( "lao2", "RollCall2");
        w.FindKeyPair("lao2", "RollCall2");
    }
}