import 'jest-extended';
import { HDWallet } from 'model/objects/HDWallet';
import { Hash } from 'model/objects';
import { WalletStore } from 'store/stores/WalletStore';

const mockMnemonicSeed: string = 'garbage effort river orphan negative kind outside quit hat camera approve first';

/**
 * This is the test class for the wallet. The test descriptions are self-explanatory.
 * The test cover both the functionality on the web front-end and cross-testing
 * compatibility with the Java front-end.
 */
describe('=== Hierarchical Deterministic Wallet Test ===', () => {
  describe('HD Wallet initialization test', () => {
    it('should initialize a wallet from mnemonic then destroy the wallet and retrieve it from redux state', async () => {
      let walletWhenInitialized: HDWallet | null = new HDWallet();
      let seedWhenInitialized: Uint8Array = new Uint8Array();

      walletWhenInitialized.initialize(mockMnemonicSeed)
        .then((isSuccessful) => expect(isSuccessful).toBe(true));

      walletWhenInitialized.getDecryptedSeed()
        .then((plaintextSeed) => {
          seedWhenInitialized = plaintextSeed;
        });

      walletWhenInitialized = null;

      expect(walletWhenInitialized).toBeNull();

      let seedFromState: Uint8Array = new Uint8Array();

      WalletStore.get()
        .then((encryptedSeed) => {
          if (encryptedSeed !== undefined) {
            HDWallet
              .fromState(encryptedSeed)
              .then((walletFromState) => {
                walletFromState.getDecryptedSeed()
                  .then((plaintextSeed) => {
                    seedFromState = plaintextSeed;
                  });
              });
          }
        });

      expect(seedWhenInitialized).toStrictEqual(seedFromState);
    });
  });

  describe('HD Wallet seed cross-testing with Java front-end', () => {
    it('should correctly recover the SAME hex master key starting from the same mnemonic as the Java front-end', async () => {
      const wallet: HDWallet = new HDWallet();

      wallet.initialize(mockMnemonicSeed)
        .then((isSuccessful) => expect(isSuccessful).toBe(true));

      const javaFrontEndPlaintextSeedFromMockMnemonic: string = '010ac98c615c31a20a6a9fcb71c94642abdd4f662d148f81d61479c8f125854bac9c0228f6705cbdd96e27ffb2d4e806d152c875a5484113434d1d561e42a94d';

      wallet.getDecryptedSeed()
        .then((plaintextSeed) => {
          expect(Buffer.from(plaintextSeed).toString('hex')).toBe(javaFrontEndPlaintextSeedFromMockMnemonic);
        });
    });
  });

  describe('HD Wallet derivation cross-testing with Java front-end', () => {
    it('should correctly recover the SAME public key from path starting from the same wallet seed, LAO ID and roll call ID as the Java front-end', async () => {
      const wallet: HDWallet = new HDWallet();

      wallet.initialize(mockMnemonicSeed)
        .then((isSuccessful) => expect(isSuccessful).toBe(true));

      const laoId: Hash = new Hash('T8grJq7LR9KGjE7741gXMqPny8xsLvsyBiwIFwoF7rg=');
      const rollCallId: Hash = new Hash('T8grJq7LR9KGjE7741gXMqPny8xsLvsyBiwIFwoF7rg=');

      const testMap: Map<[Hash, Hash], string[]> = new Map();

      const javaFrontEndPopTokenInHex: string = '7147759d146897111bcf74f60a1948b1d3a22c9199a6b88c236eb7326adc2efc';
      testMap.set([laoId, rollCallId], [javaFrontEndPopTokenInHex, '', '', '']);

      wallet.recoverAllKeys(testMap, [])
        .then((laoAndRcIdsToTokenMap) => {
          laoAndRcIdsToTokenMap.forEach((value) => {
            expect(value).toBe(javaFrontEndPopTokenInHex);
          });
        });
    });
  });
});
