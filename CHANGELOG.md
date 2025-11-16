- Improved compatibility with mods that set a custom cursor, which disables cursor changes while its active. (limited
  capability on NeoForge for now)
- Fixed cursor flickering with some mods that use custom cursors when state tracking is enabled.
- Fixed cursor flickering with some mods that use standard cursors when remap option is disabled.
- Fixed internal cursor operations sometimes creating external state if no resource pack is selected. 
- Fixed crash if another mod creates a vanilla `CursorType` with an invalid resource location name and is selected in `Window`.