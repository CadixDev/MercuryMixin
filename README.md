MercuryMixin
============

MercuryMixin is an addon library for [Mercury](https://github.com/CadixDev/Mercury)
that remaps [Mixins](https://github.com/SpongePowered/Mixin).

## Usage

Add the `MixinRemapper` before the `MercuryRemapper`, using the same
`MappingSet`:

```java
final Mercury mercury = new Mercury();
mercury.getProcessors().add(MixinRemapper.create(mappings));
mercury.getProcessors().add(MercuryRemapper.create(mappings));
mercury.rewrite(in, out);
```
