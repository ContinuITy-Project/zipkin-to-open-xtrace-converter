package org.continuity.zipkin.openxtrace.conversion;

import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.continuity.zipkin.openxtrace.impl.ZipkinCallable;

/**
 * Creates instances of {@link ZipkinCallable} which can be filled by calling
 * {@link ZipkinCallable#fromZipkin(org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle)}.
 *
 * @author Henning Schulz
 *
 */
public class ZipkinCallableFactory {

	public static final ZipkinCallableFactory INSTANCE = new ZipkinCallableFactory();

	private ZipkinCallableFactory() {
	}

	public ZipkinCallable createForSubTrace(ZipkinSubTraceBundle subTrace) {
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
