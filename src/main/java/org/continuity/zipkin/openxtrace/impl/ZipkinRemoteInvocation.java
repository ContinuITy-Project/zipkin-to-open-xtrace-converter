package org.continuity.zipkin.openxtrace.impl;

import java.util.Objects;
import java.util.Optional;

import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.spec.research.open.xtrace.api.core.Location;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkinRemoteInvocation extends ZipkinTimedCallable implements RemoteInvocation {

	private ZipkingSubTraceImpl targetSubTrace;

	@Override
	public ZipkinCallable fromZipkin(ZipkinSubTraceBundle input) {
		super.fromZipkin(input);

		if (input.getRemaining() != null) {
			this.targetSubTrace = new ZipkingSubTraceImpl().fromZipkin(input.getRemaining(), child -> Objects.equals(getSpan().getId(), child.getParentId())).withCaller(this);
		}

		return this;
	}

	@Override
	public String getTarget() {
		String host = getSpan().getRemoteEndpoint().getServiceName();

		if (host == null) {
			host = getSpan().getRemoteEndpoint().getIpv4();
		}

		if (host == null) {
			host = getSpan().getRemoteEndpoint().getIpv6();
		}

		int port = getSpan().getRemoteEndpoint().getPort();

		if (port <= 0) {
			return host;
		} else {
			return host + ":" + port;
		}
	}

	@Override
	public Optional<Location> getTargetLocation() {
		return Optional.ofNullable(targetSubTrace == null ? null : targetSubTrace.getLocation());
	}

	@Override
	public Optional<SubTrace> getTargetSubTrace() {
		return Optional.ofNullable(targetSubTrace);
	}

	@Override
	protected boolean isKnownTag(ZipkinTagInformation tag) {
		return false;
	}

}
