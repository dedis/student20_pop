package com.github.dedis.student20_pop.model;

import android.util.Log;
import android.util.Pair;

import java.security.KeyPair;
//import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import net.i2p.crypto.eddsa.KeyPairGenerator;


public class Wallet {
    private Map<Pair<String, String>,  KeyPair> keys = new HashMap<>();

    public void GenerateKeyPair(String LaoID, String RollCallID) throws NoSuchAlgorithmException {
        /*
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair kp = keyGen.generateKeyPair();*/

        //source code of library: https://github.com/str4d/ed25519-java/blob/master/src/net/i2p/crypto/eddsa/KeyPairGenerator.java
        KeyPairGenerator keyGen = new KeyPairGenerator();
        KeyPair kp = keyGen.generateKeyPair();

        byte[] b = kp.getPrivate().getEncoded();
        Log.d("GENERATE", "Private key: "+Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()));
        Log.d("GENERATE", "Public key: "+Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
        keys.put(new Pair(LaoID, RollCallID), kp);
    }

    public KeyPair FindKeyPair(String LaoID, String RollCallID) {
        KeyPair kp = keys.get(new Pair(LaoID,RollCallID));
        Log.d("FIND", "Private key: "+Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()));
        Log.d("FIND", "Public key: "+Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
        return kp;
    }
}