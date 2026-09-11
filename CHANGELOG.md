Stable release of **v4.2.0**:

- Migrated to SDL3
- Animated Cursors are now animated natively via SDL3. Can be reverted from the configuration screen.
- Updated the Cursor Settings screen (navigable from **Options**>**Controls**>**Mouse Settings**>**Cursor Settings**
  `without mod menu like always)
- Fixed non-vanilla Blaze3D `CursorType`s not being selected in `Window` if it doesn't have a corresponding Cursors Extended texture.

---

Changes since v4.2.0-alpha.3:

- Port to 26.3-rc-1
- Fixed hotspot sliders not updating the cursor's hotspots.
- Enabled compatibility workarounds for SDL.
- Updated English (US) translations for workaround options.
- Added restart message in config screen if changes require restart. 