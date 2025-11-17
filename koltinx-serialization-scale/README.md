# SCALE format support for kotlinx-serialization

This module provides support for SCALE codec serialization in two formats:
- **Dynamic Format (Scale)**: Converts between Kotlin types and dynamic structures used by the core substrate-sdk-android module
- **Binary Format (BinaryScale)**: Direct binary encoding/decoding following the SCALE codec specification

## Table of Contents

- [Get started](#get-started)
  - [Installation](#installation)
- [Dynamic Format (Scale)](#dynamic-format-scale)
  - [Usage](#usage)
  - [Scale Types](#scale-types)
    - [Primitives](#primitives)
    - [Strings](#strings)
    - [Byte Arrays](#byte-arrays)
    - [Booleans](#booleans)
    - [Regular enums](#regular-enums)
    - [Classes](#classes)
    - [Objects](#objects)
    - [Value classes](#value-classes)
    - [Sealed classes](#sealed-classes)
  - [Annotations](#annotations)
    - [@SerialName](#serialname-annotation)
    - [@SerializedFallback](#serializedfallback-annotation)
    - [@TransientStruct](#transientstruct-annotation)
    - [@AsTuple](#astuple-annotation)
- [Binary Format (BinaryScale)](#binary-format-binaryscale)
  - [Usage](#usage-1)
  - [Binary Types](#binary-types)
    - [Primitives](#primitives-1)
      - [Booleans](#booleans-1)
      - [Numbers](#numbers)
    - [Compact Integers](#compact-integers)
    - [Strings](#strings-1)
    - [Byte Arrays](#byte-arrays-1)
      - [Variable Length](#variable-length)
      - [Fixed Length](#fixed-length)
    - [Lists](#lists)
      - [Variable Length](#variable-length-1)
      - [Fixed Length](#fixed-length-1)
    - [Optional Types](#optional-types)
      - [Optional Booleans](#optional-booleans)
    - [Classes](#classes-1)
    - [Objects](#objects-1)
    - [Value Classes](#value-classes-1)
    - [Enums](#enums)
    - [Sealed Classes (Discriminated Unions)](#sealed-classes-discriminated-unions)
  - [Annotations](#annotations-1)
    - [@EnumIndex](#enumindex)
    - [@FixedLength](#fixedlength)

## Get started

### Installation

1. Add plugin to the root `build.gradle` file. The version of the plugin should match
the version of kotlin used in your project

```groovy
plugins {
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.9.22' apply false
}
```

and to each module where you want to declare `@Serializable` entities

```groovy
plugins {
    id "kotlinx-serialization"
}
```

3. Add a runtime dependency
```groovy
dependencies {
    implementation "io.github.nova-wallet.substrate-sdk-android:kotlinx-serialization-scale:$substrateSdkVersion"
}
```

---

## Dynamic Format (Scale)

The `Scale` format converts Kotlin types to/from dynamic structures compatible with substrate-sdk-android.

### Usage

1. Declare a new type that you want to convert to/from

```kotlin
@Serializable
class Test(val a: Boolean)
```

2. Use `Scale` object to encode and decode instances of `Test`

```kotlin
val decoded = Test(a=true)
val encoded = Struct.Instance(mapOf("a" to true))

assert(encoded == Scale.encode(decoded))
assert(decoded == Scale.decode<Test>(encoded))
```

### Scale Types

#### Primitives

All decimal numbers encode to `BigInteger`

```kotlin
assert(BigInteger.ONE == Scale.encode(1))
assert(BigInteger.ONE == Scale.encode(1.toShort()))
assert(BigInteger.ONE == Scale.encode(1.toByte()))
assert(BigInteger.ONE == Scale.encode(1.toLong()))
assert(BigInteger.ONE == Scale.encode(1.toBigInteger()))
```

`Float` and `Double` types are not supported since they have no equivalent in SCALE standard

### Strings

Strings encode to UTF8-encoded byte arrays

```kotlin
// Using `equals` instead of `contentEquals` for the sake of clarity
assert("Some".encodeToByteArray() == Scale.encode("Some"))
```

### Byte Arrays

Byte arrays encode to themselves

```kotlin
// Using `equals` instead of `contentEquals` for the sake of clarity
assert(byteArrayOf(1, 2, 3) == Scale.encode(byteArrayOf(1, 2, 3)))
```

### Booleans

Booleans encode to itself

```kotlin
assert(true == Scale.encode(true))
```

#### Regular enums

By default, enum values encode to the corresponding value name

```kotlin
enum class A {
        SOME
}

assert("SOME" == Scale.encode(A.SOME))
```

#### Using `ByteArray` or `BigInteger` in a struct
When using `ByteArray` or `BigInteger` in the nested context (as a field of a class), you should use the
following typealiases: `ByteArraySerializable` and `BigIntegerSerializable`.
Otherwise the code will not compile / encoding wont work properly

### Classes

Regular classes encode to `Struct.Instance`. They should be marked with `Serializable`
to be recognized by the plugin
Note that naming of the fields is converted from camel case to snake case as the substrate-sdk
expects snake case named keys for structs

```kotlin
@Serializable
class Test(val someName: Boolean)

val decoded = Test(someName=true)
val encoded = Struct.Instance(mapOf("some_name" to true))

assert(encoded == Scale.encode(decoded))
```

### Objects

Objects encode to `null`

```kotlin
@Serializable
object Singleton

assert(null == Scale.encode(Singleton))
```

### Value classes

Value classes are transient for encoding and encode to their inner values

```kotlin
@JvmInline
@Serializable
value class Transient(val value: Boolean)

assertEquals(true, Scale.encode(Transient(true)))
```

### Sealed classes

Sealed hierarchies encode to `DictEnum.Entry<*>`
When encoding sealed class instance, it is important to type them as base class (`as Value`)
Otherwise serialization plugin will try to resolve them as a non-polymorphic type, without wrapping them into Enum

Also node that when encoding sealed subclass with a single field, the optimization will be applied and
wrapping into `Struct.Instance` will be skipped

```kotlin
@Serializable
sealed class Value {

    @Serializable
    object Null : Value()

    @Serializable
    class Single(val a: Boolean): Value()

    @Serializable
    class Double(val a: Boolean, val b: Boolean): Value()
}

assertEquals(
    DictEnum.Entry("Null", null),
    Scale.encode(Value.Null as Value)
)
assertEquals(
    DictEnum.Entry("Single", true),
    Scale.encode(Value.Single(true) as Value)
)
assertEquals(
    DictEnum.Entry("Double", Struct.Instance(mapOf("a" to true, "b" to true))),
    Scale.encode(Value.Double(true, true) as Value)
)
```

### Annotations

#### `@SerialName` annotation

You can use `@SerialName` to alter the behavior of naming policy for classes and sealed subclasses

```kotlin
@Serializable
class Test(@SerialName("changed") val someName: Boolean)

val decoded = Test(someName=true)
val encoded = Struct.Instance(mapOf("changed" to true))
```

```kotlin
@Serializable
sealed class Value {

    @Serializable
    @SerialName("AnotherName")
    class SomeName(val a: Boolean): Value()
}

@Serializable
assertEquals(
    DictEnum.Entry("AnotherName", true),
    Scale.encode(Value.SomeName(true) as Value)
)
```

Note that for regular enums it is important to mark enum as `@Serializable`
when applying `@SerialName` or other annotations

```kotlin
@Serializable
enum class ABC {
    @SerialName("B") A
}

assertEquals("B", Scale.encode(ABC.A))
```

#### `@SerializedFallback` annotation

`@SerializedFallback` can be used on regular enums and sealed hierarchies to denote a fallback case
when decoding fails to find the matching variant in the type

```kotlin
@Serializable
@SerializedFallback("UNKNOWN")
enum class ABC {
    A, B, UNKNOWN
}

assertEquals(ABC.UNKNOWN, Scale.decode<ABC>("C"))
```

#### `@TransientStruct` annotation

`@TransientStruct` annotation can be used on regular classes with single field to enable value-class like behavior
This is useful when you want a transient wrapper around some class but cannot use `value` modifier e.g. because you need to also override `hashCode`

```kotlin
 @Serializable
@TransientStruct
class Transient(val value: Boolean)

assertEquals(true, Scale.encode(Transient(true)))
```

#### `@AsTuple` annotation

`@AsTuple` annotation can be used on regular classes to enable encoding to and decoding from tuples.
When it is applied, serializer will threat fields declaration order to be equal to the tuple element index.
This is useful when you want to serialize a typed tuple or enum variant with anonymous associated values

```kotlin
@Serializable
@AsTuple
class AsTupleStruct(val a: Boolean, val b: Boolean)

assertEquals(listOf(true, false), Scale.encode(AsTupleStruct(true, false)))
```

---

## Binary Format (BinaryScale)

The `BinaryScale` format provides direct binary encoding/decoding following the SCALE codec specification. Unlike the dynamic `Scale` format, `BinaryScale` converts Kotlin types directly to/from SCALE-encoded byte arrays.

### Usage

1. Declare a new type that you want to convert to/from

```kotlin
@Serializable
class Test(val a: Boolean)
```

2. Use `BinaryScale` object to encode and decode instances of `Test`

```kotlin
val value = Test(a = true)
val encoded: ByteArray = BinaryScale.encodeToByteArray(value)
val decoded: Test = BinaryScale.decodeFromByteArray(encoded)

assert(value == decoded)
```

### Binary Types

#### Primitives

##### Booleans

Booleans encode to a single byte: `0x00` for `false`, `0x01` for `true`

```kotlin
val encoded = BinaryScale.encodeToByteArray(true)
assert(encoded contentEquals byteArrayOf(0x01))

val decoded = BinaryScale.decodeFromByteArray<Boolean>(byteArrayOf(0x00))
assert(decoded == false)
```

##### Numbers

All numeric types are encoded in little-endian byte order:
- `Byte` (i8): 1 byte
- `Short` (i16): 2 bytes
- `Int` (i32): 4 bytes
- `Long` (i64): 8 bytes
- `UByte` (u8): 1 byte
- `UShort` (u16): 2 bytes
- `UInt` (u32): 4 bytes
- `ULong` (u64): 8 bytes

```kotlin
@Serializable
data class Numbers(
    val s1: Byte, val s2: Short, val s3: Int, val s4: Long,
    val u1: UByte, val u2: UShort, val u3: UInt, val u4: ULong,
)

val value = Numbers(1, 2, 3, 4, 5.toUByte(), 6.toUShort(), 7.toUInt(), 8.toULong())
val encoded = BinaryScale.encodeToByteArray(value)
val decoded = BinaryScale.decodeFromByteArray<Numbers>(encoded)
assert(value == decoded)
```

`Float` and `Double` types are not supported since they have no equivalent in SCALE standard.

#### Compact Integers

`BigInteger` values are encoded using SCALE compact encoding, which provides efficient representation for integers of varying sizes.

```kotlin
val number = 100.toBigInteger()
val encoded = BinaryScale.encodeToByteArray(number)
val decoded = BinaryScale.decodeFromByteArray<BigInteger>(encoded)
assert(number == decoded)
```

When using `BigInteger` in a nested context (as a field of a class), use the `BigIntegerSerializable` typealias:

```kotlin
@Serializable
data class TestData(val a: BigIntegerSerializable)

val value = TestData(100.toBigInteger())
val encoded = BinaryScale.encodeToByteArray(value)
```

#### Strings

Strings are encoded as variable-length byte arrays with a compact-encoded length prefix followed by UTF-8 encoded bytes.

```kotlin
val value = "Test"
val encoded = BinaryScale.encodeToByteArray(value)
val decoded = BinaryScale.decodeFromByteArray<String>(encoded)
assert(value == decoded)
```

#### Byte Arrays

Byte arrays can be encoded in two ways:

##### Variable Length

By default, byte arrays are encoded with a compact-encoded length prefix followed by the bytes:

```kotlin
val value = ByteArray(25) { it.toByte() }
val encoded = BinaryScale.encodeToByteArray(value)
val decoded = BinaryScale.decodeFromByteArray<ByteArray>(encoded)
assert(value contentEquals decoded)
```

##### Fixed Length

Use the `@FixedLength` annotation to encode byte arrays without a length prefix:

```kotlin
@Serializable
class TestData(@FixedLength(20) val bytes: ByteArray)

val value = ByteArray(20) { it.toByte() }
val encoded = BinaryScale.encodeToByteArray(TestData(value))
// encoded contains exactly 20 bytes, no length prefix
```

For generic types and top-level encoding, use the wrapper classes:

```kotlin
val bytes = ByteArray(20) { 1 }
val encoded = BinaryScale.encodeToByteArray(WithLength20(bytes))
val decoded = BinaryScale.decodeFromByteArray<WithLength20<ByteArray>>(encoded)
```

Available wrapper classes: `WithLength20`, `WithLength32`, `WithLength64`

When using `ByteArray` in a nested context, you can use the `ByteArraySerializable` typealias for consistency:

```kotlin
@JvmInline
@Serializable
value class Wrapper(val a: ByteArraySerializable)
```

#### Lists

Lists can be encoded in two ways, similar to byte arrays:

##### Variable Length

By default, lists are encoded with a compact-encoded length prefix followed by the elements:

```kotlin
val value = listOf(true, false, true)
val encoded = BinaryScale.encodeToByteArray(value)
val decoded = BinaryScale.decodeFromByteArray<List<Boolean>>(encoded)
assert(value == decoded)
```

##### Fixed Length

Use the `@FixedLength` annotation to encode lists without a length prefix:

```kotlin
@Serializable
data class TestData(@FixedLength(20) val list: List<Boolean>)

val value = (0 until 20).map { true }
val encoded = BinaryScale.encodeToByteArray(TestData(value))
```

For generic types, use wrapper classes:

```kotlin
val value = (0 until 20).map { true }
val encoded = BinaryScale.encodeToByteArray(WithLength20(value))
```

#### Optional Types

Nullable types are encoded with a prefix byte indicating presence:
- `0x00` for `null`
- `0x01` followed by the encoded value for non-null values

```kotlin
val nullValue: Byte? = null
val encoded = BinaryScale.encodeToByteArray(nullValue)
assert(encoded contentEquals byteArrayOf(0x00))

val someValue: Byte? = 0x12
val encoded2 = BinaryScale.encodeToByteArray(someValue)
assert(encoded2 contentEquals byteArrayOf(0x01, 0x12))
```

##### Optional Booleans

Optional booleans use a special encoding with three states:
- `0x00` for `null`
- `0x01` for `false`
- `0x02` for `true`

```kotlin
@Serializable
data class TestData(val a: Boolean?)

val encoded1 = BinaryScale.encodeToByteArray(TestData(null))
assert(encoded1 contentEquals byteArrayOf(0x00))

val encoded2 = BinaryScale.encodeToByteArray(TestData(false))
assert(encoded2 contentEquals byteArrayOf(0x01))

val encoded3 = BinaryScale.encodeToByteArray(TestData(true))
assert(encoded3 contentEquals byteArrayOf(0x02))
```

#### Classes

Regular classes encode their fields in declaration order without field names. They should be marked with `@Serializable` to be recognized by the plugin.

```kotlin
@Serializable
data class Person(val age: Byte, val active: Boolean)

val value = Person(age = 25, active = true)
val encoded = BinaryScale.encodeToByteArray(value)
// Encoded as: [0x19, 0x01] (25 followed by true)

val decoded = BinaryScale.decodeFromByteArray<Person>(encoded)
assert(value == decoded)
```

#### Objects

Objects encode to an empty byte array since they carry no data:

```kotlin
@Serializable
object Singleton

val encoded = BinaryScale.encodeToByteArray(Singleton)
assert(encoded.isEmpty())
```

#### Value Classes

Value classes are transparent for encoding and encode to their inner values:

```kotlin
@JvmInline
@Serializable
value class UserId(val value: Int)

val userId = UserId(42)
val encoded = BinaryScale.encodeToByteArray(userId)
// Encoded same as Int(42)

val decoded = BinaryScale.decodeFromByteArray<UserId>(encoded)
assert(userId == decoded)
```

#### Enums

Regular enums encode to a single byte representing their ordinal (0-indexed position):

```kotlin
@Serializable
enum class Status {
    PENDING, ACTIVE, COMPLETED
}

val encoded = BinaryScale.encodeToByteArray(Status.ACTIVE)
assert(encoded contentEquals byteArrayOf(0x01)) // ACTIVE is at index 1
```

You can customize the index using the `@EnumIndex` annotation:

```kotlin
@Serializable
enum class Priority {
    @EnumIndex(10)
    LOW,
    @EnumIndex(20)
    MEDIUM,
    @EnumIndex(30)
    HIGH
}

val encoded = BinaryScale.encodeToByteArray(Priority.MEDIUM)
assert(encoded contentEquals byteArrayOf(0x14)) // 0x14 = 20
```

#### Sealed Classes (Discriminated Unions)

Sealed hierarchies encode as a variant index byte followed by the variant's data. Use the `@EnumIndex` annotation to specify variant indices:

```kotlin
@Serializable
sealed class Result {

    @Serializable
    @EnumIndex(0)
    object Empty : Result()

    @Serializable
    @EnumIndex(1)
    data class Value(val data: Boolean) : Result()

    @Serializable
    @EnumIndex(2)
    data class Pair(val a: Boolean, val b: Boolean) : Result()
}

// Object variant: just the index
val encoded1 = BinaryScale.encodeToByteArray<Result>(Result.Empty)
assert(encoded1 contentEquals byteArrayOf(0x00))

// Single field variant: index + field value
val encoded2 = BinaryScale.encodeToByteArray<Result>(Result.Value(true))
assert(encoded2 contentEquals byteArrayOf(0x01, 0x01))

// Multiple fields variant: index + all fields in order
val encoded3 = BinaryScale.encodeToByteArray<Result>(Result.Pair(true, false))
assert(encoded3 contentEquals byteArrayOf(0x02, 0x01, 0x00))
```

### Annotations

#### `@EnumIndex`

Specifies a custom index for enum entries or sealed class variants. Can be applied to:
- Enum entries
- Sealed class subclasses

```kotlin
@Serializable
enum class CustomEnum {
    @EnumIndex(2)
    A,
    @EnumIndex(1)
    B,
    @EnumIndex(0)
    C
}

// Encodes to their custom indices, not their ordinal positions
```

#### `@FixedLength`

Instructs the encoder/decoder to skip the length prefix for collections (byte arrays and lists). The length must be known at compile time.

```kotlin
@Serializable
data class FixedData(
    @FixedLength(32) val hash: ByteArray,
    @FixedLength(10) val items: List<Int>
)
```

For generic type parameters, use the wrapper classes `WithLength20`, `WithLength32`, or `WithLength64`.

#### Using `ByteArray` or `BigInteger` in a struct

When using `ByteArray` or `BigInteger` in a nested context (as a field of a class), you should use the following typealiases: `ByteArraySerializable` and `BigIntegerSerializable`. Otherwise the code will not compile / encoding won't work properly.