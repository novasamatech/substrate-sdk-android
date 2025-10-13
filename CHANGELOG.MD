# Changelog

## v2.9.2

* Support for raw scale serialization
* Support for encoding regular enums to DictEnum.Entry with null associated value

### Changes

#### Raw scale serialization

* Useful when we want to operate low level scale structures for some of the fields / arguments
* `RawScaleValue` can be used for fields
* `AsRawScaleValue` wrapper can be used for top-level serialization (e.g. storage keys / whole values)

#### Regular enum encoding to DictEnum.Entry with null associated value

* Introduced`AsDictEnum` annotation that can be applied to `enum class` to signal that it should be encoded as `DictEnum.Entry`

## v2.9.1

### Changes

* Automatically convert between snake and camel case for struct fields

## v2.9.0

This release is focused around fixes and refactoring of import from json feature

### Changes

* Fix compatibility of ED25519 and ECDSA JSON files when custom derivation path is used
* Finally make sr25519 work in tests
* Refactor and improve coverage of json-related tests. Now tests cover self-compatibility as well as 
compatibility with external sources (like polkadot-js) for all encryption x derivation path cases
* Add post-conditions to ensure we always derive a public key that corresponds to the one present in json 

### Breaking changes

* `JsonSeedEncoder` and `JsonSeedDecoder` are renamed to `JsonEncoder` and `JsonDecoder` respectively
* Removed `seed` argument from `JsonEncoder#generate` as well as from return type of `JsonDecoder#decode`
Seed is a vague concept in a context of a json import, which is solely focused around private key exports
so it should not be used

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

