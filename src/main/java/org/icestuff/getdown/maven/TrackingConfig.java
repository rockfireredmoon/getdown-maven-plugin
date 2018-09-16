package org.icestuff.getdown.maven;

import org.apache.maven.plugins.annotations.Parameter;

public class TrackingConfig {

	/**
	 * Tracking URL.
	 */
	@Parameter(property = "tracking.url")
	String url;

	/**
	 * Tracking URL.
	 */
	@Parameter(property = "tracking.urlSuffix")
	String urlSuffix;

	/**
	 * Tracking percents.
	 */
	@Parameter(property = "tracking.percents")
	int[] percents;

	/**
	 * Tracking cookie name
	 */
	@Parameter(property = "tracking.cookieName")
	String cookieName;

	/**
	 * Tracking cookie property
	 */
	@Parameter(property = "tracking.cookieProperty")
	String cookieProperty;

}
