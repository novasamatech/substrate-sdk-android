[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.nova-wallet/substrate-sdk-android/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.nova-wallet/substrate-sdk-android)

# Nova Substrate SDK for Android (Kotlin/Java)
Nova Substrate SDK is a native Android library to help developers build native mobile apps for Substrate-based networks, e.g. Polkadot, Kusama & parachains

# Table of contents

- [Nova Substrate SDK for Android (Kotlin/Java)](#nova-substrate-sdk-for-android-kotlinjava)
- [Table of contents](#table-of-contents)
  - [Bip39](#bip39)
  - [JSON import/export](#json-importexport)
    - [Import](#import)
    - [Export](#export)
  - [Extensions](#extensions)
    - [Hex](#hex)
    - [Hashing](#hashing)
  - [Icon generation](#icon-generation)
  - [Junction Decoder](#junction-decoder)
  - [Runtime](#runtime)
  - [Scale](#scale)
    - [Define a schema](#define-a-schema)
    - [Create and encode structs](#create-and-encode-structs)
    - [Decode and use structs](#decode-and-use-structs)
    - [Standart Data Types](#standart-data-types)
    - [Custom Data Types](#custom-data-types)
    - [Nullable fields](#nullable-fields)
  - [SS58](#ss58)
  - [WSRPC](#wsrpc)
    - [Initialize socket](#initialize-socket)
    - [Use socket](#use-socket)
    - [Reconnect strategy](#reconnect-strategy)
    - [Delivery type](#delivery-type)
    - [Using with coroutines](#using-with-coroutines)
    - [Mappers](#mappers)
      - [Usage](#usage)

## Bip39

Bip39 is the algorithm which provides an opportunity to use a list of words, called mnemonic, instead of a raw 32 byte seed. Library provides `Bip39` class to work with mnemonics:

``` kotlin
val bip39 = Bip39()

val newMnemonic = bip39.generateMnemonic(length = MnemonicLength.TWELVE) // twelve words
val entropy = bip39.generateEntropy(newMnemonic)
val theSameMnemonic = bip39.generateMnemonic(entropy)

```
To generate a seed, a `passphrase` is needed. Technically, it is a decoded derivation path (see [Junction Decoder](#junction-decoder))

``` kotlin
val seed = bip39.generateSeed(entropy, passphrase)
```

## JSON import/export
Library provides support for decoding/encoding account information using JSON format, compatible with Polkadot.js

### Import
Using `JsonSeedDecoder` you can perform decoding of the imported JSON:

``` kotlin
val decoder = JsonSeedDecoder(..)

decoder.extractImportMetaData(myJson) // does not perform full decoding (skips secret decrypting). Faster
decoder.decode(myJson, password) // performs full decoding. Slower
```

### Export
Using `JsonSeedEncoder` you can generate JSON out of account information:

``` kotlin
val encoder = JsonSeedEncoder(..)

val json = encoder.generate(keypair, seed, password, name, encryptionType, genesis, addressByte)
```

## Extensions
Library provides several extensions, that implement most common operations.
### Hex

``` kotlin
fun ByteArray.toHexString(withPrefix: Boolean = false): String
fun String.fromHex(): ByteArray
fun String.requirePrefix(prefix: String): String
fun String.requireHexPrefix(): String
```

### Hashing

``` kotlin
fun ByteArray.xxHash128(): ByteArray
fun ByteArray.xxHash64(): ByteArray

fun ByteArray.blake2b512(): ByteArray
fun ByteArray.blake2b256(): ByteArray
fun ByteArray.blake2b128(): ByteArray

fun XXHash64.hash(bytes: ByteArray, seed: Long = 0): ByteArray
fun BCMessageDigest.hashConcat(bytes: ByteArray): ByteArray
fun XXHash64.hashConcat(bytes: ByteArray): ByteArray
```

## Icon generation

There's a support for default Polkadot.js icon generation using `IconGenerator`:

``` kotlin
val generator = IconGenerator()
val drawable =  generator.getSvgImage(accountId, sizeInPixels)
```

## Junction Decoder

`JunctionDecoder` provides support for derivation paths:

``` kotlin
val derivationPath: String = ...
val decoder = JunctionDecoder()

val passphrase = decoder.getPassword(derivationPath) // retrieve passphrase to use in entropy -> seed generation
val decodedPath = decoder.decodeDerivationPath(derivationPath)
```

## Runtime

You can create storage keys easily:

``` kotlin
val accountId: ByteArray = ..

val bondedKey = Module.Staking.Bonded.storageKey(bytes)
val accountInfoKey = Module.System.Account.storageKey(bytes)
```

If you're missing some specific service/module, you can define it by your own:

``` kotlin
 object Staking : Module("Staking") {

    object ActiveEra : Service<Unit>(Staking, "ActiveEra") {

        override fun storageKey(storageArgs: Unit): String {
            return StorageUtils.createStorageKey(
                service = this,
                identifier = null
            )
        }
    }
}
```

## Scale

Library provides a convenient DSL to deal with scale encoding/decoding. Original codec reference: [Link](https://substrate.dev/docs/en/knowledgebase/advanced/codec).
### Define a schema

``` kotlin
object AccountData : Schema<AccountData>() {
    val free by uint128()
    val reserved by uint128()
    val miscFrozen by uint128()
    val feeFrozen by uint128()
}
```
### Create and encode structs

``` kotlin
val struct = AccountData { data ->
    data[AccountData.free] = BigDecimal("1")
    data[AccountData.reserved] = BigInteger("0")
    data[AccountData.miscFrozen] = BigInteger("0")
    data[AccountData.feeFrozen] = BigInteger("0")
}

val inHex = struct.toHexString() // encode
val asBytes = struct.toByteArray() // or as byte array
```
###  Decode and use structs

``` kotlin
val inHex = ...
val struct = AccountData.read(inHex)
val free = struct[AccountData.free]
```

### Standart Data Types
Library provides the support for the following data types:
* Numbers: `uint8`, `uint16`, `uint32`, `uint64`, `uint128`, `uint(nBytes)`, `compactInt`, `byte`, `long`
* Primitives: `bool`, `string`
* Arrays:
    * `sizedByteArray(n)` - only content is encoded/decoded), size is thus known in advance
    * `byteArray` - size can vary, so the size is also encoded/decoded alongside with the content
* Compound types:
    * `vector<D>` - List of objects of the some data type
    * `optional<D>` - Nullable container for other data type
    * `pair<D1, D2>`
    * `enum(D1, D2, D3...)` - like union in C, stores only one value at once, but this value can have different data type
    * `enum<E : Enum>` - for classical Kotlin enum

### Custom Data Types
If the decoding/encoding cannot be done using standart data types, you can create your own by extending `DataType<T>`:
``` kotlin
object Delimiter : DataType<Byte>() {
    override fun conformsType(value: Any?): Boolean {
        return value is Byte && value == 0
    }

    override fun read(reader: ScaleCodecReader): Byte {
        val read = reader.readByte()

        if (read != 0.toByte()) throw java.lang.IllegalArgumentException("Delimiter is not 0")

        return 0
    }

    override fun write(writer: ScaleCodecWriter, ignored: Byte) {
        writer.writeByte(0)
    }
}
```
And use it in your schema using `custom()` keyword:
``` kotlin
object CustomTypeTest : Schema<CustomTypeTest>() {
    val delimiter by custom(Delimiter)
}
````

### Default values
You can supply and default values for each field in the schema:
``` kotlin
object DefaultValues : Schema<DefaultValues>() {
    val bytes by sizedByteArray(length = 10, default = ByteArray(10))
    val text by string(default = "Default")
    val bigInteger by uint128(default = BigInteger.TEN)
}
```

### Nullable fields

By default, all fields are non null. However, you can use `optional()` to change the default behavior:
``` kotlin
object Person : Schema<Person>() {
    val friendName by string().optional() // friendName now is Field<String?>
}
```

## SS58

SS58 is an address format using in substate ecosystem. You can encode/decode address using `SS58Encoder`:

``` kotlin
val encoder = SS58Encoder()
val address = encoder.encode(publicKey, addressByte)
val accountId = encoder.decode(address)
```

## WSRPC

Library provides an implementation of `SocketService`, which simplifies communication with the node: it provides a seamless error recovery, subscription mechanism.

### Initialize socket
To create a socket service, you need to provide several parameters:

``` kotlin
val reconnector = Reconnector(..) // to configure reconnect strategy and scheduling executor
val requestExecutor = RequestExecutor(..) // to configure sending executor
val socketService = SocketService(gson, logger, websocketFactory, reconnector, requestExecutor)
```

### Use socket

``` kotlin
socketService.start(url) // async connect
socketService.stop() // all subscriptions/pending requests are cancelled
socketService.switchUrl(newUrl) // stops current connection and start a new one

// execute single request
socketService.executeRequest(runtimeRequest, deliveryType, object : SocketService.ResponseListener<RpcResponse> {
            override fun onNext(response: RpcResponse) {
                // success
            }

            override fun onError(throwable: Throwable) {
                // unrecoverable error happened
            }
        })

// subscribe to changes
socketService.subscribe(runtimeRequest, object : SocketService.ResponseListener<SubscriptionChange> {
                override fun onNext(response: SubscriptionChange) {
                   // change arrived
                }

                override fun onError(throwable: Throwable) {
                   // unrecoverable error happened
                }
            })
```

### Reconnect strategy

During setup of `Reconnector`, you can specify a `ReconnectStrategy`. There are several of them bundled with library:
* `ConstantReconnectStrategy`
* `LinearReconnectStrategy`
* `ExponentialReconnectStrategy`. This is a default reconnect strategy.

You can create your own strategy by implementing `ReconnectStrategy` interface.

### Delivery type

While sending a request, you can specify a `DeliveryType`. Currently, there are 3 of them:
* `AT_LEAST_ONCE` - attempts to send request until succeeded. This is a default delivery type.
* `AT_MOST_ONCE` - send request once, reports error if attempt failed.
* `ON_RECONNECT` - similar to `AT_LEAST_ONCE`, but remembers request and sends it on each reconnect. Currently used for subscription initiation.

### Using with coroutines

Library has a out-of-box support for coroutines:

``` kotlin
scope.launch {
    val response = socketService.executeAsync(request, deliveryType) // suspend function
}

socketService.subscriptionFlow(request).onEach { change ->
        // do stuff here
}.launchIn(scope)
```

### Mappers

The mappers for most common types are provided:
* `scale` - For scale-encoded values
* `scaleCollection` - For list of scale-encoded values
* `pojo` - for json values
* `pojoList` - for list of json values

All mappers return a `nullable` result by default. You can add `nonNull()` modifier to change this behavior. In case of null result, the `RpcException` will be thrown.

#### Usage

``` kotlin
scale().nonNull().map(response, gson)

// or with coroutines adapter
socketService.executeAsync(request, deliveryType, mapper = scale().nonNull())
```
