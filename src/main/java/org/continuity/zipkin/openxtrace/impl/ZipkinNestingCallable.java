package org.continuity.zipkin.openxtrace.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.conversion.ZipkinCallableFactory;
import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.spec.research.open.xtrace.api.core.TreeIterator;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;
import org.spec.research.open.xtrace.api.utils.CallableIterator;

/**
 *
 * @author Henning Schulz
 *
 */
public abstract class ZipkinNestingCallable extends ZipkinTimedCallable implements NestingCallable {

	private static final String KEY_REMAINING = "REMAINING";

	private List<Callable> callees;

	@Override
	public ZipkinCallable fromZipkin(ZipkinSubTraceBundle input) {
		super.fromZipkin(input);

		Map<String, List<ZipkinSpan>> spansPerParent = input.getRemaining().stream().collect(Collectors.groupingBy(this::thisIdOrRest));

		if (spansPerParent.get(getSpan().getId()) != null) {
			this.callees = spansPerParent.get(getSpan().getId()).stream().map(child -> new ZipkinSubTraceBundle(child, spansPerParent.get(KEY_REMAINING)))
					.map(ZipkinCallableFactory.INSTANCE::createForSubTrace).collect(Collectors.toList());
		}

		return this;
	}

	private String thisIdOrRest(ZipkinSpan child) {
		if (Objects.equals(getSpan().getId(), child.getParentId())) {
			return child.getParentId();
		} else {
			return KEY_REMAINING;
		}
	}

	@Override
	public long getExclusiveTime() {
		long subTime = getCallees(TimedCallable.class).stream().mapToLong(TimedCallable::getResponseTime).sum();
		return getResponseTime() - subTime;
	}

	@Override
	public TreeIterator<Callable> iterator() {
		return new CallableIterator(this);
	}

	@Override
	public List<Callable> getCallees() {
		return callees;
	}

	@Override
	public <T extends Callable> List<T> getCallees(Class<T> type) {
		return getCallees().stream().filter(c -> type.isAssignableFrom(c.getClass())).map(type::cast).collect(Collectors.toList());
	}

	@Override
	public int getChildCount() {
		return getCallees().size();
	}

}
