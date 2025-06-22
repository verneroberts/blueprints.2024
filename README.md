# Blueprints Mod

A client-side Minecraft mod that allows you to project reference images and schematics directly into the Minecraft world. Perfect for builders who want to recreate structures from images or follow architectural plans while building.

## Download

**[Download from Modrinth](https://modrinth.com/mod/blueprints)**

## Features

- **Image Projection**: Display reference images directly in your Minecraft world
- **Full Control**: Position, scale, rotate, and adjust transparency of images
- **Per-World Organization**: Images are automatically organized by world and dimension
- **Selective Visibility**: Only visible when holding an ItemFrame in your main or offhand
- **Render Options**: Choose to render images behind blocks or always on top
- **Intuitive Controls**: Easy-to-use interface for managing and positioning blueprints

## How to Use

### Requirements
- Hold an **ItemFrame** in either your main hand or offhand to see projected images

### Getting Started
1. Press **O** (default key) to open the main config screen
2. The mod will automatically create folders for your images:
   ```
   {Minecraft}/config/blueprints/{world name}/{dimension name}
   ```
3. Add your reference images to the appropriate folder
4. Click **Reload** in the config to scan for new images

### Main Config Screen

![Blueprint selection screen](https://cdn.modrinth.com/data/cached_images/0681f87ed54eea703b07ad951976803f24be84b9.png)

**Features:**
- **Reload Button**: Rescans filesystem for newly added images
- **Path Display**: Shows the folder location for uploading images
- **Render Mode Toggle**: Choose between "Hide behind blocks" or "Always show"
- **Blueprint Grid**: Visual selection of available blueprints
- **Hide/Show**: Blueprints with dark overlay are hidden; use Ctrl+Click to toggle
- **Page Navigation**: Adjust images per page and navigate with chevron buttons

### Blueprint Positioning

![Position and adjust each blueprint](https://cdn.modrinth.com/data/cached_images/202296a42df78866f1fffb25ca343bc3658400eb.png)

Click on any blueprint to access the positioning screen with these controls:

**Movement Controls:**
- Position adjustments are player-facing aware (left is always left relative to you)
- **Shift**: Hold for larger adjustments
- **Ctrl**: Hold for precise, small adjustments  
- **Shift + Ctrl**: Hold both for maximum adjustment increments

## Demo

See the mod in action: [Building with Blueprints Demo](https://youtu.be/8ToyyT0e1bc?t=220)

---

## Development

### Prerequisites

- Install the latest [Java Development Kit (JDK)](https://adoptium.net/releases.html)

### Initial Setup

```bash
.\gradlew.bat build
.\gradlew.bat vscode
.\gradlew.bat genSources
```

### Running the Mod

**Option 1 - VS Code (Recommended):**
- Press **F5** in VS Code for hot-reload development

**Option 2 - Command Line:**
```bash
.\gradlew runClient
```

### Building for Release

```bash
.\gradlew build
```
- Built JAR will be located in `build/libs/`

### Upgrading Dependencies

When updating the mod:

1. Reference the [Fabric Example Mod](https://github.com/FabricMC/fabric-example-mod/) for latest practices
2. Update these files:
   - `build.gradle`
   - `gradle.properties` 
   - `gradle/wrapper/gradle-wrapper.properties`
   - `src/main/resources/fabric.mod.json`
3. Clean and rebuild:
   ```bash
   .\gradlew.bat clean
   .\gradlew.bat genSources
   .\gradlew.bat build
   ```
4. Clean VS Code workspace: Run "Java: Clean the Java language server workspace" from Command Palette (Ctrl+Shift+P)
