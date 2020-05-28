MercuryMixin
============

MercuryMixin is a [Mercury] processor designed to remap [Mixin]s. MercuryMixin
is licensed under the [Mozilla Public License 2.0](./LICENSE.txt).

## Usage

MercuryMixin revolves around the `MixinRemapper` class, which is designed to
be used in conjunction with `MercuryRemapper`.

```java
final Mercury mercury = new Mercury();

// MixinRemapper does not intend to replicate what MercuryRemapper does, and
// will instead populate the MappingSet to be applied by MercuryRemapper.
mercury.getProcessors().add(MixinRemapper.create(mappings));
mercury.getProcessors().add(MercuryRemapper.create(mappings));

mercury.rewrite(in, out);
```

### Enforce proper usage of @Mutable and @Final

MercuryMixin has an included "Cleaner" processor, added by request of The
Sponge Team, which effectively just enforces clean use of the Mixin library:

- Using `@Final` where necessary, and removing where not.
- Using `@Mutable` where necessary, and removing where not.

```java
final Mercury mercury = new Mercury();
mercury.getProcessors().add(MixinCleaner.create());
mercury.rewrite(in, out);
```

## Discuss

**Found an issue with Mercury?** [Make an issue]! We'd rather close invalid
reports than have bugs go unreported :)

We have an IRC channel on [EsperNet], `#cadix`, which is available for all
[registered](https://esper.net/getting_started.php#registration) users to join
and discuss Mercury and other Cadix projects.

[Mercury]: https://github.com/CadixDev/Mercury
[Mixin]: https://github.com/SpongePowered/Mixin
[Make an issue]: https://github.com/CadixDev/MercuryMixin/issues/new
[EsperNet]: https://esper.net/
