import { sign } from 'tweetnacl';
import { get } from 'idb-keyval';
import STRINGS from 'res/strings';

/**
 * building the MOCK crypto library
 */
const crypto = {};

/* adding to crypto used by mockCryptoManager the subtle property
   with the MOCKED functions: generateKey, encrypt, decrypt */
Object.defineProperty(crypto, 'subtle', {
  value: {
    generateKey: () => ({
      publicKey: sign.keyPair().secretKey,
      privateKey: sign.keyPair().publicKey,
    }),

    /* this mock encrypt function appends public encryption key to seed */
    encrypt: async (s: AesGcmParams, publicKey: CryptoKey, plaintext: Uint8Array) => {
      const ciphertext: string = plaintext.join().concat(publicKey.toString());
      const ciphertextBuffer: ArrayBuffer = Buffer.from(ciphertext);
      return Promise.resolve(ciphertextBuffer);
    },

    /* the mock decrypt function is called AFTER walletCryptoManager destruction.
       The check is done on key coherence in database: since the encryption appended
       the public key to the seed we need to retrieve the public key and undo the append
       in order to provide correct decryption */
    decrypt: async (s: RsaOaepParams, privateKey: CryptoKey, ciphertext: ArrayBuffer) => {
      /* note the publicKey has to be retrieved here since the one passed in
      parameter by the decrypt call in handler is the private key */
      const publicKey = await get(STRINGS.wallet_public_key_id);
      /* undoing public key append by substituting the substring with empty string */
      const plaintext: string = ciphertext.toString().replace(publicKey.toString(), '');
      const plaintextBuffer: ArrayBuffer = Buffer.from(plaintext);
      return Promise.resolve(plaintextBuffer);
    },
  },
});

/**
 * returns the MOCK SubtleCrypto library on browser for browser context
 */
export function getCrypto() : Crypto {
  return crypto as Crypto;
}
