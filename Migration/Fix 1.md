# Stage 01

可穿戴材质装作物品时，表现正常。穿戴的材质会变成紫黑。

受影响物品的注册键：

*代表helmet, chestplate, leggings, boots

alloy_chestplate
bronze_*
cf_pack
hazmat_*
jetpack
nano_*
quantum_*
robber_boots
night_vision_goggles

# Stage 02

右键生成船，闪退。

崩溃报告：

[086月2026 09:34:45.327] [Server thread/ERROR] [net.minecraft.server.MinecraftServer/]: Encountered an unexpected exception
net.minecraft.ReportedException: Ticking entity
at net.minecraft.server.MinecraftServer.tickChildren(MinecraftServer.java:870) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.MinecraftServer.tickServer(MinecraftServer.java:806) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.client.server.IntegratedServer.tickServer(IntegratedServer.java:84) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.MinecraftServer.runServer(MinecraftServer.java:654) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.MinecraftServer.lambda$spin$2(MinecraftServer.java:244) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at java.lang.Thread.run(Thread.java:840) ~[?:?]
Caused by: java.lang.RuntimeException: java.lang.NoSuchFieldException: lastLocation
at ic2.api.entity.boat.AbstractBoatEntity.tick(AbstractBoatEntity.java:402) ~[%23194!/:?]
at net.minecraft.server.level.ServerLevel.tickNonPassenger(ServerLevel.java:659) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.world.level.Level.guardEntityTick(Level.java:457) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.level.ServerLevel.lambda$tick$3(ServerLevel.java:323) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.world.level.entity.EntityTickList.forEach(EntityTickList.java:53) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.level.ServerLevel.tick(ServerLevel.java:303) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.MinecraftServer.tickChildren(MinecraftServer.java:866) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
... 5 more
Caused by: java.lang.NoSuchFieldException: lastLocation
at java.lang.Class.getDeclaredField(Class.java:2612) ~[?:?]
at ic2.api.entity.boat.AbstractBoatEntity.tick(AbstractBoatEntity.java:286) ~[%23194!/:?]
at net.minecraft.server.level.ServerLevel.tickNonPassenger(ServerLevel.java:659) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.world.level.Level.guardEntityTick(Level.java:457) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.level.ServerLevel.lambda$tick$3(ServerLevel.java:323) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.world.level.entity.EntityTickList.forEach(EntityTickList.java:53) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.level.ServerLevel.tick(ServerLevel.java:303) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
at net.minecraft.server.MinecraftServer.tickChildren(MinecraftServer.java:866) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar%23189!/:?]
... 5 more
[086月2026 09:34:45.537] [Server thread/FATAL] [net.minecraftforge.common.ForgeMod/]: Preparing crash report with UUID dac1838e-423f-498a-8659-bf1ee8e73a5c
[086月2026 09:34:45.538] [Server thread/ERROR] [net.minecraft.server.MinecraftServer/]: This crash report has been saved to: D:\Files\Codes\JavaSources\ic2\run\.\crash-reports\crash-2026-06-08_09.34.45-server.txt
[086月2026 09:34:45.539] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Stopping server
[086月2026 09:34:45.539] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving players
[086月2026 09:34:45.555] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving worlds
[086月2026 09:34:45.892] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[新的世界]'/minecraft:overworld
[086月2026 09:34:46.546] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[新的世界]'/minecraft:the_nether
[086月2026 09:34:46.548] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[新的世界]'/minecraft:the_end
[086月2026 09:34:46.558] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (新的世界): All chunks are saved
[086月2026 09:34:46.558] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[086月2026 09:34:46.558] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[086月2026 09:34:46.558] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage: All dimensions are saved
[086月2026 09:34:47.044] [Render thread/ERROR] [net.minecraft.client.Minecraft/FATAL]: Reported exception thrown!
net.minecraft.ReportedException: Ticking entity
at net.minecraft.world.level.Level.guardEntityTick(Level.java:466) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.client.multiplayer.ClientLevel.lambda$tickEntities$4(ClientLevel.java:251) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.world.level.entity.EntityTickList.forEach(EntityTickList.java:53) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.client.multiplayer.ClientLevel.tickEntities(ClientLevel.java:249) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.client.Minecraft.tick(Minecraft.java:1791) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.client.Minecraft.runTick(Minecraft.java:1078) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.client.Minecraft.run(Minecraft.java:700) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.client.main.Main.run(Main.java:212) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.client.main.Main.main(Main.java:51) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[?:?]
at jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[?:?]
at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:?]
at java.lang.reflect.Method.invoke(Method.java:569) ~[?:?]
at net.minecraftforge.fml.loading.targets.ForgeClientUserdevLaunchHandler.lambda$launchService$0(ForgeClientUserdevLaunchHandler.java:25) ~[fmlloader-1.19.2-43.5.2.jar:?]
at cpw.mods.modlauncher.LaunchServiceHandlerDecorator.launch(LaunchServiceHandlerDecorator.java:30) ~[modlauncher-10.0.9.jar:?]
at cpw.mods.modlauncher.LaunchServiceHandler.launch(LaunchServiceHandler.java:53) ~[modlauncher-10.0.9.jar:?]
at cpw.mods.modlauncher.LaunchServiceHandler.launch(LaunchServiceHandler.java:71) ~[modlauncher-10.0.9.jar:?]
at cpw.mods.modlauncher.Launcher.run(Launcher.java:108) ~[modlauncher-10.0.9.jar:?]
at cpw.mods.modlauncher.Launcher.main(Launcher.java:78) ~[modlauncher-10.0.9.jar:?]
at cpw.mods.modlauncher.BootstrapLaunchConsumer.accept(BootstrapLaunchConsumer.java:26) ~[modlauncher-10.0.9.jar:?]
at cpw.mods.modlauncher.BootstrapLaunchConsumer.accept(BootstrapLaunchConsumer.java:23) ~[modlauncher-10.0.9.jar:?]
at cpw.mods.bootstraplauncher.BootstrapLauncher.main(BootstrapLauncher.java:141) ~[bootstraplauncher-1.1.2.jar:?]
Caused by: java.lang.RuntimeException: java.lang.NoSuchFieldException: lastLocation
at ic2.api.entity.boat.AbstractBoatEntity.tick(AbstractBoatEntity.java:402) ~[main/:?]
at net.minecraft.client.multiplayer.ClientLevel.tickNonPassenger(ClientLevel.java:269) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.world.level.Level.guardEntityTick(Level.java:457) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
... 21 more
Caused by: java.lang.NoSuchFieldException: lastLocation
at java.lang.Class.getDeclaredField(Class.java:2612) ~[?:?]
at ic2.api.entity.boat.AbstractBoatEntity.tick(AbstractBoatEntity.java:286) ~[main/:?]
at net.minecraft.client.multiplayer.ClientLevel.tickNonPassenger(ClientLevel.java:269) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
at net.minecraft.world.level.Level.guardEntityTick(Level.java:457) ~[forge-1.19.2-43.5.2_mapped_official_1.19.2-recomp.jar:?]
... 21 more

存档损坏，无法加载。