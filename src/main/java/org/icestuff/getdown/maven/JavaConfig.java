package org.icestuff.getdown.maven;

public class JavaConfig {

	/**
	 * Java exact version. This can either be in the dotted format or a
	 * long integer calculated thus <code>PATCH + 100 * (REV + 100 * (MIN + 100 * MAJ))</code>. If
	 * this is set, then neither minVersion or maxVersion may be set
	 * @parameter(property = "java.version")
	 */
	String version;

	/**
	 * Java minimum version. This can either be in the dotted format or a
	 * long integer calculated thus <code>PATCH + 100 * (REV + 100 * (MIN + 100 * MAJ))</code>.
	 * If this is set, exact version may not be set.
	 * @parameter(property = "java.minVersion")
	 */
	String minVersion;

	/**
	 * Java maximum version. This can either be in the dotted format or a
	 * long integer calculated thus <code>PATCH + 100 * (REV + 100 * (MIN + 100 * MAJ))</code>.
	 * If this is set, exact version may not be set.
	 * @parameter(property = "java.maxVersion")
	 */
	String maxVersion;

	/**
	 * The java system property used to determine the version.
	 * @parameter(property = "java.versionProp")
	 */
	String versionProp ;

	/**
	 * The regular expression used to extract the MAJ, MIN, REV and PATCH elements
	 * @parameter(property = "java.versionRegex")
	 */
	String versionRegex;

	/**
	 * The java download resource paths.
	 * @parameter(property = "java.downloads")
	 */
	JavaDownload[] downloads;

}
