
# IndustrialCraft2: Refactored

<img src="https://img.shields.io/badge/Minecraft-1.20.1-brightgreen" alt="Minecraft 1.20.1">
<img src="https://img.shields.io/badge/Forge-47.4.20-orange" alt="Forge 47.4.20">
<img src="https://img.shields.io/badge/Version-2.10.22--ex120-blue" alt="Version 2.10.22-ex120">
<img src="https://img.shields.io/badge/License-ARR-red" alt="License ARR">

[中文 Readme](./README.md)

Decompiled from the official build `2.9.40-ex119`, with missing and broken functionality backported from `2.8.222-ex112`.

## Preface

IC2 is dead. No official responses. No source release. The would-be successors have gone silent for over a year. The IC2 Dev Team even made their position clear: **the source will never be opened**. Ever.

And yet the community never stopped asking. Players have been waiting -- for a high-version IC2 that actually feels like IC2.

Someone did step up. [IC2CR](https://github.com/yu1745/ic2-fabric) went open-source first, targeting 1.20.1 Fabric, with no small fanfare. But look closer: mechanics changed, values changed, UI changed -- and much of the port was, frankly, delegated to AI. Functional enough, perhaps. But that's not IC2. That's something else wearing IC2's name.

We took a different approach. Our goal was exactly one thing: **port IC2 to 1.20.1, completely, without touching a single original mechanic or value.**

We did it. What you're getting is a complete, playable IC2 on 1.20.1 Forge / NeoForge — the one you remember.

## Differences from the Original

We know why you're here, so we kept changes to an absolute minimum. Here is every deviation from the original, in full:

- **Removed the item-form UU Matter** (`uu_matter`, the pink slimeball) and its associated downstream machines. The IC2 Dev Team had already removed the Mass Fabricator (`mass_fabricator`) recipe back in `2.8.222-ex112`, making the leftover UU Matter duplication functionality vestigial and pointless.

- **Removed Refined Iron Ingot** (`refined_iron_ingot`). This is IC2 Classic's item -- a steel ingot substitute that has no place in mainline IC2. We don't need the substitute.

- **Adjusted ore generation parameters for tin, lead, and uranium** (relative to `2.9.40-ex119`). Vanilla IC2 biases tin toward mountain biomes, but players demand tin far more than lead and tend to dig deep rather than climb high. We redistributed tin's vertical spread while keeping its total abundance slightly above lead's, resulting in a more sensible gameplay pace.
- **Adjusted the wireless charging behavior of the charge pad**. The original charge pad could only charge items on the player that were at or below the pad's voltage tier, and couldn't charge items above the pad's voltage tier at all. We changed it so that the station can charge any item on the player, but the charging speed is still limited by the station's voltage tier.


## Copyright Statement

The repository is licensed as All Rights Reserved / No License. The copyright of this project belongs to the original IC2 Dev Team. We do not own the copyright of IC2's code and resource files, nor do we claim to own the copyright of IC2's code and resource files. We are simply decompiling, fixing, porting, and maintaining IC2.

## Quick Start

Please use Java 17 to build and compile this branch.

Other information, please turn to [Release](./release.md)。

```shell
gradlew build
gradlew runClient
gradlew runServer
```
