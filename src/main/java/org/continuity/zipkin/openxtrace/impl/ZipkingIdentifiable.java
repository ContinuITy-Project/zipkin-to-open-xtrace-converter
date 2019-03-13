package org.continuity.zipkin.openxtrace.impl;

import java.util.Optional;

import org.spec.research.open.xtrace.api.core.Identifiable;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkingIdentifiable implements Identifiable {

	private Object identifier;

	@Override
	public Optional<Object> getIdentifier() {
		return Optional.ofNullable(this.identifier);
	}

	@Override
	public void setIdentifier(Object identifier) {
		this.identifier = identifier;
	}

}
