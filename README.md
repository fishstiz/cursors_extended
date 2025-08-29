# Cursors Extended

Replaces the boring old default system cursors with Minecraft-themed cursors.

<img width="32" height="32" alt="default" src="https://github.com/user-attachments/assets/44f5a884-92a1-4e4b-a028-c645e6075845" />
<img width="32" height="32" alt="pointing_hand" src="https://github.com/user-attachments/assets/a2d150db-55c3-4a90-aaef-daffb07d84de" />
<img width="32" height="32" alt="grabbing" src="https://github.com/user-attachments/assets/91b2b145-028a-498d-8807-07c47da2492c" />
<img width="32" height="32" alt="ibeam" src="https://github.com/user-attachments/assets/f0cd2de5-64b2-4d97-9c6a-896782bf2ac2" />
<img width="32" height="32" alt="shift" src="https://github.com/user-attachments/assets/bfe3faf3-a8b0-4106-a586-ebe9106efe9c" />
<img width="32" height="32" alt="busy" src="https://github.com/user-attachments/assets/279f3e17-d417-46a0-981e-d06459daad83" />
<img width="32" height="32" alt="not_allowed" src="https://github.com/user-attachments/assets/b409ae2a-f559-4880-8d37-3cf0eacfec84" />
<img width="32" height="32" alt="crosshair" src="https://github.com/user-attachments/assets/b11e9b70-2fb5-4b24-b636-966b882601e2" />
<img width="40" height="40" alt="resize_all" src="https://github.com/user-attachments/assets/3298d914-ab62-4e9e-992d-c195d2f1ec98" />
<img width="40" height="40" alt="resize_ew" src="https://github.com/user-attachments/assets/6d913c1d-cbef-44a3-bb3b-840126df333a" />
<img width="40" height="40" alt="resize_ns" src="https://github.com/user-attachments/assets/ef98d600-9090-4ee8-9bf0-d64ebe0faedf" />
<img width="40" height="40" alt="resize_nwse" src="https://github.com/user-attachments/assets/785e3721-11c3-4d5f-b97b-2ee473a1ae28" />
<img width="40" height="40" alt="resize_nesw" src="https://github.com/user-attachments/assets/546dde7a-7aff-4d26-a8d5-46889b96e620" />

<br>

Once called [Minecraft Cursor](https://github.com/fishstiz/minecraft-cursor).
Snapshot 25w35a of Minecraft introduced new cursor shapes, prompting major breaking changes for the mod.

## 📌 Requirements

- Minecraft >25w35a
- When on Fabric: Fabric API

## ✨ Features

- Custom & animated textures for each cursor shape through resource packs.
- Configurable custom textures. Allows for toggling cursors, and changing the hotspot and scale.
    - Navigable from **Mouse Settings... > Cursor Settings...** or from Mod Menu (if installed)
- Additional cursor shapes and mappings with **Legacy Mode** and **Adaptive Cursor**.

![cursors_extended](https://github.com/user-attachments/assets/79d6643d-7997-4902-aed5-840e28a7826b)

## 🎨 Creating Resource Packs

- Sample Resource
  Pack: https://github.com/fishstiz/cursors_extended/tree/master/common/src/main/resources/resourcepacks/legacy

## 🔗 Adding Compatibility

- Mods can use **`CursorType`** (vanilla class) to create custom cursors. When submitted to `GuiGraphics`/`DrawContext`,
  Cursors Extended will try to load a texture matching the cursor's name. If none is found, it falls back to the
  cursor's `#select` method.
- You can also create other **standard cursors** using `CursorType#createStandardCursor`, if it's not already provided.
  They'll automatically be registered to Cursors Extended.
- As long as you use `CursorType`s and use it conventionally within Minecraft, it should be compatible.