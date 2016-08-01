package org.fmr.flink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;

import com.google.cloud.dataflow.sdk.coders.AvroCoder;
import com.google.cloud.dataflow.sdk.coders.Coder;

public class AvroDeserializationSchema<T> implements SerializationSchema<T>, DeserializationSchema<T> {

	private static final long serialVersionUID = -1017915709068314410L;

	private final Class<T> avroType;

	private final AvroCoder<T> coder;
	private transient ByteArrayOutputStream out;

	public AvroDeserializationSchema(Class<T> clazz) {
		this.avroType = clazz;
		this.coder = AvroCoder.of(clazz);
		this.out = new ByteArrayOutputStream();
	}

	@Override
	public byte[] serialize(T element) {
		if (out == null) {
			out = new ByteArrayOutputStream();
		}
		try {
			out.reset();
			coder.encode(element, out, Coder.Context.NESTED);
		} catch (IOException e) {
			throw new RuntimeException("Avro encoding failed.", e);
		}
		return out.toByteArray();
	}

	@Override
	public T deserialize(byte[] message) throws IOException {
		System.out.println(new String(message));
		return coder.decode(new ByteArrayInputStream(message), Coder.Context.NESTED);
	}

	@Override
	public boolean isEndOfStream(T nextElement) {
		return false;
	}

	@Override
	public TypeInformation<T> getProducedType() {
		return TypeExtractor.getForClass(avroType);
	}
}
