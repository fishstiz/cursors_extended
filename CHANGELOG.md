- Restored compatibility workarounds and **Remap Standard Cursors** option as most mods that had their own cursor
  system aren't actually migrating to the vanilla cursor APIs introduced in 1.21.9.
    - **Fabric**: applies if GLFW isn't loaded early.
    - **NeoForge**: applies for owo-lib.
- If the cursor does not change from the system cursor, it's likely a compatibility issue.
  - **Fabric**: look out for a warning log indicating that the workarounds could not be applied due to GLFW being loaded early. 
  - **NeoForge**: report the issue for a possible workaround or patch for the incompatible mod.
  - Try **Aggressive Cursor** or **Virtual Mode** to help diagnose or bypass the issue.  