package org.continuity.zipkin.openxtrace.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import zipkin2.Span.Kind;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipkinSpan {

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

}
