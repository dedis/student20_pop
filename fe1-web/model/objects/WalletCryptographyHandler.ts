import { get, set, update } from 'idb-keyval';
import STRINGS from 'res/strings';
import { getCrypto } from '../../utils/Crypto';

/* wallet cryptography key interface */
export interface WalletCryptoKey {
  privateKey: CryptoKey,
  publicKey: CryptoKey,
}

/**
 * This class has the job of handling the cryptography functions of the wallet.
 * It interacts with the IndexedDB database in order to store and retrieve the secret key.
 * It also encrypts and decrypts the wallet seed (used to create the tokens) with the retrieved key.
 * More info on this approach at https://blog.engelke.com/2014/09/19/saving-cryptographic-keys-in-the-browser/
 */
export class WalletCryptographyHandler {
  private readonly publicKeyId: string = STRINGS.wallet_public_key_id;

  private readonly privateKeyId: string = STRINGS.wallet_private_key_id;

  /* encryption/decryption algorithm (RSA) */
  private readonly algorithm = {
    name: 'RSA-OAEP',
    modulusLength: 4096,
    publicExponent: new Uint8Array([1, 0, 1]),
    hash: 'SHA-256',
  };

  /* usages for the RSA key */
  private readonly keyUsages: KeyUsage[] = ['encrypt', 'decrypt'];

  /**
   * This functions verifies weather or not the wallet storage in IndexedDB database
   * is initialised. If not it initialises the storage.
   */
  public async initWalletStorage(): Promise<void> {
    /* verifies if wallet storage has already been initialised */
    const tryPublicKey: CryptoKey = await this.getKeyFromDatabase(STRINGS.wallet_public_key);
    const tryPrivateKey: CryptoKey = await this.getKeyFromDatabase(STRINGS.wallet_private_key);
    const walletIsNotInitialised: boolean = (tryPublicKey === undefined)
      || (tryPrivateKey === undefined);

    if (walletIsNotInitialised) {
      await this.handleWalletInitialization();
      console.log('Wallet cryptography was not initialized');
    }
    console.log('Wallet cryptography storage ready');
  }

  /**
   * encrypts the given ed25519 seed with the RSA key stored in the indexedDB database
   * @param plaintext ed25519 seed toUint8Array()
   */
  public async encrypt(plaintext: Uint8Array): Promise<ArrayBuffer> {
    const key: CryptoKey = await this.getKeyFromDatabase(STRINGS.wallet_public_key);
    if (key === undefined) {
      throw Error('Error while retrieving encryption key from database: undefined');
    }
    return getCrypto().subtle.encrypt(this.algorithm, key, plaintext);
  }

  /**
   * decrypts the encrypted ed25519 seed with the RSA key stored in the indexedDB database
   * @param ciphertext ed25519 encrypted seed (ArrayBuffer)
   */
  public async decrypt(ciphertext: ArrayBuffer): Promise<ArrayBuffer> {
    const key = await this.getKeyFromDatabase(STRINGS.wallet_private_key);
    if (key === undefined) {
      throw Error('Error while retrieving decryption key from database: undefined');
    }
    return getCrypto().subtle.decrypt(this.algorithm, key, ciphertext);
  }

  /**
   * adds an encryption/decryption key to the storage
   * @private
   * @param key the key that will be used to encrypt/decrypt all the
   * seed generating all tokens in the wallet
   */
  private async putKeyInDatabase(key: WalletCryptoKey): Promise<void> {
    await set(this.publicKeyId, key.publicKey);
    await set(this.privateKeyId, key.privateKey);
  }

  /**
   * returns the requested encryption key from the storage (only one crypto-key allowed in storage)
   * @private
   * @param type 'public' if the desired key is the public (encryption) key,
   * 'private' if the desired key is the private (decryption) key
   */
  private async getKeyFromDatabase(type: string): Promise<CryptoKey> {
    if (type === STRINGS.wallet_private_key) {
      const key = await get(this.privateKeyId);
      return key;
    }
    const key = await get(this.publicKeyId);
    return key;
  }

  /**
   * this is used to update the encryption key in case it will ever be
   * necessary to change it, it updates the key since only one crypto-key
   * is allowed in the wallet storage.
   * @private
   * @param key the new key for encryption/decryption
   */
  private async updateKeyInDatabase(key: WalletCryptoKey): Promise<void> {
    await update(this.publicKeyId, () => key.publicKey);
    await update(this.privateKeyId, () => key.privateKey);
  }

  /**
   * This method should solve the problems of wallet storage initialisation.
   * If the wallet storage is not correctly initialised the reason could be two:
   * 1. it never was initialised
   * 2. it was initialised but then cleared for some reason (e.g. clear browser data)
   *
   * In the first case the wallet is initialised by generating an RSA key and creating the
   * wallet storage. In the second case a solution still has to be found, se issue on github.
   * @private
   */
  private async handleWalletInitialization() {
    /* if (storage has never been initialised) { */
    const key: WalletCryptoKey = await this.generateRSAKey();
    await this.putKeyInDatabase(key);

    /* } else if (the IndexedDB was cleared for some reason) {
      TODO: find a way to fix this and retrieve the keys
      https://github.com/dedis/student_21_pop/issues/297
      }
    */
  }

  /**
   * generates the RSA key according to specified algorithm
   * @private
   */
  private async generateRSAKey(): Promise<WalletCryptoKey> {
    const keyPair = await getCrypto().subtle.generateKey(
      this.algorithm, false, this.keyUsages,
    );
    return {
      publicKey: keyPair.publicKey,
      privateKey: keyPair.privateKey,
    };
  }
}
