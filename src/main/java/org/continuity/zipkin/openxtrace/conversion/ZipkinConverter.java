package org.continuity.zipkin.openxtrace.conversion;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map.Entry;

import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.continuity.zipkin.openxtrace.impl.ZipkinTraceImpl;
import org.spec.research.open.xtrace.api.core.Trace;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkinConverter {

	private final DuplicatesResolver duplicatesResolver = new DuplicatesResolver();

	public Trace convert(List<ZipkinSpan> trace) {
		List<ZipkinSpan> resolved = trace.stream()
				.collect(collectingAndThen(groupingBy(ZipkinSpan::getId),
						map -> map.entrySet().stream().map(Entry::getValue).map(duplicatesResolver::resolve).flatMap(List::stream).collect(toList())));

		return new ZipkinTraceImpl().fromZipkin(resolved);
	}

}
