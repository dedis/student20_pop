/**
 * @return the SubtleCrypto library on browser for browser context
 */
export function getCrypto() : Crypto {
  return window.crypto;
}
