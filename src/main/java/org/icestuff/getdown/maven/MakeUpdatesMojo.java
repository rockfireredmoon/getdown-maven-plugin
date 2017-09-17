package org.icestuff.getdown.maven;

import com.threerings.getdown.tools.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Make deployable Java apps.
 */
@Mojo(name = "updates", aggregator = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class MakeUpdatesMojo extends AbstractGetdownMojo {

	/**
	 * The URL from which the client is downloaded.
	 */
	@Parameter(required = true)
	private String appbase;

	/**
	 * The main class name.
	 */
	@Parameter(required = true)
	private String mainClass;

	/**
	 * The directory in which files will be stored prior to processing.
	 */
	@Parameter(defaultValue = "${project.build.directory}/getdown", required = true)
	private File workDirectory;

	/**
	 * The path where the libraries are placed within the getdown structure.
	 */
	@Parameter(defaultValue = "")
	protected String libPath;

	/**
	 * The location of the directory (relative or absolute) containing non-jar
	 * resources that are to be included in the getdown bundle.
	 */
	@Parameter
	private File resourcesDirectory;

	/**
	 * [optional] transitive dependencies filter - if omitted, the plugin will
	 * include all transitive dependencies. Provided and test scope dependencies
	 * are always excluded.
	 */
	@Parameter
	private Dependencies dependencies;
	/**
	 * Set to true to exclude all transitive dependencies.
	 * 
	 * @parameter
	 */
	@Parameter
	private boolean excludeTransitive;
	@Parameter
	private boolean ignoreMissingMain;

	/**
	 * By default, Getdown will fail if it is running a non-versioned
	 * application and cannot contact the server configured in appbase to check
	 * for updates. If you add allow_offline = true to your getdown.txt, Getdown
	 * will ignore such failures and allow the application to be run anyway.
	 */
	@Parameter(defaultValue = "false")
	private boolean allowOffline;

	@Parameter
	private String[] appargs;

	@Parameter
	private String[] jvmargs;
	/**
	 * When set to true, this flag indicates that a version attribute should be
	 * output in each of the jar resource elements in the generated JNLP file.
	 * <p/>
	 * <strong>Note: </strong> since version 1.0-beta-5 we use the version
	 * download protocol optimization (see
	 * http://docs.oracle.com/javase/tutorial
	 * /deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html).
	 */
	@Parameter(defaultValue = "false")
	private boolean outputJarVersions;

	@Parameter()
	private ResourceSet[] resourceSets;

	@Parameter()
	private ResourceSet[] uresourceSets;

	public static class ResourceSet {

		@Parameter
		private String path;

		@Parameter
		private String[] includes;

		@Parameter
		private String[] excludes;
	}

	/**
	 * Represents the configuration element that specifies which of the current
	 * project's dependencies will be included or excluded from the resources
	 * element in the generated JNLP file.
	 */
	public static class Dependencies {

		private List<String> includes;

		private List<String> excludes;

		public List<String> getIncludes() {
			return includes;
		}

		public void setIncludes(List<String> includes) {
			this.includes = includes;
		}

		public List<String> getExcludes() {
			return excludes;
		}

		public void setExcludes(List<String> excludes) {
			this.excludes = excludes;
		}
	}

	//
	private Artifact artifactWithMainClass;
	private List<Artifact> packagedJnlpArtifacts = new ArrayList<Artifact>();
	private final List<String> modifiedJnlpArtifacts = new ArrayList<String>();

	private List<String> uresourceSetPaths;

	private List<String> resourceSetPaths;

	public void execute() throws MojoExecutionException {

		getLog().debug("using work directory " + workDirectory);
		getLog().debug("using library directory " + libPath);

		Util.makeDirectoryIfNecessary(workDirectory);
		Util.makeDirectoryIfNecessary(getLibDirectory());

		try {
			initSign();
			Util.copyResources(getLog(), getResourcesDirectory(), workDirectory);

			artifactWithMainClass = null;

			processDependencies();

			if (artifactWithMainClass == null && !ignoreMissingMain) {
				throw new MojoExecutionException(
						"didn't find artifact with main class: " + mainClass + ". Did you specify it? ");
			}

			copyResourceSets();
			makeConfigFile();
			makeDigestFile();
		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoExecutionException("Failure to run the plugin: ", e);
		}
	}

	protected void copyResourceSets() throws MojoExecutionException {
		if (resourceSets != null) {
			resourceSetPaths = copyResourceSets(resourceSets);
		}
		if (uresourceSets != null) {
			uresourceSetPaths = copyResourceSets(uresourceSets);
		}

		copyUIResources();
	}

	protected void makeDigestFile() throws IOException, GeneralSecurityException {
		getLog().info("Making digest");
		Digester.createDigest(1, workDirectory);
		Digester.createDigest(2, workDirectory);
		if (sign != null) {
			getLog().info("Signing digest");
			sign.signDigest(workDirectory);
		}
	}

	protected void makeConfigFile() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File(workDirectory, "getdown.txt"));
		try {
			writer.println("# The URL from which the client is downloaded");
			writer.println(String.format("appbase = %s", appbase));
			writer.println(String.format("allow_offline = %s", allowOffline));
			writer.println();
			writeUIConfiguration(writer);
			writer.println();
			writer.println("# Application jar files");

			for (Artifact s : packagedJnlpArtifacts) {
				String name = "";
				if (!libPath.equals("")) {
					name = libPath + "/";
				}
				name += s.getArtifactId();
				if (outputJarVersions) {
					name += "-" + s.getVersion();
				}
				name += ".jar";
				writer.println(String.format("code = %s", name));
			}

			writer.println();
			writer.println("# Resources");
			if (resourceSetPaths != null) {
				if (resourceSetPaths != null) {
					for (String p : resourceSetPaths) {
						writer.println(String.format("resource = %s", p));
					}
				}
			}
			writeUIResources(writer);
			writer.println();

			if (uresourceSetPaths != null) {
				writer.println("# Unpacked Resources");
				for (String p : uresourceSetPaths) {
					writer.println(String.format("uresource = %s", p));
				}
				writer.println();
			}

			writer.println("# The main entry point for the application");
			writer.println(String.format("class = %s", mainClass));
			if (appargs != null) {
				for (String s : appargs) {
					writer.println(String.format("apparg = %s", s));
				}
			}
			if (jvmargs != null) {
				for (String s : jvmargs) {
					writer.println(String.format("jvmarg = %s", s));
				}
			}

		} finally {
			writer.close();
		}
	}

	@Override
	protected void writeUIConfiguration(PrintWriter writer) {
		super.writeUIConfiguration(writer);
		writer.println(String.format("ui.name = %s", ui.name));
		if (ui.background != null) {
			writer.println(String.format("ui.background = %s", ui.background));
		}
		if (ui.progress != null) {
			writer.println(String.format("ui.progress = %s", ui.progress));
		}
		if (ui.progressBar != null) {
			writer.println(String.format("ui.progress_bar = %s", ui.progressBar));
		}
		if (ui.progressText != null) {
			writer.println(String.format("ui.progress_text = %s", ui.progressText));
		}
		if (ui.status != null) {
			writer.println(String.format("ui.status = %s", ui.status));
		}
		if (ui.statusText != null) {
			writer.println(String.format("ui.status_text = %s", ui.statusText));
		}
		if (ui.textShadow != null) {
			writer.println(String.format("ui.text_shadow = %s", ui.textShadow));
		}
		if (ui.installError != null) {
			writer.println(String.format("ui.install_error = %s", ui.installError));
		}
	}

	/**
	 * Detects improper includes/excludes configuration.
	 * 
	 * @throws MojoExecutionException
	 *             if at least one of the specified includes or excludes matches
	 *             no artifact, false otherwise
	 */
	void checkDependencies() throws MojoExecutionException {
		if (dependencies == null) {
			return;
		}

		boolean failed = false;

		Collection<Artifact> artifacts = project.getArtifacts();

		getLog().debug("artifacts: " + artifacts.size());

		if (dependencies.getIncludes() != null && !dependencies.getIncludes().isEmpty()) {
			failed = checkDependencies(dependencies.getIncludes(), artifacts);
		}
		if (dependencies.getExcludes() != null && !dependencies.getExcludes().isEmpty()) {
			failed = checkDependencies(dependencies.getExcludes(), artifacts) || failed;
		}

		if (failed) {
			throw new MojoExecutionException(
					"At least one specified dependency is incorrect. Review your project configuration.");
		}
	}

	/**
	 * Iterate through all the top level and transitive dependencies declared in
	 * the project and collect all the runtime scope dependencies for inclusion
	 * in the .zip and signing.
	 * 
	 * @throws MojoExecutionException
	 *             if could not process dependencies
	 */
	private void processDependencies() throws MojoExecutionException {

		processDependency(project.getArtifact());

		AndArtifactFilter filter = new AndArtifactFilter();
		// filter.add( new ScopeArtifactFilter( dependencySet.getScope() ) );

		if (dependencies != null && dependencies.getIncludes() != null && !dependencies.getIncludes().isEmpty()) {
			filter.add(new IncludesArtifactFilter(dependencies.getIncludes()));
		}
		if (dependencies != null && dependencies.getExcludes() != null && !dependencies.getExcludes().isEmpty()) {
			filter.add(new ExcludesArtifactFilter(dependencies.getExcludes()));
		}

		Collection<Artifact> artifacts = excludeTransitive ? project.getDependencyArtifacts() : project.getArtifacts();

		for (Artifact artifact : artifacts) {
			if (filter.include(artifact)) {
				processDependency(artifact);
			}
		}
	}

	private void processDependency(Artifact artifact) throws MojoExecutionException {
		// TODO: scope handler
		// Include runtime and compile time libraries
		verboseLog("Process dependency  " + artifact.getArtifactId());
		if (!Artifact.SCOPE_SYSTEM.equals(artifact.getScope()) && !Artifact.SCOPE_PROVIDED.equals(artifact.getScope())
				&& !Artifact.SCOPE_TEST.equals(artifact.getScope())) {
			String type = artifact.getType();
			if ("jar".equals(type) || "ejb-client".equals(type)) {

				// FIXME when signed, we should update the manifest.
				// see
				// http://www.mail-archive.com/turbine-maven-dev@jakarta.apache.org/msg08081.html
				// and maven1:
				// maven-plugins/jnlp/src/main/org/apache/maven/jnlp/UpdateManifest.java
				// or shouldn't we? See MOJO-7 comment end of October.
				final File toCopy = artifact.getFile();

				if (toCopy == null) {
					getLog().error("artifact with no file: " + artifact);
					getLog().error("artifact download url: " + artifact.getDownloadUrl());
					getLog().error("artifact repository: " + artifact.getRepository());
					getLog().error("artifact repository: " + artifact.getVersion());
					throw new IllegalStateException(
							"artifact " + artifact + " has no matching file, why? Check the logs...");
				}

				String name = getDependencyFileBasename(artifact, outputJarVersions);

				boolean copied = copyJarAsUnprocessedToDirectoryIfNecessary(toCopy, getLibDirectory(), name + ".jar");

				if (copied) {
					int lastIndexOf = name.lastIndexOf('.');
					if (lastIndexOf == -1) {
						modifiedJnlpArtifacts.add(name);
					} else {
						modifiedJnlpArtifacts.add(name.substring(0, lastIndexOf));
					}
				}

				packagedJnlpArtifacts.add(artifact);

				if (mainClass != null) {

					// try to find if this dependency contains the main class
					boolean containsMainClass = artifactContainsClass(artifact, mainClass);

					if (containsMainClass) {
						if (artifactWithMainClass == null) {
							artifactWithMainClass = artifact;
							getLog().info("Found main jar. Artifact " + artifactWithMainClass
									+ " contains the main class: " + mainClass);
						} else {
							getLog().warn("artifact " + artifact + " also contains the main class: " + mainClass
									+ ". IGNORED.");
						}
					}
				}

			} else
			// FIXME how do we deal with native libs?
			// we should probably identify them and package inside jars that we
			// timestamp like the native lib
			// to avoid repackaging every time. What are the types of the native
			// libs?
			{
				verboseLog("Skipping artifact of type " + type + " for " + getLibDirectory().getName());
			}
			// END COPY
		} else {
			verboseLog("Skipping artifact of scope " + artifact.getScope() + " for " + getLibDirectory().getName());
		}
	}

	public String getDependencyFileBasename(Artifact artifact, boolean outputJarVersion) {
		String filename = artifact.getArtifactId();

		if (StringUtils.isNotEmpty(artifact.getClassifier())) {
			filename += "-" + artifact.getClassifier();
		}

		if (outputJarVersion) {
			filename += "-";
			filename += artifact.getVersion();
		}
		return filename;
	}

	protected File getWorkDirectory() {
		return workDirectory;
	}

	protected boolean copyJarAsUnprocessedToDirectoryIfNecessary(File sourceFile, File targetDirectory,
			String targetFilename) throws MojoExecutionException {

		if (sourceFile == null) {
			throw new IllegalArgumentException("sourceFile is null");
		}

		if (targetFilename == null) {
			targetFilename = sourceFile.getName();
		}

		File signedTargetFile = new File(targetDirectory, targetFilename);

		boolean shouldCopy = !signedTargetFile.exists()
				|| (signedTargetFile.lastModified() < sourceFile.lastModified());

		if (shouldCopy) {
			Util.copyFile(sourceFile, signedTargetFile);

		} else {
			getLog().debug(
					"Source file hasn't changed. Do not reprocess " + signedTargetFile + " with " + sourceFile + ".");
		}

		return shouldCopy;
	}

	/**
	 * @param patterns
	 *            list of patterns to test over artifacts
	 * @param artifacts
	 *            collection of artifacts to check
	 * @return true if at least one of the pattern in the list matches no
	 *         artifact, false otherwise
	 */
	private boolean checkDependencies(List<String> patterns, Collection<Artifact> artifacts) {
		if (dependencies == null) {
			return false;
		}

		boolean failed = false;
		for (String pattern : patterns) {
			failed = ensurePatternMatchesAtLeastOneArtifact(pattern, artifacts) || failed;
		}
		return failed;
	}

	/**
	 * @param pattern
	 *            pattern to test over artifacts
	 * @param artifacts
	 *            collection of artifacts to check
	 * @return true if filter matches no artifact, false otherwise *
	 */
	private boolean ensurePatternMatchesAtLeastOneArtifact(String pattern, Collection<Artifact> artifacts) {
		List<String> onePatternList = new ArrayList<String>();
		onePatternList.add(pattern);
		ArtifactFilter filter = new IncludesArtifactFilter(onePatternList);

		boolean noMatch = true;
		for (Artifact artifact : artifacts) {
			getLog().debug("checking pattern: " + pattern + " against " + artifact);

			if (filter.include(artifact)) {
				noMatch = false;
				break;
			}
		}
		if (noMatch) {
			getLog().error("pattern: " + pattern + " doesn't match any artifact.");
		}
		return noMatch;
	}

	private File getLibDirectory() {
		if (libPath != null) {
			return new File(workDirectory, libPath);
		}
		return workDirectory;
	}

	private File getResourcesDirectory() {

		if (resourcesDirectory == null) {
			resourcesDirectory = new File(project.getBasedir(), "resources");
		}

		return resourcesDirectory;

	}

	private List<String> copyResourceSets(ResourceSet[] resourceSets) throws MojoExecutionException {
		List<String> paths = new ArrayList<String>();
		for (ResourceSet s : resourceSets) {
			File sourceDirectory = new File(s.path);
			if (!sourceDirectory.exists()) {
				getLog().info("Directory does not exist " + sourceDirectory.getAbsolutePath());
			} else {
				if (!sourceDirectory.isDirectory()) {
					getLog().debug("Not a directory: " + sourceDirectory.getAbsolutePath());
				} else {
					getLog().debug("Copying resource set from " + sourceDirectory.getAbsolutePath());

					String include = s.includes == null ? "**" : Util.concat(s.includes, ", ");
					String excludes = s.excludes == null ? Util.concat(DirectoryScanner.DEFAULTEXCLUDES, ", ")
							: (Util.concat(DirectoryScanner.DEFAULTEXCLUDES, ", ") + Util.concat(s.excludes, ", "));

					paths.addAll(
							Util.copyDirectoryStructure(getLog(), sourceDirectory, workDirectory, include, excludes));
				}

			}
		}
		return paths;
	}

	/**
	 * Tests if the given fully qualified name exists in the given artifact.
	 * 
	 * @param artifact
	 *            artifact to test
	 * @param mainClass
	 *            the fully qualified name to find in artifact
	 * @return {@code true} if given artifact contains the given fqn,
	 *         {@code false} otherwise
	 * @throws MojoExecutionException
	 *             if artifact file url is mal formed
	 */

	public boolean artifactContainsClass(Artifact artifact, final String mainClass) throws MojoExecutionException {
		boolean containsClass = true;

		// JarArchiver.grabFilesAndDirs()
		URL url;
		try {
			url = artifact.getFile().toURI().toURL();
		} catch (MalformedURLException e) {
			throw new MojoExecutionException("Could not get artifact url: " + artifact.getFile(), e);
		}
		ClassLoader cl = new java.net.URLClassLoader(new URL[] { url });
		Class<?> c = null;
		try {
			c = Class.forName(mainClass, false, cl);
		} catch (ClassNotFoundException e) {
			getLog().debug("artifact " + artifact + " doesn't contain the main class: " + mainClass);
			containsClass = false;
		} catch (Throwable t) {
			getLog().info("artifact " + artifact + " seems to contain the main class: " + mainClass
					+ " but the jar doesn't seem to contain all dependencies " + t.getMessage());
		}

		if (c != null) {
			getLog().debug("Checking if the loaded class contains a main method.");

			try {
				c.getMethod("main", String[].class);
			} catch (NoSuchMethodException e) {
				getLog().warn("The specified main class (" + mainClass + ") doesn't seem to contain a main method... "
						+ "Please check your configuration." + e.getMessage());
			} catch (NoClassDefFoundError e) {
				// undocumented in SDK 5.0. is this due to the ClassLoader lazy
				// loading the Method
				// thus making this a case tackled by the JVM Spec (Ref 5.3.5)!
				// Reported as Incident 633981 to Sun just in case ...
				getLog().warn("Something failed while checking if the main class contains the main() method. "
						+ "This is probably due to the limited classpath we have provided to the class loader. "
						+ "The specified main class (" + mainClass
						+ ") found in the jar is *assumed* to contain a main method... " + e.getMessage());
			} catch (Throwable t) {
				getLog().error("Unknown error: Couldn't check if the main class has a main method. "
						+ "The specified main class (" + mainClass
						+ ") found in the jar is *assumed* to contain a main method...", t);
			}
		}

		return containsClass;
	}
}
