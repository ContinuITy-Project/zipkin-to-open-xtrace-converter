package org.continuity.zipkin.openxtrace.impl;

import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 *
 * @author Henning Schulz
 *
 */
public abstract class ZipkinTimedCallable extends ZipkinCallable implements TimedCallable {

	@Override
	public long getExclusiveTime() {
		return getResponseTime();
	}

	@Override
	public long getResponseTime() {
		return getSpan().getDuration() * ZipkinTraceImpl.MICROS_TO_NANOS_FACTOR;
	}

	@Override
	public long getExitTime() {
		return getTimestamp() + Math.round(getResponseTime() * ZipkinTraceImpl.NANOS_TO_MILLIS_FACTOR);
	}


}
