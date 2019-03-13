package org.continuity.zipkin.openxtrace.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.TreeIterator;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.utils.SubTraceIterator;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkinTraceImpl extends ZipkingIdentifiable implements Trace, ZipkinConvertible<List<ZipkinSpan>, ZipkinTraceImpl> {

	public static final double MICROS_TO_MILLIS_FACTOR = 0.001;

	public static final long MICROS_TO_NANOS_FACTOR = 1000;

	private ZipkingSubTraceImpl root;

	private long traceId;

	@Override
	public ZipkinTraceImpl fromZipkin(List<ZipkinSpan> input) {
		Optional<ZipkinSpan> nonNullSpan = input.stream().filter(s -> s.getTraceId() != null).findFirst();

		if (nonNullSpan.isPresent()) {
			setIdentifier(nonNullSpan.get().getTraceId());
			this.traceId = new BigInteger(nonNullSpan.get().getTraceId(), 16).longValue();
		}

		this.root = new ZipkingSubTraceImpl().withContainingTrace(this).fromZipkin(input);

		return this;
	}

	@Override
	public long getExclusiveTime() {
		return root.getExclusiveTime();
	}

	@Override
	public long getResponseTime() {
		return root.getResponseTime();
	}

	@Override
	public TreeIterator<Callable> iterator() {
		return root.iterator();
	}

	@Override
	public SubTrace getRoot() {
		return this.root;
	}

	@Override
	public long getTraceId() {
		return traceId;
	}

	@Override
	public int size() {
		return root.size();
	}

	@Override
	public TreeIterator<SubTrace> subTraceIterator() {
		return new SubTraceIterator(root);
	}

}
