package jmh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

public class LargeDeserializationBenchmark {
    @State(Scope.Thread)
    public static class BenchmarkState {
        private final URL resource = Resources.getResource("data/large.json");
        private final ByteSource byteSource = Resources.asByteSource(resource);

        private ObjectMapper objectMapper;
        private ObjectMapper objectMapperWithAfterburner;
        private Gson gson;

        @Setup(Level.Trial)
        public void doSetup() {
            objectMapper = new ObjectMapper();
            objectMapperWithAfterburner = new ObjectMapper().registerModule(new AfterburnerModule());
            gson = new Gson();
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            objectMapper = null;
            objectMapperWithAfterburner = null;
            gson = null;
        }

        public ObjectMapper getObjectMapper() {
            return objectMapper;
        }

        public ObjectMapper getObjectMapperWithAfterburner() {
            return objectMapperWithAfterburner;
        }

        public Gson getGson() {
            return gson;
        }

        public URL getResource() {
            return resource;
        }

        public ByteSource getByteSource() {
            return byteSource;
        }
    }

    @Benchmark
    public void testGsonDeserializeLargeJson(BenchmarkState state, Blackhole bh) throws IOException {
        final Reader reader = new InputStreamReader(state.getByteSource().openStream());
        final Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        final Map<String, Object> json = state.getGson().fromJson(reader, type);
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonDeserializeLargeJson(BenchmarkState state, Blackhole bh) throws IOException {
        final TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {
        };
        final Map<String, Object> json = state.getObjectMapper().readValue(state.getResource(), typeReference);
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonWithAfterburnerDeserializeLargeJson(BenchmarkState state, Blackhole bh) throws IOException {
        final TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {
        };
        final Map<String, Object> json = state.getObjectMapperWithAfterburner().readValue(state.getResource(), typeReference);
        bh.consume(json);
    }
}
