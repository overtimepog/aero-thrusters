# Create: AeroThrusters

Directional thrusters for [Create: Aeronautics](https://github.com/Creators-of-Aeronautics/Simulated-Project), powered by the [Sable](https://github.com/ryanhcode/sable) physics engine. Inspired by [Create: Propulsion](https://modrinth.com/mod/create-propulsion).

## Status

**v0.4** — thruster block with Create-style polish:

### Polish (v0.4)
- Dedicated **Aero Thrusters** creative tab.
- Multi-element block model with a nozzle collar protruding from the front.
- Rich particle effects: bright core streaks, flame plume, sparks, smoke trail, and heat haze.
- Ponder scene for the thruster.

### Thruster
- Accepts fuel from Create: Diesel Generators (tagged `#c:fuel`).
- Redstone-controlled throttle — thrust scales linearly with signal 1–15.
- Dense exhaust particles while firing (END_ROD core, SOUL_FIRE_FLAME plume, FIREWORK sparks, LARGE_SMOKE trail).

### Shared QOL
- Wrench-rotatable and sneak-wrench pickup (via Create's `IWrenchable`).
- Aviator/engineer goggle overlay shows status, throttle, thrust in pN, and (shift) airflow.
- Hover tooltip (no goggles) shows a one-line readout.

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
