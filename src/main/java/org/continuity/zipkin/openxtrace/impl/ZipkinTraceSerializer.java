package org.continuity.zipkin.openxtrace.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.dflt.impl.serialization.OPENxtraceSerializationFactory;
import org.spec.research.open.xtrace.dflt.impl.serialization.OPENxtraceSerializationFormat;
import org.spec.research.open.xtrace.dflt.impl.serialization.OPENxtraceSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 *
 * @author Henning Schulz
 *
 */
public class ZipkinTraceSerializer extends StdSerializer<ZipkinTraceImpl> {

	private static final long serialVersionUID = 4215236153072773922L;

	public ZipkinTraceSerializer() {
		this(null);
	}

	protected ZipkinTraceSerializer(Class<ZipkinTraceImpl> t) {
		super(t);
	}

	@Override
	public void serialize(ZipkinTraceImpl trace, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeRawValue(serializeTraceToJsonString(trace));
	}

	private String serializeTraceToJsonString(Trace trace) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		OPENxtraceSerializer serializer = OPENxtraceSerializationFactory.getInstance().getSerializer(OPENxtraceSerializationFormat.JSON);
		serializer.prepare(out);
		serializer.writeTrace(trace);

		return out.toString(Charset.defaultCharset().name());
	}

}
