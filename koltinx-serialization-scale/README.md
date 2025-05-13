# SCALE format support for kotlinx-serialization

This module providers support for converting between regular kotlin types and dynamic structures used by
the core substrate-sdk-android module

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

## Features

### Scale types

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
class Transient(val a: Boolean, val b: Boolean)

assertEquals(listOf(true, false), Scale.encode(Transient(true, false)))
```