package org.continuity.zipkin.openxtrace.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
public abstract class ZipkinCallable extends ZipkingIdentifiable implements Callable, ZipkinConvertible<ZipkinSubTraceBundle, ZipkinCallable> {

	private ZipkingSubTraceImpl containingSubTrace;

	private ZipkinSpan span;

	private NestingCallable parent;

	private final Collection<AdditionalInformation> additionalInformation = new ArrayList<>();

	/**
	 * {@inheritDoc} <br>
	 *
	 * <i>Implementations should override this method and call {@code super.fromZipkin(input)}
	 * first!</i>
	 */
	@Override
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

	protected void addAdditionalInformation(AdditionalInformation additionalInfo) {
		additionalInformation.add(additionalInfo);
	}

	@Override
	public Optional<Collection<AdditionalInformation>> getAdditionalInformation() {
		return Optional.ofNullable(additionalInformation);
	}

	@Override
	public <T extends AdditionalInformation> Optional<Collection<T>> getAdditionalInformation(Class<T> type) {
		return Optional.ofNullable(additionalInformation.stream().filter(info -> type.isAssignableFrom(info.getClass())).map(type::cast).collect(Collectors.toList()));
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

}
