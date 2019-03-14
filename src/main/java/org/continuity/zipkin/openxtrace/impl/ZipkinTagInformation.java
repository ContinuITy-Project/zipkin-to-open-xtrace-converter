package org.continuity.zipkin.openxtrace.impl;

import java.util.Map.Entry;

import org.spec.research.open.xtrace.api.core.AdditionalInformation;

public class ZipkinTagInformation implements AdditionalInformation {

	private String name;

	private String value;

	public ZipkinTagInformation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public static ZipkinTagInformation fromEntry(Entry<String, String> entry) {
		return new ZipkinTagInformation(entry.getKey(), entry.getValue());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getValue() {
		return value;
	}

}
