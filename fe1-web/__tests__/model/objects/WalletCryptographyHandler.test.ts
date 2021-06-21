import 'jest-extended';
import { WalletCryptographyHandler } from 'model/objects/WalletCryptographyHandler';
import { sign } from 'tweetnacl';

/* used to simulate indexedDB database to test store/retrieve functions */
import 'fake-indexeddb/auto';

jest.mock('utils/Crypto');

/**
 * This test uses a MOCK version of the crypto.subtle API.
 * The scope is not testing the SubtleCrypto library but testing the function
 * calls to it. It also tests the 'store' and 'retrieve' functions of indexedDB
 * with an intermediate simulation of app restart.
 */
describe('=== Wallet Cryptography Handler tests ===', () => {
  describe('encryption - decryption', () => {
    it('should correctly encrypt and decrypt the generated seed', async () => {
      /* this is the MOCK crypto manager, it uses the mock SubtleCrypto library */

      let mockCryptoManager: WalletCryptographyHandler | null = new WalletCryptographyHandler();

      /* seed to encrypt */
      const seed = sign.keyPair().secretKey;

      /* initialization of wallet storage and crypto key in database
       here the generated keys are STORED in indexedDB simulator */
      await mockCryptoManager.initWalletStorage();

      /* encrypting using mock encrypt */
      const cypher: ArrayBuffer = await mockCryptoManager.encrypt(seed);

      /* deleting previous manager and creating new instance of wallet manager, this should ensure
         ensure the correct store and retrieve of the RSA key in indexedDB - app restart */
      mockCryptoManager = null;
      const newMockCryptoManager = new WalletCryptographyHandler();

      expect(mockCryptoManager).toBeNull();

      /* initialization of wallet storage and retrieve crypto key in database */
      await newMockCryptoManager.initWalletStorage();

      /* decrypting using mock decrypt, if the decryption works it implies indexedDB
         database has correctly stored the keys after app restart */
      const plaintext: ArrayBuffer = await newMockCryptoManager.decrypt(cypher);

      expect(plaintext.toString()).toStrictEqual(seed.join());
    });
  });
});
