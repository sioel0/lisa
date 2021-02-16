# Building LiSA 

LiSA comes as a gradle 6.0 project. After cloning the repository with `git clone https://github.com/UniVE-SSV/lisa.git` or with any other git tool of your choice, follow one of the tutorials below to get started. 

<div class="tab">
  <button class="tablinks active" onclick="openTab(event, 'Eclipse')">Eclipse</button>
  <button class="tablinks" onclick="openTab(event, 'Windows')">Windows</button>
  <button class="tablinks" onclick="openTab(event, 'Linux')">Mac/Linux</button>
</div>

<hr/>

<div id="Eclipse" class="tabcontent active">
	<b>Note</b>: make sure that the <a href="https://marketplace.eclipse.org/content/gradle-ide-pack">Gradle IDE Pack</a> plugin for Eclipse is installed.
	<br><br>
	After cloning the repository through <code>git clone</code>, import the LiSA project with <code>File -> Import... -> Existing Gradle project</code>, and select the <code>lisa</code> folder inside the cloned repository (<i>not</i> the repository root) and click on <code>Finish</code>.
	<br><br>
	The Gradle build can be executed from the Gradle Tasks view (<code>Window -> Show View -> Other... -> Gradle Tasks</code>) by double clicking on the <code>build -> build</code> task under the <code>lisa</code> project (if it does not appear in the list of projects of the Gradle Tasks view, click on the refresh icon in the top-right of the view itself).
	<br><br>
	<b>Caution 1</b>: Eclipse will signal compiler errors when the project is first imported. This is normal, since the antlr-generated classes that are used for testing will not be present. After running the gradle build a first time, the Eclipse configuration needs to be updated: right click on the <code>lisa</code> project inside the Project Explorer view (<code>Window -> Show View -> Other... -> Project Explorer</code>) or inside the Package Explorer view (<code>Window -> Show View -> Other... -> Package Explorer</code>) and select <code>Build Path -> Configure Build Path...</code>. In the dialog that opens up, six folders must be visible inside the <code>Source</code> tab: add the missing one through the <code>Add Folder...</code> button, and then edit the contents of each one's output folder and contains test sources accordingly to the following table.
	<br><br>
	<table>
		<tr><th>Folder</th><th>Output folder</th><th>Contains test sources</th></tr>
		<tr><td><code>lisa/src/main/java</code></td><td><code>lisa/bin/main</code></td><td>No</td></tr>
		<tr><td><code>lisa/src/main/resources</code></td><td><code>lisa/bin/main</code></td><td>No</td></tr>
		<tr><td><code>lisa/src/test/java</code></td><td><code>lisa/bin/test</code></td><td>Yes</td></tr>
		<tr><td><code>lisa/src/test/resources</code></td><td><code>lisa/bin/test</code></td><td>Yes</td></tr>
		<tr><td><code>lisa/src/test/antlr</code></td><td><code>lisa/bin/test</code></td><td>Yes</td></tr>
		<tr><td><code>lisa/build/generated-src/antlr/test</code></td><td><code>lisa/bin/test</code></td><td>Yes</td></tr>
	</table>
	<br>
	<b>Caution 2</b>: sometimes (e.g., when adding new dependencies to the project) the plugin does not automatically refresh the Gradle configuration, and thus the build might fail due to missing dependencies or to files not being generated. If this happens, right click on the <code>lisa</code> project inside the Project Explorer view (<code>Window -> Show View -> Other... -> Project Explorer</code>) or inside the Package Explorer view (<code>Window -> Show View -> Other... -> Package Explorer</code>) and select <code>Gradle -> Refresh Gradle project</code>.
</div>

<div id="Windows" class="tabcontent">
	Open a terminal window inside the folder where you cloned the repository, and then execute:
	<pre class="highlight"><code>cd lisa
.\gradlew.bat build</code></pre>
</div>

<div id="Linux" class="tabcontent">
	Open a terminal window inside the folder where you cloned the repository, and then execute:
	<pre class="highlight"><code>cd lisa
./gradlew build</code></pre>
</div>

<hr/>

The `build` task ensures that everything (from code generation to compilation, packaging and test execution) works fine. If the above commands succeed, then everthing is set.
