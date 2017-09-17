package org.icestuff.getdown.maven;

import org.apache.maven.plugins.annotations.Parameter;

public class UiConfig {

	/**
	 * UI Background image. Will automatically be added as a resource
	 */
	@Parameter(property = "ui.backgroundImage")
	String backgroundImage;

	/**
	 * UI Error Background image. Will automatically be added as a resource
	 */
	@Parameter(property = "ui.errorBackground")
	String errorBackground;

	/**
	 * UI progress image. Will be automatically added as a resource
	 */
	@Parameter(property = "ui.progressImage")
	String progressImage;

	/**
	 * UI Icons.
	 */
	@Parameter(property = "ui.icons")
	String[] icons;

	/**
	 * UI Mac Dock Icon. Will be automatically added as a resource
	 */
	@Parameter(property = "ui.macDockIcon")
	String macDockIcon;

	/**
	 * Applications name.
	 */
	@Parameter(property = "ui.name", defaultValue = "${project.name}")
	String name;

	/**
	 * UI Background color.
	 */
	@Parameter(property = "ui.background")
	String background;

	/**
	 * UI progress box bounds
	 */
	@Parameter(property = "ui.progress")
	String progress;

	/**
	 * UI progress bar color
	 */
	@Parameter(property = "ui.progressBar")
	String progressBar;

	/**
	 * UI progress text color
	 */
	@Parameter(property = "ui.progressText")
	String progressText;

	/**
	 * UI status box bounds
	 */
	@Parameter(property = "ui.status")
	String status;

	/**
	 * UI status text color
	 */
	@Parameter(property = "ui.statusText")
	String statusText;

	/**
	 * UI text shadow color
	 */
	@Parameter(property = "ui.textShadow")
	String textShadow;

	/**
	 * UI install error URL
	 */
	@Parameter(property = "ui.installError")
	String installError;
}
