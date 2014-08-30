package org.icestuff.getdown.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

public class Util {

    protected static void copyFileToDir(File sourceFile, File targetDir)
	    throws MojoExecutionException {
	copyFile(sourceFile, new File(targetDir, sourceFile.getName()));
    }

    protected static void copyFile(File sourceFile, File targetFile)
	    throws MojoExecutionException {
	makeDirectoryIfNecessary(targetFile.getParentFile());
	try {
	    FileUtils.copyFile(sourceFile, targetFile);
	} catch (IOException e) {
	    throw new MojoExecutionException("Could not copy file "
		    + sourceFile + " to " + targetFile, e);
	}
    }

    protected static void makeDirectoryIfNecessary(File dir)
	    throws MojoExecutionException {

	if (!dir.exists() && !dir.mkdirs()) {
	    throw new MojoExecutionException("Failed to create directory: "
		    + dir);
	}

    }

    protected static List<String> copyDirectoryStructure(Log log,
	    File sourceDirectory, File destinationDirectory, String includes,
	    String excludes) throws MojoExecutionException {
	if (!sourceDirectory.exists()) {
	    throw new MojoExecutionException(sourceDirectory
		    + " does not exist.");
	}

	List<String> paths = new ArrayList<String>();
	List<File> files;
	try {
	    files = FileUtils.getFiles(sourceDirectory, includes, excludes);
	} catch (IOException e) {
	    throw new MojoExecutionException("Could not obtain files from "
		    + sourceDirectory, e);
	}

	for (File file : files) {

	    log.debug("Copying " + file + " to " + destinationDirectory);

	    String path = file.getAbsolutePath().substring(
		    sourceDirectory.getAbsolutePath().length() + 1);

	    File destDir = new File(destinationDirectory, path);

	    log.debug("Copying " + file + " to " + destDir);

	    if (file.isDirectory()) {
		makeDirectoryIfNecessary(destDir);
	    } else {
		try {
		    paths.add(AbstractGetdownMojo.getRelativePath(
			    sourceDirectory, file));
		} catch (IOException e) {
		    throw new MojoExecutionException(
			    "Could not relativize path of " + file + " from "
				    + sourceDirectory, e);
		}
		try {
		    FileUtils
			    .copyFileToDirectory(file, destDir.getParentFile());
		} catch (IOException e) {
		    throw new MojoExecutionException("Could not copy file "
			    + file + " to directory" + destDir, e);
		}
	    }
	}
	return paths;
    }

    protected static String concat(String[] array, String delim) {
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < array.length; i++) {
	    if (i > 0) {
		buffer.append(delim);
	    }
	    String s = array[i];
	    buffer.append(s).append(delim);
	}
	return buffer.toString();
    }

    protected static void copyResources(Log log, File sourceDirectory,
	    File targetDirectory) throws MojoExecutionException {
	if (!sourceDirectory.exists()) {
	    log.info("Directory does not exist "
		    + sourceDirectory.getAbsolutePath());
	} else {
	    if (!sourceDirectory.isDirectory()) {
		log.debug("Not a directory: "
			+ sourceDirectory.getAbsolutePath());
	    } else {
		log.debug("Copying resources from "
			+ sourceDirectory.getAbsolutePath());

		// this may needs to be parametrized somehow
		String excludes = concat(DirectoryScanner.DEFAULTEXCLUDES, ", ");
		copyDirectoryStructure(log, sourceDirectory, targetDirectory,
			"**", excludes);
	    }

	}
    }

    public static void deleteFile(File file) throws MojoExecutionException {
	if (file.exists() && !file.delete()) {
	    throw new MojoExecutionException("Could not delete file: " + file);
	}
    }

    public static void copyResources(URI uri, ClassLoader classLoader,
	    File target) throws MojoExecutionException {
	URL url;

	String scheme = uri.getScheme();
	if ("classpath".equals(scheme)) {

	    // get resource from class-path
	    String path = uri.getPath();

	    if (path == null) {
		// can happen when using classpath:myFile
		path = uri.toString().substring(scheme.length() + 1);
	    }

	    if (path.startsWith("/")) {
		// remove first car
		path = path.substring(1);
	    }
	    url = classLoader.getResource(path);
	} else {
	    // classic url from uri
	    try {
		url = uri.toURL();
	    } catch (MalformedURLException e) {
		throw new MojoExecutionException("Bad uri syntax " + uri, e);
	    }
	}

	InputStream inputStream;

	try {
	    inputStream = url.openStream();
	} catch (IOException e) {
	    throw new MojoExecutionException("Could not open resource " + url,
		    e);
	}

	if (inputStream == null) {
	    throw new MojoExecutionException("Could not find resource " + url);
	}
	try {
	    OutputStream outputStream = null;

	    try {
		outputStream = new FileOutputStream(target);
		org.codehaus.plexus.util.IOUtil.copy(inputStream, outputStream);
		outputStream.close();
		inputStream.close();
	    } catch (IOException e) {
		throw new MojoExecutionException(
			"Could not copy resource from " + url + " to " + target,
			e);
	    } finally {
		if (outputStream != null) {
		    org.codehaus.plexus.util.IOUtil.close(outputStream);
		}
	    }
	}

	finally {
	    org.codehaus.plexus.util.IOUtil.close(inputStream);
	}

    }
}
