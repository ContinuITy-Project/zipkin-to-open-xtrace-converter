package org.continuity.zipkin.openxtrace.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.spec.research.open.xtrace.api.core.callables.HTTPMethod;
import org.spec.research.open.xtrace.api.core.callables.HTTPRequestProcessing;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkinHttpRequestProcessing extends ZipkinNestingCallable implements HTTPRequestProcessing {

	public static final String KEY_COOKIE = "cookie";

	@Override
	public Optional<Map<String, String>> getHTTPAttributes() {
		return Optional.empty();
	}

	@Override
	public Optional<Map<String, String>> getHTTPHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put(KEY_COOKIE, getSpan().extractCookies());

		return Optional.ofNullable(headers);
	}

	@Override
	public Optional<Map<String, String[]>> getHTTPParameters() {
		Map<String, String[]> params = new LinkedHashMap<>();

		for (Entry<String, String> queryParam : getSpan().extractQueryParameters().entrySet()) {
			params.put(queryParam.getKey(), new String[] { queryParam.getValue() });
		}

		return Optional.ofNullable(params);
	}

	@Override
	public Optional<Map<String, String>> getHTTPSessionAttributes() {
		String cookieString = getSpan().extractCookies();

		if (cookieString != null) {
			LinkedHashMap<String, String> cookies = new LinkedHashMap<>();
			Arrays.stream(cookieString.split(";")).map(String::trim).map(s -> s.split("=")).forEach(s -> cookies.put(s[0], s.length > 1 ? s[1] : null));
			return Optional.ofNullable(cookies);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getRequestBody() {
		return Optional.ofNullable(getSpan().extractBody());
	}

	@Override
	public Optional<HTTPMethod> getRequestMethod() {
		String method = getSpan().extractHttpMethod();

		if (method != null) {
			return Optional.ofNullable(HTTPMethod.valueOf(method));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Long> getResponseCode() {
		return Optional.ofNullable(getSpan().extractStatusCode());
	}

	@Override
	public Optional<Map<String, String>> getResponseHTTPHeaders() {
		return Optional.empty();
	}

	@Override
	public String getUri() {
		return getSpan().extractPath();
	}

}
