package org.continuity.zipkin.openxtrace.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.continuity.zipkin.openxtrace.data.ZipkinSubTraceBundle;
import org.spec.research.open.xtrace.api.core.AdditionalInformation;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;

/**
 *
 * @author Henning Schulz
 *
 */
public abstract class ZipkinCallable extends ZipkingIdentifiable implements Callable {

	private ZipkingSubTraceImpl containingSubTrace;

	private ZipkinSpan span;

	private NestingCallable parent;

	/**
	 * <i>Implementations should override this method and call {@code super.fromZipkin(input)}
	 * first!</i>
	 */
	public ZipkinCallable fromZipkin(ZipkinSubTraceBundle input) {
		this.span = input.getRoot();
		setIdentifier(span.getId());
		return this;
	}

	public ZipkinCallable withContainingSubTrace(ZipkingSubTraceImpl containingSubTrace) {
		this.containingSubTrace = containingSubTrace;
		return this;
	}

	public ZipkinCallable withParent(NestingCallable parent) {
		this.parent = parent;
		return this;
	}

	protected ZipkinSpan getSpan() {
		return span;
	}

	@Override
	public Optional<Collection<AdditionalInformation>> getAdditionalInformation() {
		return Optional.ofNullable(span.getTags().entrySet().stream().map(ZipkinTagInformation::fromEntry).filter(not(this::isKnownTag)).collect(Collectors.toList()));
	}

	@Override
	public <T extends AdditionalInformation> Optional<Collection<T>> getAdditionalInformation(Class<T> type) {
		Optional<Collection<AdditionalInformation>> additionalInfo = getAdditionalInformation();

		if (additionalInfo.isPresent()) {
			return Optional.ofNullable(additionalInfo.get().stream().filter(info -> type.isAssignableFrom(info.getClass())).map(type::cast).collect(Collectors.toList()));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public SubTrace getContainingSubTrace() {
		return containingSubTrace;
	}

	@Override
	public Optional<List<String>> getLabels() {
		return Optional.empty();
	}

	@Override
	public NestingCallable getParent() {
		return parent;
	}

	@Override
	public Optional<Long> getThreadID() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getThreadName() {
		return Optional.empty();
	}

	@Override
	public long getTimestamp() {
		return Math.round(span.getTimestamp() * ZipkinTraceImpl.MICROS_TO_MILLIS_FACTOR);
	}

	protected abstract boolean isKnownTag(ZipkinTagInformation tag);

	private <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}

}
