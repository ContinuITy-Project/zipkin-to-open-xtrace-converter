package org.continuity.zipkin.openxtrace;

import java.io.IOException;
import java.util.Arrays;

import org.continuity.zipkin.openxtrace.conversion.ZipkinConverter;
import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
import org.spec.research.open.xtrace.api.core.Trace;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/open-xtrace")
public class OpenXtraceController {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		RestTemplate restTemplate = new RestTemplate();

		String json = restTemplate.getForObject("http://172.16.145.68:9411/api/v2/traces?limit=100", String.class);

		ObjectMapper mapper = new ObjectMapper();

		ZipkinSpan[][] traces = mapper.readValue(json, ZipkinSpan[][].class);

		ZipkinConverter converter = new ZipkinConverter();

		for (ZipkinSpan[] trace : traces) {
			Trace openXtrace = converter.convert(Arrays.asList(trace));

			System.out.println(openXtrace);
		}
	}

}
