# Changelog

## v2.8.0 

This release is focused around Bip32 and Bip39 derivation improvements

### New features

* Added `Bip32Ed25519KeypairFactory` that implement bip32 derivation with ed25519 curve

### Breaking changes

* `Bip32KeypairFactory` is renamed to `Bip32EcdsaKeypairFactory`
* `EthereumSeedFactory` is renamed to `Bip39SeedFactory`. It is also moved into `bip39` package (was `ethereum`)
* `EthereumKeypairFactory` is removed. `Bip32EcdsaKeypairFactory` should be used instead
* All classes related to bip32 are moved from `ethereum` to `bip32` package: 
`Bip32EcdsaKeypairFactory`, `Bip32ExtendedKeyPair`

