package org.continuity.zipkin.openxtrace.impl;

/**
 *
 * @author Henning Schulz
 *
 * @param <Z>
 * @param <O>
 */
public interface ZipkinConvertible<Z, O> {

	/**
	 * Converts from Zipkin.
	 *
	 * @param input
	 *            The Zipkin input.
	 * @return An OPEN.xtrace output.
	 */
	O fromZipkin(Z input);

}
