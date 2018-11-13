package org.icestuff.getdown.maven;

public class UiConfig {

	/**
	 * UI Background image. Will automatically be added as a resource
	 * @parameter(property = "ui.backgroundImage")
	 */
	String backgroundImage;

	/**
	 * UI Error Background image. Will automatically be added as a resource
	 * @parameter(property = "ui.errorBackground")
	 */
	String errorBackground;

	/**
	 * UI progress image. Will be automatically added as a resource
	 * @parameter(property = "ui.progressImage")
	 */
	String progressImage;

	/**
	 * UI Icons.
	 * @parameter(property = "ui.icons")
	 */
	String[] icons;

	/**
	 * UI Mac Dock Icon. Will be automatically added as a resource
	 * @parameter(property = "ui.macDockIcon")
	 */
	String macDockIcon;

	/**
	 * Applications name.
	 * @parameter(property = "ui.name", defaultValue = "${project.name}")
	 */
	String name;

	/**
	 * UI Background color.
	 * @parameter(property = "ui.background")
	 */
	String background;

	/**
	 * UI progress box bounds
	 * @parameter(property = "ui.progress")
	 */
	String progress;

	/**
	 * UI progress bar color
	 * @parameter(property = "ui.progressBar")
	 */
	String progressBar;

	/**
	 * UI progress text color
	 * @parameter(property = "ui.progressText")
	 */
	String progressText;

	/**
	 * UI status box bounds
	 * @parameter(property = "ui.status")
	 */
	String status;

	/**
	 * UI status text color
	 * @parameter(property = "ui.statusText")
	 */
	String statusText;

	/**
	 * UI text shadow color
	 * @parameter(property = "ui.textShadow")
	 */
	String textShadow;

	/**
	 * UI install error URL
	 * @parameter(property = "ui.installError")
	 */
	String installError;

	/**
	 * UI hide decorations
	 * @parameter(property = "ui.hideDecorations")
	 */
	boolean hideDecorations;

	/**
	 * UI min show seconds
	 * @parameter(property = "ui.minShowSeconds")
	 */
	Integer minShowSeconds;
}
