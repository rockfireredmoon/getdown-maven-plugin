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

	protected static void copyFileToDir(File sourceFile, File targetDir) throws MojoExecutionException {
		copyFile(sourceFile, new File(targetDir, sourceFile.getName()));
	}

	protected static void copyFile(File sourceFile, File targetFile) throws MojoExecutionException {
		makeDirectoryIfNecessary(targetFile.getParentFile());
		try {
			FileUtils.copyFile(sourceFile, targetFile);
		} catch (IOException e) {
			throw new MojoExecutionException("Could not copy file " + sourceFile + " to " + targetFile, e);
		}
	}

	protected static void makeDirectoryIfNecessary(File dir) throws MojoExecutionException {

		if (!dir.exists() && !dir.mkdirs()) {
			throw new MojoExecutionException("Failed to create directory: " + dir);
		}

	}

	protected static List<String> copyDirectoryStructure(Log log, File sourceDirectory, File destinationDirectory,
			String includes, String excludes, String dest, String prefix) throws MojoExecutionException {
		if (!sourceDirectory.exists()) {
			throw new MojoExecutionException(sourceDirectory + " does not exist.");
		}

		List<String> paths = new ArrayList<String>();
		List<File> files;
		try {
			files = FileUtils.getFiles(sourceDirectory, includes, excludes);
		} catch (IOException e) {
			throw new MojoExecutionException("Could not obtain files from " + sourceDirectory, e);
		}

		for (File source : files) {

			File destination;

			if (dest != null) {
				/* Copy everything to specific dest directory */
				destination = new File(dest);
				if (!destination.isAbsolute()) {
					destination = new File(destinationDirectory, dest);
				}
			} else {
				String path = source.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length() + 1);
				if (prefix != null) {
					destination = new File(prefix);
					/* Copy to a path prefixed by prefix */
					if (destination.isAbsolute()) {
						destination = new File(destination, path);
					} else {
						destination = new File(destinationDirectory, prefix + File.separator + path);
					}
				} else {
					/* Just copy to same path in destination directory */
					destination = new File(destinationDirectory, path);
				}
			}

			if (source.isDirectory()) {
				makeDirectoryIfNecessary(destination);
			} else {
				File destFile = new File(destination.getParentFile(), source.getName());
				try {
					paths.add(AbstractGetdownMojo.getRelativePath(destinationDirectory, destFile));
				} catch (IOException e) {
					throw new MojoExecutionException(
							"Could not relativize path of " + source + " from " + sourceDirectory, e);
				}
				try {
					FileUtils.copyFileToDirectory(source, destination.getParentFile());
				} catch (IOException e) {
					throw new MojoExecutionException("Could not copy file " + source + " to directory" + destination, e);
				}
			}
		}
		return paths;
	}

	protected static String copyFile(Log log, File file, File destinationDirectory, String dest, String prefix)
			throws MojoExecutionException {
		if (!file.exists()) {
			throw new MojoExecutionException(file + " does not exist.");
		}

		if (file.isDirectory()) {
			throw new MojoExecutionException(file + " is a directory.");
		}

		log.info("Copying " + file + " to " + destinationDirectory);

		File destDir;

		if (dest != null) {
			/* Copy everything to specific dest directory */
			destDir = new File(dest);
			if (!destDir.isAbsolute()) {
				destDir = new File(destinationDirectory, dest);
			}
		} else {
			if (prefix != null) {
				destDir = new File(prefix);
				/* Copy to a path prefixed by prefix */
				if (destDir.isAbsolute()) {
					destDir = new File(destDir, file.getPath());
				} else {
					destDir = new File(destinationDirectory, prefix + File.separator + file.getPath());
				}
			} else {
				/* Just copy to same path in destination directory */
				destDir = new File(destinationDirectory, file.getPath());
			}
		}

		log.info("Copying " + file + " to " + destDir);

		try {
			FileUtils.copyFileToDirectory(file, destDir.getParentFile());
		} catch (IOException e) {
			throw new MojoExecutionException("Could not copy file " + file + " to directory" + destDir, e);
		}

		try {
			File parfile = new File(destDir.getParentFile(), file.getName());
			String relativePath = AbstractGetdownMojo.getRelativePath(destinationDirectory, parfile);
			log.info("Relative path of " + destinationDirectory + " vs " + parfile + " is " + relativePath);
			return relativePath;
		} catch (IOException e) {
			throw new MojoExecutionException("Could not relativize path of " + file + " from " + file, e);
		}
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

	protected static void copyResources(Log log, File sourceDirectory, File targetDirectory, String dest, String prefix)
			throws MojoExecutionException {
		if (!sourceDirectory.exists()) {
			log.warn("Directory does not exist " + sourceDirectory.getAbsolutePath());
		} else {
			if (!sourceDirectory.isDirectory()) {
				log.debug("Not a directory: " + sourceDirectory.getAbsolutePath());
			} else {
				log.debug("Copying resources from " + sourceDirectory.getAbsolutePath());
				copyDirectoryStructure(log, sourceDirectory, targetDirectory, "**", concat(DirectoryScanner.DEFAULTEXCLUDES, ", "), dest, prefix);
			}

		}
	}

	public static void deleteFile(File file) throws MojoExecutionException {
		if (file.exists() && !file.delete()) {
			throw new MojoExecutionException("Could not delete file: " + file);
		}
	}

	public static void copyResources(URI uri, ClassLoader classLoader, File target) throws MojoExecutionException {
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
			throw new MojoExecutionException("Could not open resource " + url, e);
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
				throw new MojoExecutionException("Could not copy resource from " + url + " to " + target, e);
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
