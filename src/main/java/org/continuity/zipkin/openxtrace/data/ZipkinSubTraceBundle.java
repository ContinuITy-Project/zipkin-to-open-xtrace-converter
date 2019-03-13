package org.continuity.zipkin.openxtrace.data;

import java.util.List;

public class ZipkinSubTraceBundle {

	private ZipkinSpan root;

	private List<ZipkinSpan> remaining;

	public ZipkinSubTraceBundle(ZipkinSpan root, List<ZipkinSpan> remaining) {
		this.root = root;
		this.remaining = remaining;
	}

	public ZipkinSpan getRoot() {
		return root;
	}

	public void setRoot(ZipkinSpan root) {
		this.root = root;
	}

	public List<ZipkinSpan> getRemaining() {
		return remaining;
	}

	public void setRemaining(List<ZipkinSpan> remaining) {
		this.remaining = remaining;
	}

}
