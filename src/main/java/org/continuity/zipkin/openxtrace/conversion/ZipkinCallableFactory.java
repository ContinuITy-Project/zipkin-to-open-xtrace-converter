package org.continuity.zipkin.openxtrace.conversion;

import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.continuity.zipkin.openxtrace.impl.ZipkinCallable;

/**
 * Creates instances of {@link ZipkinCallable}.
 *
 * @author Henning Schulz
 *
 */
public class ZipkinCallableFactory {

	public static final ZipkinCallableFactory INSTANCE = new ZipkinCallableFactory();

	private ZipkinCallableFactory() {
	}

	/**
	 * Creates a callable.
	 *
	 * @param subTrace
	 *            The sub trace bundle for which the callable is to be created.
	 * @return The created callable.
	 */
	public ZipkinCallable createForSubTrace(ZipkinSubTraceBundle subTrace) {
		return createEmptyCallable(subTrace).fromZipkin(subTrace);
	}

	private ZipkinCallable createEmptyCallable(ZipkinSubTraceBundle subTrace) {
		// TODO

		switch (subTrace.getRoot().getKind()) {
		case CLIENT:
			break;
		case CONSUMER:
			break;
		case PRODUCER:
			break;
		case SERVER:
			break;
		default:
			break;

		}
	}

}
