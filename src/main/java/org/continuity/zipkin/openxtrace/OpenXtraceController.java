package org.continuity.zipkin.openxtrace;

import static java.util.Comparator.comparingLong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.conversion.ZipkinConverter;
import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spec.research.open.xtrace.api.core.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Henning Schulz
 *
 */
@RestController
@RequestMapping("/open-xtrace")
public class OpenXtraceController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenXtraceController.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ZipkinConverter converter;

	@Value("${zipkin.base-url}")
	private String zipkinBaseUrl;

	@Value("${zipkin.limit-per-request}")
	private int limitPerRequest;

	@RequestMapping(path = "/get", method = RequestMethod.GET)
	public List<Trace> getOpenXtraces(Date fromDate, Date toDate) throws JsonParseException, JsonMappingException, IOException {
		LOGGER.info("Retrieving and converting Zipkin traces between {} and {} in slices of max {} traces...", fromDate, toDate, limitPerRequest);

		Set<Trace> traces = new TreeSet<>(comparingLong(Trace::getTraceId));

		long endTs = toDate.getTime();
		long startTs = fromDate.getTime();
		long oldestTrace = 0;
		long preOldestTrace = -1;

		while ((endTs > startTs) && (oldestTrace != preOldestTrace)) {
			Set<Trace> current = getAndConvertTraces(endTs, endTs - startTs);

			if (current.size() > 0) {
				Trace oldest = getOldest(current);
				endTs = oldest.getRoot().getRoot().getTimestamp();

				preOldestTrace = oldestTrace;
				oldestTrace = oldest.getTraceId();

				traces.addAll(current);
			} else {
				break;
			}
		}

		List<Trace> traceList = new ArrayList<>(traces);
		Collections.sort(traceList, comparingLong(this::getTimestamp));

		return traceList;
	}

	private Set<Trace> getAndConvertTraces(long endTs, long lookback) throws JsonParseException, JsonMappingException, IOException {
		String json = restTemplate.getForObject(zipkinBaseUrl + "/api/v2/traces?endTs=" + endTs + "&lookback=" + lookback + "&limit=" + limitPerRequest, String.class);
		ZipkinSpan[][] traces = objectMapper.readValue(json, ZipkinSpan[][].class);

		LOGGER.info("Got {} traces with endTs={} and lookback={}.", traces.length, endTs, lookback);

		return Arrays.stream(traces).map(Arrays::asList).map(converter::convert).collect(Collectors.toSet());
	}

	private Trace getOldest(Set<Trace> traces) {
		return traces.stream().reduce((a, b) -> getTimestamp(a) <= getTimestamp(b) ? a : b).get();
	}

	private long getTimestamp(Trace trace) {
		if ((trace != null) && (trace.getRoot() != null) && (trace.getRoot().getRoot() != null)) {
			return trace.getRoot().getRoot().getTimestamp();
		} else {
			return -1;
		}
	}

}
