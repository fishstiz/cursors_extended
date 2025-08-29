Snapshot 25w35a introduced new cursor shapes. While this does not invalidate the features of this mod, it warranted
significant breaking changes.

- Renamed from Minecraft Cursor to [**Cursors Extended**](https://github.com/fishstiz/cursors_extended) (official rename
  on stable release)
- Updated the resource pack structure. Sample provided below.
- Updated built-in cursor textures.
- Added built-in resource pack for legacy textures.
- Added **Legacy Cursor Mode** under compatibility to enable the old cursor detection and other cursor mappings (enabled
  by default).
- Added Adaptive Option to toggle Scrollbar Pointing Hand cursor and Resize NS cursor.
- Added Ukrainian Translation. ([#39](https://github.com/fishstiz/minecraft-cursor/pull/39) by StarmanMine142)
- Removed a bunch of options.
- Removed the Java API as it's no longer needed, and discontinued the experimental JSON API due to no demand.
- Added new way to create cursor textures when **Cursors Extended** is loaded without the need for a Java
  API: https://github.com/fishstiz/cursors_extended/blob/master/README.md#-adding-compatibility

Now that cursor types are official, hopefully more mods consider compatibility and use the vanilla methods.

Creating documentation for Resource Packs soon, but it's mostly the same, just create and move files
around: https://github.com/fishstiz/cursors_extended/tree/master/common/src/main/resources/resourcepacks/legacy

Report any issues here: https://github.com/fishstiz/cursors_extended