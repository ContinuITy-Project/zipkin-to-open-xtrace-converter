package org.continuity.zipkin.openxtrace.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.continuity.zipkin.openxtrace.data.ZipkinSpan;
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

		String cookie = getSpan().extractCookies();
		if (cookie != null) {
			headers.put(KEY_COOKIE, cookie);
		}

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
		String methodName = getSpan().extractHttpMethod();

		HTTPMethod method = null;

		if (methodName != null) {
			method = HTTPMethod.valueOf(methodName);
		}

		if (method == null) {
			String upperCaseName = getSpan().getName().toUpperCase();

			for (HTTPMethod m : HTTPMethod.values()) {
				if (upperCaseName.contains(m.name())) {
					method = m;
					break;
				}
			}
		}

		return Optional.ofNullable(method);
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

	@Override
	protected boolean isKnownTag(ZipkinTagInformation tag) {
		switch (tag.getName()) {
		case ZipkinSpan.KEY_HTTP_METHOD:
			return Objects.equals(tag.getValue(), getRequestMethod().orElse(HTTPMethod.GET).toString());
		case ZipkinSpan.KEY_BODY:
			return Objects.equals(tag.getValue(), getRequestBody().orElse(null));
		case ZipkinSpan.KEY_HTTP_STATUS_CODE:
			return Objects.equals(tag.getValue(), getResponseCode().orElse(-1L).toString());
		case ZipkinSpan.KEY_HTTP_PATH:
		case ZipkinSpan.KEY_HTTP_URL:
			return Objects.equals(tag.getValue(), getUri());
		case ZipkinSpan.KEY_COOKIE:
			return true;
		case ZipkinSpan.KEY_HTTP_HOST:
		default:
			return false;
		}
	}

}
