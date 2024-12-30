## Building on a new PC

 install latest Java Development Kit (JDK) https://adoptium.net/releases.html

  .\gradlew.bat build

  .\gradlew.bat vscode


## Setting up a mod development environment

 .\gradlew.bat genSources
 
## running
F5 in VS code so you can hot-reload!

alternatively:

.\gradlew runClient

## Packaging

* .\gradlew build
* jar is in `build/libs`


## Upgrading

* refer to this: https://github.com/FabricMC/fabric-example-mod/
* review and update:
  * build.gradle
  * gradle.properties
  * gradle-wrapper.properties
  * fabric.mod.json
* genSources again
 .\gradlew.bat clean
 .\gradlew.bat build
* clean vscode workspace
  "Java: Clean the Java language server workspace" from the Command Palette (Ctrl+Shift+P).

 jar is in `build/libs`
