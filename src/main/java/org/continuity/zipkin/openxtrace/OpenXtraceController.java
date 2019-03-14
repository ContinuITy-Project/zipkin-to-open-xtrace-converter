package org.continuity.zipkin.openxtrace;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.continuity.zipkin.openxtrace.conversion.ZipkinConverter;
import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.spec.research.open.xtrace.api.core.Trace;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ZipkinConverter converter;

	@RequestMapping(path = "/get", method = RequestMethod.GET)
	public List<Trace> getOpenXtraces() throws JsonParseException, JsonMappingException, IOException {
		String json = restTemplate.getForObject("http://172.16.145.68:9411/api/v2/traces?limit=20", String.class);
		ZipkinSpan[][] traces = objectMapper.readValue(json, ZipkinSpan[][].class);

		return Arrays.stream(traces).map(Arrays::asList).map(converter::convert).collect(Collectors.toList());
	}

}
