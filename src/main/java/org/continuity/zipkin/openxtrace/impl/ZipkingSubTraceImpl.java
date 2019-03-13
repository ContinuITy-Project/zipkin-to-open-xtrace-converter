package org.continuity.zipkin.openxtrace.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.conversion.ZipkinCallableFactory;
import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ZipkingSubTraceImpl extends ZipkingIdentifiable implements SubTrace, Location, ZipkinConvertible<List<ZipkinSpan>, ZipkingSubTraceImpl> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZipkingSubTraceImpl.class);

	private static final String KEY_HTTP_URL = "http.url";

	private static final String KEY_ROOT = "ROOT";
	private static final String KEY_NON_ROOT = "NON_ROOT";

	private Trace containingTrace;
	private ZipkinCallable root;

	private long subTraceId;

	private ZipkinSpan rootSpan;
	private int size;

	@Override
	public ZipkingSubTraceImpl fromZipkin(List<ZipkinSpan> input) {

		Map<String, List<ZipkinSpan>> spansPerType = input.stream().collect(Collectors.groupingBy(span -> span.getParentId() == null ? KEY_ROOT : KEY_NON_ROOT));

		if (spansPerType.get(KEY_ROOT).size() != 1) {
			throw new IllegalArgumentException(
					"Cannot create SubTrace from " + spansPerType.get(KEY_ROOT).size() + " root spans! Containing trace is " + (containingTrace == null ? null : containingTrace.getIdentifier()));
		}

		setupFromRootSpan(spansPerType.get(KEY_ROOT).get(0), spansPerType.get(KEY_NON_ROOT));

		return this;
	}

	private void setupFromRootSpan(ZipkinSpan root, List<ZipkinSpan> remaining) {
		ZipkinSubTraceBundle subTrace = new ZipkinSubTraceBundle(root, remaining);

		this.rootSpan = root;
		this.size = (remaining == null ? 0 : remaining.size()) + 1;
		this.root = ZipkinCallableFactory.INSTANCE.createForSubTrace(subTrace);
		this.root.withContainingSubTrace(this).fromZipkin(subTrace);
	}

	public ZipkingSubTraceImpl withContainingTrace(Trace containingTrace) {
		this.containingTrace = containingTrace;
		setIdentifier(containingTrace.getIdentifier().toString() + "-subtrace");
		this.subTraceId = containingTrace.getTraceId();

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
		String host = Optional.ofNullable(rootSpan.getLocalEndpoint().getServiceName()).orElse(extractUrl().getHost());
		return Optional.ofNullable(host).orElse(rootSpan.getLocalEndpoint().getIpv4());
	}

	private URL extractUrl() {
		String url = rootSpan.getTags().get(KEY_HTTP_URL);

		if (url != null) {
			try {
				return new URL(url);
			} catch (MalformedURLException e) {
				LOGGER.error("Cannot parse URL!", e);
			}
		}

		return null;
	}

	@Override
	public Optional<String> getNodeType() {
		return Optional.ofNullable(rootSpan.getLocalEndpoint().getServiceName());
	}

	@Override
	public int getPort() {
		if (rootSpan.getLocalEndpoint().getPort() > 0) {
			return rootSpan.getLocalEndpoint().getPort();
		} else if (extractUrl() != null) {
			return extractUrl().getPort();
		} else {
			return -1;
		}
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
