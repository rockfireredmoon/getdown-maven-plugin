package org.icestuff.getdown.maven;

public class TrackingConfig {

	/**
	 * Tracking URL.
	 * @parameter(property = "tracking.url")
	 */
	String url;

	/**
	 * Tracking URL.
	 * @parameter(property = "tracking.urlSuffix")
	 */
	String urlSuffix;

	/**
	 * Tracking percents.
	 * @parameter(property = "tracking.percents")
	 */
	int[] percents;

	/**
	 * Tracking cookie name
	 * @parameter(property = "tracking.cookieName")
	 */
	String cookieName;

	/**
	 * Tracking cookie property
	 * @parameter(property = "tracking.cookieProperty")
	 */
	String cookieProperty;

}
