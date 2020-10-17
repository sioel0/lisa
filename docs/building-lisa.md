# Building LiSA 

LiSA comes as a gradle 6.0 project. Building LiSA boils down to executing three simple commands.

**Mac/Linux**
```bash
git clone https://github.com/UniVE-SSV/lisa.git
cd lisa/lisa
./gradlew build
```

**Windows**
```powershell
git clone https://github.com/UniVE-SSV/lisa.git
cd lisa\lisa
.\gradlew.bat build
```

The `build` task ensures that everything (from code generation to compilation, packaging and test execution) works fine. If the above commands succeed, then everyhing is set.

## Using Eclipse 

The [Gradle IDE Pack](https://marketplace.eclipse.org/content/gradle-ide-pack) plugin for Eclipse can manage all the integration between the build system and the IDE.
When installed, create the project for LiSA with `File -> Import... -> Existing Gradle project`, and select the folder where LiSA was cloned into.

The Gradle build (the same executed above with the command lines and that is executed on every commit) can be executed from the Gradle Tasks view (`Window -> Show View -> Other... -> Gradle Tasks`)
by double clicking on the `build -> build` task under the `lisa` project (if it does not appear in the list of projects of the Gradle Tasks view, click on the refresh icon in the top-right of the view itself).

**Caution**: sometimes (e.g., when adding new dependencies to the project) the plugin does not automatically refresh the Gradle configuration, and thus the build might fail
due to missing dependencies or to files not being generated. If this happens, right click on the `lisa` project inside the `Project Explorer` (`Window -> Show View -> Other... -> Project Explorer`) view 
or inside the `Package Explorer` view (`Window -> Show View -> Other... -> Package Explorer`) and select `Gradle -> Refresh Gradle project`.

