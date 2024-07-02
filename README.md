## Building on a new PC

 install latest Java Development Kit (JDK) https://adoptium.net/releases.html

  .\gradlew.bat build


## Setting up a mod development environment

 .\gradlew.bat genSources
 .\gradlew.bat vscode

## running

* .\gradlew runClient

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
 .\gradlew.bat build

 jar is in `build/libs`
