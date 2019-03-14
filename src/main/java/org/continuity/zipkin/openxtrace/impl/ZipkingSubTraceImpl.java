package org.continuity.zipkin.openxtrace.impl;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.conversion.ZipkinCallableFactory;
import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.spec.research.open.xtrace.api.core.Location;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.TreeIterator;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;
import org.spec.research.open.xtrace.api.utils.CallableIterator;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkingSubTraceImpl extends ZipkingIdentifiable implements SubTrace, Location {

	private static final String KEY_ROOT = "ROOT";
	private static final String KEY_NON_ROOT = "NON_ROOT";

	private Trace containingTrace;
	private ZipkinCallable root;

	private long subTraceId;

	private ZipkinSpan rootSpan;
	private int size;

	public ZipkingSubTraceImpl fromZipkin(List<ZipkinSpan> input, Predicate<ZipkinSpan> rootCriterion) {
		Map<String, List<ZipkinSpan>> spansPerType = input.stream().collect(Collectors.groupingBy(span -> rootCriterion.test(span) ? KEY_ROOT : KEY_NON_ROOT));

		if (spansPerType.get(KEY_ROOT).size() != 1) {
			throw new IllegalArgumentException(
					"Cannot create SubTrace from " + spansPerType.get(KEY_ROOT).size() + " root spans with IDs "
							+ spansPerType.get(KEY_ROOT).stream().map(ZipkinSpan::getId).collect(Collectors.toList()));
		}

		setupFromRootSpan(spansPerType.get(KEY_ROOT).get(0), spansPerType.get(KEY_NON_ROOT));

		return this;
	}

	private void setupFromRootSpan(ZipkinSpan root, List<ZipkinSpan> remaining) {
		ZipkinSubTraceBundle subTrace = new ZipkinSubTraceBundle(root, remaining);

		this.rootSpan = root;
		this.size = (remaining == null ? 0 : remaining.size()) + 1;
		this.root = ZipkinCallableFactory.INSTANCE.createForSubTrace(subTrace).withContainingSubTrace(this);
		this.subTraceId = new BigInteger(root.getTraceId(), 16).longValue();
	}

	public ZipkingSubTraceImpl withContainingTrace(Trace containingTrace) {
		this.containingTrace = containingTrace;
		setIdentifier(containingTrace.getIdentifier().orElse("unknown").toString() + "-subtrace");

		return this;
	}

	public ZipkingSubTraceImpl withCaller(ZipkinCallable caller) {
		setIdentifier(caller.getIdentifier().orElse("unknown").toString() + "-target");

		return this;
	}

	@Override
	public long getExclusiveTime() {
		if (root instanceof TimedCallable) {
			return getResponseTime() - ((TimedCallable) root).getResponseTime();
		} else {
			return getResponseTime();
		}
	}

	@Override
	public long getResponseTime() {
		return rootSpan.getDuration() * ZipkinTraceImpl.MICROS_TO_NANOS_FACTOR;
	}

	@Override
	public TreeIterator<Callable> iterator() {
		return new CallableIterator(root);
	}

	@Override
	public Optional<String> getApplication() {
		return Optional.ofNullable(rootSpan.getLocalEndpoint().getServiceName());
	}

	@Override
	public Optional<String> getBusinessTransaction() {
		return Optional.ofNullable(rootSpan.getName());
	}

	@Override
	public String getHost() {
		return rootSpan.extractHost();
	}

	@Override
	public Optional<String> getNodeType() {
		return Optional.ofNullable(rootSpan.getLocalEndpoint().getServiceName());
	}

	@Override
	public int getPort() {
		return rootSpan.extractPort();
	}

	@Override
	public Optional<String> getRuntimeEnvironment() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getServerName() {
		return Optional.empty();
	}

	@Override
	public Trace getContainingTrace() {
		return containingTrace;
	}

	@Override
	public Location getLocation() {
		return this;
	}

	@Override
	public SubTrace getParent() {
		return null;
	}

	@Override
	public Callable getRoot() {
		return root;
	}

	@Override
	public long getSubTraceId() {
		return subTraceId;
	}

	@Override
	public List<SubTrace> getSubTraces() {
		return Collections.emptyList();
	}

	@Override
	public int size() {
		return root == null ? -1 : size;
	}

}
