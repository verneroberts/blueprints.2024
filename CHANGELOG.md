# Changelog

All notable changes to the Blueprints mod will be documented in this file.

## [26.2.12] - 2026-07-03

### Added
- Block grid overlay per blueprint: cycle Off / Dark / Light lines, with configurable spacing in blocks (pairs with calibration for block counting)
- Flip/mirror toggles per blueprint (horizontal and vertical)
- Drag and drop image files onto the config screen to import them into the current world's blueprint folder
- Search box in the config screen to filter blueprints by filename

### Fixed
- Current page no longer resets when clicking buttons on the config screen

### Changed
- Upgraded to Minecraft 26.2
- Updated Fabric Loader to 0.19.3 and Fabric API to 0.154.0+26.2
- Ported to the 26.2 rendering and GUI APIs (PrimitiveTopology, GpuBufferSlice mapping, Gui.setScreen, GameRenderer.mainRenderTarget)

## [1.21.11.10] - 2026-02-27

### Fixed
- Images rendering through blocks in "Render visible" mode (issue #24)
  - Switched render-visible mode to a direct GPU render pass with explicit DynamicTransforms UBO binding
  - Registered a custom pipeline (POSITION_TEX_COLOR_SNIPPET base) with depth testing, depth write, and face culling disabled

## [1.21.10.9] - 2025-10-12

### Added
- Automated publishing to Modrinth and CurseForge via GitHub Actions
- CHANGELOG.md for version history tracking

### Changed
- Mod is now properly marked as client-side only

## [1.21.10.8] - 2025-10-12

### Added
- Calibration feature to resize images based on known dimensions

### Fixed
- Crash due to mixin bindings

## [1.21.10.7] - 2025-10-11

### Changed
- Upgraded to Minecraft 1.21.10

## [1.21.9.7] - 2025-10-11

### Changed
- Upgraded to Minecraft 1.21.9

## [1.21.8.7] - 2025-08-17

### Changed
- Upgraded to Minecraft 1.21.8

## [1.21.7.7] - 2025-08-17

### Changed
- Upgraded to Minecraft 1.21.7

## [1.21.6.7] - 2025-06-22

### Added
- Blueprints now render in order from furthest away from the player to closest

### Changed
- Updated to Minecraft 1.21.6

## [1.21.5.7] - 2025-06-02

### Added
- Support for many more image types
- Experimental support for animated GIFs

### Changed
- Updated to Minecraft 1.21.5

### Known Issues
- Some GIFs may distort slightly while animating

## [1.21.4.6] - 2024-12-30

### Changed
- Updated to Minecraft 1.21.4

## [1.21.0.6] - 2024-09-01

### Added
- Overhauled configuration screen with hover-to-scroll functionality
- In-world push/pull buttons that move the last configured blueprint based on player facing direction
- Keyboard shortcuts for push/pull (configurable in settings)

### Changed
- Position/rotation/scale boxes now support scroll wheel for value changes
- Ctrl/Shift/Both modifiers change scroll rate

## [1.21.0.5] - 2024-08-19

### Added
- Blueprints are now always visible from both sides

### Changed
- Overhauled blueprint selection screen to a grid layout with paging

## [1.21.0.4] - 2024-08-18

### Added
- New push and pull buttons for individual images on positioning screen
- Scrollable image list component

### Changed
- Disabled background blur effect on positioning screen

## [1.21.0.3] - 2024-07-07

### Changed
- Images now render in their original aspect ratio

## [1.20.1.2] - 2024-03-29

### Fixed
- Crash when used with ReplayMod viewer

---

## How to Update This File

When preparing a new release:

1. Add a new version section at the top (below this comment block)
2. Use the format: `## [version] - YYYY-MM-DD`
3. Organize changes into categories:
   - **Added** - New features
   - **Changed** - Changes to existing functionality
   - **Deprecated** - Soon-to-be removed features
   - **Removed** - Removed features
   - **Fixed** - Bug fixes
   - **Security** - Security fixes

Example:
```markdown
## [1.21.10.9] - 2025-10-15

### Added
- New blueprint template system
- Support for custom block palettes

### Fixed
- Fixed crash when placing large blueprints
```
