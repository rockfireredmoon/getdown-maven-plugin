# Getdown Maven Plugin

A Maven plugin for the brilliant [Getdown](https://github.com/threerings/getdown/). Parts of it were loosely
based on [Webstart Maven Plugin](http://mojo.codehaus.org/webstart/webstart-maven-plugin/),

## Features

* Gathers dependencies
* Generates your manifest
* Generates your update directory
* Generates your stubs
* Generates your applet directory (not yet complete)
* Signs stub for applets

## Limitations

* Doesn't yet handle Versioning.
* Doesn't yet handle Auxiliary Resources
* Doesn't yet handle Platform-specific Configuration
* Doesn't yet handle Alternative Entry Points
* Doesn't yet handle Custom Java Installation

## Goals

* `updates` Generates update directory
* `stubs` Generates stubs directory for building installers
* `applet` Generates applet template directory (with signed getdown jar)

## Usage

Add the following to your pom.xml. For now the SNAPSHOT plugin is hosted on Github itself
(using the instructions found here https://engineering.groupon.com/2014/misc/maven-and-github-forking-a-github-repository-and-hosting-its-maven-artifacts/) :-

```
	<pluginRepositories>
		<pluginRepository>
			<id>getdown-maven-plugin-mvn-repo</id>
			<url>https://raw.github.com/rockfireredmoon/getdown-maven-plugin/mvn-repo/</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</pluginRepository>
	</pluginRepositories>
```

And add the plugin configuration and bind it to the package phase.

```
	<plugin>
		<groupId>org.icestuff</groupId>
		<artifactId>getdown-maven-plugin</artifactId>
		<version>0.0.1-SNAPSHOT</version>
		<executions>
			<execution>
				<phase>package</phase>
				<goals>
					<goal>updates</goal>
				</goals>
				<configuration>
					<!-- This is the minimum required configuration, see below for more -->
					<appbase>http://myserver.com/myapp/getdown</appbase>	
					<mainClass>org.icestuff.getdown.maven.examples.MyApp</mainClass>			
				</configuration>
			</execution>
		</executions>
	</plugin>
```

Then run the package phase :-

```
mvn package
```

This will produce your updates directory in *target/getdown*.

## Configuration

### Basic

| Key                    | Default | Descriptions |
| --- | --- | --- |
| appbase | Required | The HTTP URL of the update directory. | 
| libPath | Root of appbase | The subdirectory under appbase under which the jar files will be stored. | 
| mainClass | Required | The Java class name that contains the `main(String[] args)` method. |
| outputJarVersions | false | Whether to include version numbers in the file names created. |
| verbose | false | Be verbose about the build. |
| appargs | None | List of **apparg** elements, each one an argument to pass to the *mainClass* when launched |
| jvmargs | None | List of **jvmarg** elements, each one an argument to pass to the JVM when launched |
| ignoreMissingMain | false | If the plugin is added to a project of type *pom*, it may fail to find the mainClass, although this won't necessarily prevent your app from running. Set this to true to ignore this error.|
| workDirectory | ${project.build.directory}/getdown | Location where update files end up. |
| excludeTransitive | false | Whether to exclude transitive dependencies. | 
| allowOffline | false | Whether the getdown launcher will allow offline usage. |
| resources | None | List of **resource** elements, each one a path to an additional resource to include. |
| uresources | None | List of **uresource** elements, each one a path to an additional resource to include that should be unpacked. |
  

### UI

Configuration of the download / launch UI.

| Key                    | Default | Descriptions |
| --- | --- | --- |
| name | ${project.name} | The name displayed. |
| icons | None | List of **icon** elements that each contain a path to an icon file. |
| progressImage | None | Path to the progress bar image. |
| backgroundImage | None | Path to the background image. |
| progress | None | Bounding box of progress. |
| progressText | None | Progress text color. |
| status | None | Bounding box of status. |
| statusText | None | Status text color. |
| textShadow | None | Shadow color. |
| errorBackground | None | Path to image for error background. |
| macDockIcon | None | Path to image for Mac dock icon. |
| installError | None | Install error URL. |

## Examples

Here are some complete example projects. You can find the full source for these at [getdown-maven-example1](https://github.com/rockfireredmoon/getdown-maven-example1)
and  [getdown-maven-example2](https://github.com/rockfireredmoon/getdown-maven-example2)  

### Example 1

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<artifactId>getdown-maven-example1</artifactId>
	<name>Getdown Maven Example 1</name>
	<description>Simples Example POM, builds update directory and manifest</description>
	<groupId>org.icestuff</groupId>
	<version>0.0.1-SNAPSHOT</version>
	<build>
		<plugins>
			<plugin>
				<groupId>org.icestuff</groupId>
				<artifactId>getdown-maven-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>updates</goal>
						</goals>
						<configuration>
							<appbase>http://myserver.com/myapp/getdown</appbase>
							<libPath>lib</libPath>
							<mainClass>org.icestuff.getdown.maven.examples.MyApp</mainClass>
							<outputJarVersions>false</outputJarVersions>
							<verbose>true</verbose>
							<appargs>
								<apparg>Some Argument</apparg>
							</appargs>
							<ui>
								<name>My App</name>
								<icons>
									<icon>${basedir}/src/main/images/myapp.png</icon>
								</icons>
								<progressImage>${basedir}/src/main/images/progress.png</progressImage>
								<backgroundImage>${basedir}/src/main/images/splash.png</backgroundImage>
								<progress>80, 244, 196, 11</progress>
								<progressText>FFFFFF</progressText>
								<statusText>FFFFFF</statusText>
								<status>20, 170, 316, 64</status>
								<textShadow>111111</textShadow>
							</ui>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
	<pluginRepositories>
		<pluginRepository>
			<id>getdown-maven-plugin-mvn-repo</id>
			<url>https://raw.github.com/rockfireredmoon/getdown-maven-plugin/mvn-repo/</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</pluginRepository>
	</pluginRepositories>
</project>
```

### Example 2

The following example uses some other Maven plugins to spit out stub installers for Linux, Mac OSX and Windows along with the update directory.

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<artifactId>getdown-maven-example2</artifactId>
	<name>Getdown Maven Example 2</name>
	<description>Advanced Example POM that builds update directory and native installers for Linux, Mac OS X and Windows</description>
	<groupId>org.icestuff</groupId>
	<version>0.0.1-SNAPSHOT</version>
	<build>

		<plugins>
			<!-- Create the update directory, stubs etc -->
			<plugin>
				<groupId>org.icestuff</groupId>
				<artifactId>getdown-maven-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>updates</goal>
							<goal>stub</goal>
							<goal>applet</goal>
						</goals>
						<configuration>
							<appbase>http://www.myserver.com/myapp/getdown/</appbase>
							<libPath>lib</libPath>
							<mainClass>org.icestuff.getdown.maven.examples.MyApp</mainClass>
							<outputJarVersions>false</outputJarVersions>
							<verbose>true</verbose>
							<appargs>
								<apparg>Some Argument</apparg>
							</appargs>
							<ui>
								<name>My App</name>
								<icons>
									<icon>${basedir}/src/main/images/myapp.png</icon>
								</icons>
								<progressImage>${basedir}/src/main/images/progress.png</progressImage>
								<backgroundImage>${basedir}/src/main/images/splash.png</backgroundImage>
								<progress>80, 244, 196, 11</progress>
								<progressText>FFFFFF</progressText>
								<statusText>FFFFFF</statusText>
								<status>20, 170, 316, 64</status>
								<textShadow>111111</textShadow>
							</ui>
						</configuration>
					</execution>
				</executions>
			</plugin>


			<!-- Now from the stub we create some platform specific installers to 
				give the user shortcuts etc. For Linux, because apps will be installed in 
				root paths, the launch script actually links getdown to the users home directory 
				and runs it from there. This lets all users share the shortcut, but have 
				their own cache.. -->

			<!-- First a Deb for Debian and based systems such as Ubuntu, Mint -->

			<plugin>
				<artifactId>jdeb</artifactId>
				<groupId>org.vafer</groupId>
				<version>1.2</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>jdeb</goal>
						</goals>
						<configuration>
						</configuration>
					</execution>
				</executions>
				<dependencies>
				</dependencies>
				<configuration>
					<controlDir>src/main/deb/control</controlDir>
					<skipPOMs>false</skipPOMs>
					<installDir>/usr/lib/myapp</installDir>
					<deb>${project.basedir}/target/myapp.deb</deb>
					<dataSet>
						<data>
							<type>directory</type>
							<src>target/getdown-stub</src>
							<includes>*.*</includes>
							<mapper>
								<type>perm</type>
								<strip>1</strip>
								<prefix>/usr/lib/myapp</prefix>
								<user>root</user>
								<group>root</group>
								<filemode>755</filemode>
							</mapper>
						</data>
						<data>
							<type>directory</type>
							<src>${basedir}/src/main/scripts</src>
							<includes>*</includes>
							<mapper>
								<type>perm</type>
								<strip>1</strip>
								<prefix>/usr/bin</prefix>
								<user>root</user>
								<group>root</group>
								<filemode>755</filemode>
							</mapper>
						</data>
						<data>
							<type>directory</type>
							<src>${basedir}/src/main/images</src>
							<includes>myapp.png</includes>
							<mapper>
								<type>perm</type>
								<strip>1</strip>
								<prefix>/usr/share/pixmaps</prefix>
								<user>root</user>
								<group>root</group>
								<filemode>755</filemode>
							</mapper>
						</data>
						<data>
							<type>directory</type>
							<src>${basedir}/src/main/applications</src>
							<includes>*</includes>
							<mapper>
								<type>perm</type>
								<strip>1</strip>
								<prefix>/usr/share/applications</prefix>
								<user>root</user>
								<group>root</group>
								<filemode>755</filemode>
							</mapper>
						</data>
					</dataSet>
				</configuration>
			</plugin>

			<!-- Now an RPM -->

			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>rpm-maven-plugin</artifactId>
				<version>2.1-alpha-4</version>
				<extensions>true</extensions>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>attached-rpm</goal>
						</goals>
						<configuration>
						</configuration>
					</execution>
				</executions>
				<configuration>
					<sourceEncoding>UTF-8</sourceEncoding>
					<name>myapp</name>
					<group>Application/Internet</group>
					<packager>Some Person</packager>
					<copyright>2014 Some Person</copyright><!-- <projVersion></projVersion> --><!-- <release>0</release> -->
					<prefix>/usr</prefix>
					<defineStatements>
						<defineStatement>_unpackaged_files_terminate_build 0</defineStatement>
					</defineStatements>

					<requires>
						<require>java &gt;= 1.7.0</require>
					</requires>
					<mappings>
						<mapping>
							<directory>/usr/bin</directory>
							<filemode>755</filemode>
							<username>root</username>
							<groupname>root</groupname>
							<directoryIncluded>false</directoryIncluded>
							<sources>
								<source>
									<location>${basedir}/src/main/scripts/myapp</location>
								</source>
							</sources>
						</mapping>
						<mapping>
							<directoryIncluded>false</directoryIncluded>
							<directory>/usr/share/applications</directory>
							<filemode>755</filemode>
							<username>root</username>
							<groupname>root</groupname>
							<sources>
								<source>
									<location>${basedir}/src/main/applications/myapp.desktop</location>
								</source>
							</sources>
						</mapping>
						<mapping>
							<directoryIncluded>false</directoryIncluded>
							<directory>/usr/share/pixmaps</directory>
							<filemode>755</filemode>
							<username>root</username>
							<groupname>root</groupname>
							<sources>
								<source>
									<location>${basedir}/src/main/images/myapp.png</location>
								</source>
							</sources>
						</mapping>

						<mapping>
							<directory>/usr/lib/myapp</directory>
							<filemode>755</filemode>
							<username>root</username>
							<groupname>root</groupname>
							<sources>
								<source>
									<location>${basedir}/target/getdown-stub</location>
									<includes>
										<include>**/*</include>
									</includes>
								</source>
							</sources>
						</mapping>
					</mappings>
				</configuration>

			</plugin>

			<!-- The RPM is output in the rpm build tree. We want it at the root with 
				the other built files, and renamed slightly. As the RPM plugin has no way 
				of setting this (that I can see), we move it using Ant -->
			<plugin>
				<artifactId>maven-antrun-plugin</artifactId>
				<version>1.7</version>
				<executions>
					<execution>
						<phase>package</phase>
						<configuration>
							<target>
								<copy todir="target">
									<fileset dir="target/rpm/myapp/RPMS/noarch">
										<include name="*.rpm" />
									</fileset>
									<mapper type="regexp" from="^([\w]*)-.*$$" to="\1.noarch.rpm" />
								</copy>
							</target>
						</configuration>
						<goals>
							<goal>run</goal>
						</goals>
					</execution>
				</executions>
			</plugin>

			<!-- Now for Windows -->

			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>nsis-maven-plugin</artifactId>
				<version>1.0-alpha-1</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>generate-headerfile</goal>
							<goal>make</goal>
						</goals>
						<configuration>
							<outputFile>SetupMyApp.exe</outputFile>
							<scriptFile>src/main/nsis/myapp.nsi</scriptFile>
						</configuration>
					</execution>
				</executions>
			</plugin>

			<!-- And now OS X -->

			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>osxappbundle-maven-plugin</artifactId>
				<version>1.0-alpha-3-cm</version>
				<configuration>
					<buildDirectory>target/MyApp</buildDirectory>
					<bundleName>MyApp</bundleName>
					<zipFile>${basedir}/target/myapp-app.zip</zipFile>
					<excludeArtifacts>true</excludeArtifacts>
					<javaApplicationStub>${basedir}/src/main/stubs/osx-stub</javaApplicationStub>
					<mainClass>com.threerings.getdown.launcher.GetdownApp</mainClass>
					<additionalClasspath>
						<path>getdown.jar</path>
					</additionalClasspath>
					<additionalResources>
						<fileSet>
							<directory>${basedir}/target/getdown-stub</directory>
							<includes>
								<include>**/*</include>
							</includes>
						</fileSet>
					</additionalResources>
				</configuration>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>bundle</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
	<pluginRepositories>
		<pluginRepository>
			<id>getdown-maven-plugin-mvn-repo</id>
			<url>https://raw.github.com/rockfireredmoon/getdown-maven-plugin/mvn-repo/</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</pluginRepository>
	</pluginRepositories>
	<repositories>
		<repository>
			<id>codehaus-snapshots</id>
			<url>http://nexus.codehaus.org/snapshots/</url>
			<snapshots />
			<releases>
				<enabled>false</enabled>
			</releases>
		</repository>
	</repositories>
</project>
```