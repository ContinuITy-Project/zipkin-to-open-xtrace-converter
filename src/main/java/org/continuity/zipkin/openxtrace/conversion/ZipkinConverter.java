package org.continuity.zipkin.openxtrace.conversion;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.continuity.zipkin.openxtrace.impl.ZipkinTraceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spec.research.open.xtrace.api.core.Trace;

public class ZipkinConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZipkinConverter.class);

	private final DuplicatesResolver duplicatesResolver = new DuplicatesResolver();

	public Trace convert(List<ZipkinSpan> trace) {
		List<ZipkinSpan> resolved = trace.stream()
				.collect(collectingAndThen(groupingBy(ZipkinSpan::getId),
						map -> map.entrySet().stream().map(Entry::getValue).map(duplicatesResolver::resolve).flatMap(List::stream).collect(toList())));

		return new ZipkinTraceImpl().fromZipkin(resolved);
	}

	private void convertRecursivley(List<ZipkinSpan> currentSpans, List<ZipkinSpan> trace, String indent) {
		for (ZipkinSpan span : currentSpans) {
			System.out.print(indent);
			printSpan(span, System.out);

			convertRecursivley(trace.stream().filter(s -> Objects.equals(s.getParentId(), span.getId())).collect(Collectors.toList()), trace, indent + "  ");
		}
	}

	private void printSpan(ZipkinSpan span, PrintStream out) {
		out.print(span.getKind());
		out.print(" ");
		out.print(span.getName());
		out.print(" ");
		out.print(span.getLocalEndpoint().getServiceName());
		out.print(" ");
		out.print(span.getTags().get("http.url"));
		out.print(" d=");
		out.print(span.getDuration());
		out.print(" (");
		out.print(span.getId());
		out.println(")");
	}

}
