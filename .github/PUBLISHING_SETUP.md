# Publishing Setup Guide

This guide explains how to set up automated publishing to Modrinth and CurseForge for the Blueprints mod.

## Overview

The `release.yml` workflow automatically publishes your mod to:
- GitHub Releases
- Modrinth
- CurseForge

This happens whenever you push a version tag (e.g., `v1.21.10.9`).

## Prerequisites

1. GitHub repository with the mod code
2. Accounts on [Modrinth](https://modrinth.com) and [CurseForge](https://www.curseforge.com)

## Step-by-Step Setup

### 1. Create Projects on Modrinth and CurseForge

#### Modrinth:
1. Go to https://modrinth.com and sign in
2. Click "Create a project" > "Mod"
3. Fill in project details:
   - **Name**: Blueprints
   - **Summary**: Project blueprints into your world
   - **Categories**: Choose relevant categories (e.g., Utility, World Generation)
   - **Client/Server side**: Choose appropriate settings
4. Note your **Project ID** (found in the URL or project settings)

#### CurseForge:
1. Go to https://www.curseforge.com/minecraft/mc-mods and sign in
2. Click "Start a Project"
3. Fill in project details:
   - **Project Name**: Blueprints
   - **Game**: Minecraft
   - **Project Type**: Mod
4. Note your **Project ID** (numeric, found in project settings)

### 2. Generate API Tokens

#### Modrinth API Token:
1. Go to https://modrinth.com/settings/pats
2. Click "Create a PAT" (Personal Access Token)
3. Give it a name (e.g., "GitHub Actions")
4. Select scopes:
   - **Required**: `CREATE_VERSION`
   - **Optional**: `CREATE_REPORT` (for analytics)
5. Click "Create" and copy the token (you won't see it again!)

#### CurseForge API Token:
1. Go to https://www.curseforge.com/account/api-tokens
2. Click "Generate Token"
3. Give it a name (e.g., "GitHub Actions")
4. Copy the token

### 3. Add Secrets to GitHub Repository

1. Go to your GitHub repository
2. Navigate to **Settings** > **Secrets and variables** > **Actions**
3. Click **New repository secret**
4. Add the following secrets:

| Secret Name | Value |
|-------------|-------|
| `MODRINTH_TOKEN` | Your Modrinth Personal Access Token |
| `CURSEFORGE_TOKEN` | Your CurseForge API Token |

### 4. Update Workflow Configuration

Edit `.github/workflows/release.yml` and replace the placeholder project IDs:

```yaml
modrinth-id: YOUR_MODRINTH_PROJECT_ID    # Replace with your Modrinth project ID
curseforge-id: YOUR_CURSEFORGE_PROJECT_ID  # Replace with your CurseForge project ID (numeric)
```

**Example:**
```yaml
modrinth-id: abc123xyz
curseforge-id: 123456
```

### 5. Test the Workflow

#### Option A: Create a Test Tag (Recommended)

1. Update `mod_version` in `gradle.properties` to a new version (e.g., `1.21.10.9`)
2. Commit the change:
   ```bash
   git add gradle.properties
   git commit -m "Bump version to 1.21.10.9"
   ```
3. Create and push a tag:
   ```bash
   git tag v1.21.10.9
   git push origin v1.21.10.9
   ```
4. Check the workflow in **Actions** tab on GitHub

#### Option B: Manual Trigger

1. Go to **Actions** tab in your GitHub repository
2. Select "Release" workflow
3. Click "Run workflow"
4. Select your branch and click "Run workflow"

### 6. Verify Publication

After the workflow completes:

1. **GitHub**: Check the [Releases page](../../releases) for the new release
2. **Modrinth**: Visit your Modrinth project page and check the "Versions" tab
3. **CurseForge**: Visit your CurseForge project page and check the "Files" tab

## Troubleshooting

### Common Issues

**"Invalid token" error:**
- Verify the token is correctly added to GitHub Secrets
- Check that the token has the required permissions
- Regenerate the token if necessary

**"Project not found" error:**
- Verify the project IDs are correct in `release.yml`
- Ensure the projects are approved and published on the platforms

**"Version already exists" error:**
- Each version can only be published once
- Increment the version number in `gradle.properties`
- Delete the old version from the platform (if needed)

**File upload failed:**
- Check that your build produces the expected JAR file
- Verify the file pattern in the workflow matches your JAR name

### Getting Help

- [mc-publish Documentation](https://github.com/Kir-Antipov/mc-publish)
- [Modrinth API Documentation](https://docs.modrinth.com/)
- [CurseForge API Documentation](https://support.curseforge.com/en/support/solutions/articles/9000197321-curseforge-api)

## Workflow Features

The workflow automatically:
- Builds your mod with Gradle
- Creates a GitHub Release with installation instructions
- Publishes to Modrinth with proper metadata
- Publishes to CurseForge with proper metadata
- Declares dependencies (Fabric API, Fabric Loader)
- Sets the supported Minecraft version and Java version
- Includes a changelog with each release

## Customization

You can customize the workflow by editing `.github/workflows/release.yml`:

- **Version type**: Change `version-type: release` to `beta` or `alpha` for pre-releases
- **Changelog**: Modify the changelog template
- **Dependencies**: Add or modify dependencies in the `dependencies` section
- **Game versions**: Add support for multiple Minecraft versions
