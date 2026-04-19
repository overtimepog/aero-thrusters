# Aero Thrusters

Fuel-based thrusters for [Create: Aeronautics](https://github.com/Creators-of-Aeronautics/Simulated-Project), powered by the [Sable](https://github.com/ryanhcode/sable) physics engine. Inspired by [Create: Propulsion](https://modrinth.com/mod/create-propulsion).

## Status

**v0.1** — single directional thruster block. Accepts fuel from Create: Diesel Generators, redstone-controllable, thrust scales with signal strength.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.219+
- Create 6.0.9+
- Sable 1.0.5+
- Aeronautics (optional — needed for assemblers and aviator goggles)
- Create: Diesel Generators (optional — provides fuels via `#c:fuel`)

## Building

```
./gradlew build
```

Output JAR at `build/libs/`.
