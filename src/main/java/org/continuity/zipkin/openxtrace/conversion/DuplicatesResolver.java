package org.continuity.zipkin.openxtrace.conversion;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zipkin2.Span.Kind;

/**
 *
 *
 * @author Henning Schulz
 *
 */
public class DuplicatesResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(DuplicatesResolver.class);

	/**
	 * If there are multiple spans in the list, resolves it to one or multiple spans with different
	 * IDs. Otherwise, the input list is returned.
	 *
	 * @param spans
	 *            A list of spans with the same ID.
	 * @return A list of spans with different IDs.
	 */
	public List<ZipkinSpan> resolve(List<ZipkinSpan> spans) {
		String appliedRule = null;
		List<ZipkinSpan> resultingSpans = new ArrayList<>();
		String origId = spans.get(0).getId();
		int origSize = spans.size();

		if (spans.size() <= 1) {
			return spans;
		} else if (spans.size() == 2) {
			List<ZipkinSpan> clientServer = tryToResolveServerClient(spans.get(0), spans.get(1));

			if (clientServer.size() > 0) {
				appliedRule = "as CLIENT -> SERVER";
				resultingSpans = clientServer;
			}
		}

		if (appliedRule == null) {
			List<ZipkinSpan> filtered = spans.stream().filter(this::isNotPointless).collect(Collectors.toList());

			if (filtered.size() <= 1) {
				appliedRule = "by filtering the pointless ones (no name, service name, http.url)";
				resultingSpans = filtered;
			}
		}

		if (appliedRule == null) {
			LOGGER.error("There are {} spans in trace {} with ID {} that could not be resolved! Randomly taking one!", spans.size(), spans.get(0).getTraceId(), spans.get(0).getId());
			return Collections.singletonList(spans.get(0));
		} else {
			LOGGER.warn("There were {} spans in trace {} with ID {}! Resolved it {}.", origSize, spans.get(0).getTraceId(), origId, appliedRule);
			return resultingSpans;
		}
	}

	private List<ZipkinSpan> tryToResolveServerClient(ZipkinSpan first, ZipkinSpan second) {
		List<ZipkinSpan> resultingSpans = new ArrayList<>();

		switch (first.getKind()) {
		case CLIENT:
			if (second.getKind() == Kind.SERVER) {
				resolveClientServer(first, second);
				resultingSpans.add(first);
				resultingSpans.add(second);
			}
			break;
		case SERVER:
			if (second.getKind() == Kind.CLIENT) {
				resolveClientServer(second, first);
				resultingSpans.add(first);
				resultingSpans.add(second);
			}
			break;
		default:
			break;
		}

		return resultingSpans;
	}

	/**
	 * Changes the span IDs so that {@code client} calls {@code server}.
	 *
	 * @param client
	 *            A span of kind {@link Kind#CLIENT}.
	 * @param server
	 *            A span of kind {@link Kind#SERVER}.
	 */
	private void resolveClientServer(ZipkinSpan client, ZipkinSpan server) {
		String newId = String.format("%0" + client.getId().length() + "x", new BigInteger(client.getId(), 16).add(BigInteger.ONE));
		client.setId(newId);
		server.setParentId(newId);

		LOGGER.debug("Orig ID: {}, new ID: {}", server.getId(), newId);
	}

	private boolean isNotPointless(ZipkinSpan span) {
		return (span.getName() != null) || (span.getLocalEndpoint().getServiceName() != null) || (span.getTags().get("http.url") != null);
	}

}
