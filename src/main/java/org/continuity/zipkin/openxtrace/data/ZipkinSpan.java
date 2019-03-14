package org.continuity.zipkin.openxtrace.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import zipkin2.Span.Kind;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipkinSpan {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZipkinSpan.class);

	private static final String KEY_HTTP_URL = "http.url";
	private static final String KEY_HTTP_PATH = "http.path";
	private static final String KEY_HTTP_METHOD = "http.method";
	private static final String KEY_HTTP_STATUS_CODE = "http.status_code";
	private static final String KEY_BODY = "body";
	private static final String KEY_COOKIE = "cookie";

	private static final Collection<String> KNOWN_TAGS = Arrays.asList(KEY_HTTP_URL, KEY_HTTP_PATH, KEY_HTTP_METHOD, KEY_HTTP_STATUS_CODE, KEY_BODY, KEY_COOKIE);

	private String traceId;
	private String parentId;
	private String id;
	private Kind kind;
	private String name;
	private long timestamp;
	private long duration;
	private final ZipkinEndpoint localEndpoint = new ZipkinEndpoint();
	private final ZipkinEndpoint remoteEndpoint = new ZipkinEndpoint();
	private final List<ZipkinAnnotation> annotations = new ArrayList<>();
	private final Map<String, String> tags = new HashMap<>();
	private int flags;
	private boolean shared;

	@JsonIgnore
	public String getParentIdOrElse(String elseValue) {
		return parentId == null ? elseValue : parentId;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Timestamp in microseconds.
	 *
	 * @return
	 */
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Duration in microseconds.
	 *
	 * @return
	 */
	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public ZipkinEndpoint getLocalEndpoint() {
		return localEndpoint;
	}

	public ZipkinEndpoint getRemoteEndpoint() {
		return remoteEndpoint;
	}

	public List<ZipkinAnnotation> getAnnotations() {
		return annotations;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	@JsonIgnore
	public String extractHost() {
		String host = getLocalEndpoint().getServiceName();

		if (host == null) {
			URI url;
			host = (url = extractUri()) == null ? null : url.getHost();
		}

		return Optional.ofNullable(host).orElse(getLocalEndpoint().getIpv4());
	}

	@JsonIgnore
	public int extractPort() {
		if (getLocalEndpoint().getPort() > 0) {
			return getLocalEndpoint().getPort();
		} else if (extractUri() != null) {
			return extractUri().getPort();
		} else {
			return -1;
		}
	}

	@JsonIgnore
	public String extractPath() {
		String path = getTags().get(KEY_HTTP_PATH);

		if (path == null) {
			URI url;
			path = (url = extractUri()) == null ? null : url.getPath();
		}

		return path;
	}

	@JsonIgnore
	public String extractHttpMethod() {
		return getTags().get(KEY_HTTP_METHOD);
	}

	private URI extractUri() {
		String url = getTags().get(KEY_HTTP_URL);

		if (url != null) {
			try {
				return new URI(url);
			} catch (URISyntaxException e) {
				LOGGER.error("Cannot parse URL!", e);
			}
		}

		return null;
	}

	@JsonIgnore
	public String extractBody() {
		return getTags().get(KEY_BODY);
	}

	@JsonIgnore
	public Long extractStatusCode() {
		String statusCode = getTags().get(KEY_HTTP_STATUS_CODE);

		if (statusCode == null) {
			return null;
		} else {
			return Long.parseLong(statusCode);
		}
	}

	@JsonIgnore
	public String extractCookies() {
		return getTags().get(KEY_COOKIE);
	}

	@JsonIgnore
	public Map<String, String> extractQueryParameters() {
		URI url;
		String query = (url = extractUri()) == null ? null : url.getQuery();

		if (query != null) {
			Map<String, String> params = new LinkedHashMap<>();
			Arrays.stream(query.split("&")).map(String::trim).map(s -> s.split("=")).forEach(s -> params.put(s[0], s.length > 1 ? s[1] : null));
			return params;
		} else {
			return Collections.emptyMap();
		}
	}

	@JsonIgnore
	public Map<String, String> extractSpecialTags() {
		return getTags().keySet().stream().filter(not(KNOWN_TAGS::contains)).collect(Collectors.toMap(x -> x, tags::get));
	}

	private <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}

}
