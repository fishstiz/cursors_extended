- Improved compatibility with mods that set a custom cursor, which disables cursor changes while its active. (limited
  capability on NeoForge for now)
- Fixed cursor flickering with some mods that use custom cursors when state tracking is enabled.
- Fixed cursor flickering with some mods that use standard cursors when remap option is disabled.
- Fixed internal cursor operations sometimes creating external state if no resource pack is selected.
- Fixed remap option no longer working after some time if ixeris plus certain mods (owo-lib) are installed.
- Fixed crash if another mod creates a vanilla `CursorType` with an invalid resource location name and is selected in
  `Window`.
- Fixed resource pack not being detected when computing hash during initial resource load when certain mods (FancyMenu)
  are installed. **This may or may not reset your cursor settings.**