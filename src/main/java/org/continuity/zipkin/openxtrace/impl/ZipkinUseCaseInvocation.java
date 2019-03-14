package org.continuity.zipkin.openxtrace.impl;

import org.spec.research.open.xtrace.api.core.callables.UseCaseInvocation;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkinUseCaseInvocation extends ZipkinNestingCallable implements UseCaseInvocation {

	@Override
	public String getUseCaseName() {
		return getSpan().getName();
	}

	@Override
	protected boolean isKnownTag(ZipkinTagInformation tag) {
		return false;
	}

}
