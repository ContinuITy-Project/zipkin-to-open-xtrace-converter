package org.continuity.zipkin.openxtrace.conversion;

import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.continuity.zipkin.openxtrace.impl.ZipkinCallable;
import org.continuity.zipkin.openxtrace.impl.ZipkinHttpRequestProcessing;
import org.continuity.zipkin.openxtrace.impl.ZipkinRemoteInvocation;
import org.continuity.zipkin.openxtrace.impl.ZipkinUseCaseInvocation;

import zipkin2.Span.Kind;

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
		ZipkinSpan root = subTrace.getRoot();

		if (root.getKind() == Kind.CLIENT) {
			return new ZipkinRemoteInvocation();
		} else if ((root.getKind() == Kind.SERVER) && (root.getTags().containsKey(ZipkinSpan.KEY_HTTP_URL) || root.getTags().containsKey(ZipkinSpan.KEY_HTTP_PATH))) {
			return new ZipkinHttpRequestProcessing();
		} else {
			return new ZipkinUseCaseInvocation();
		}
	}

}
