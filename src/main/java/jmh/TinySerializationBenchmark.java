package jmh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.gson.Gson;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collections;
import java.util.Map;

public class TinySerializationBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        final String tinyString = "covfefe";
        final Map<String, Object> tinyMap = Collections.singletonMap("foobar", "covfefe");

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
    }

    @Benchmark
    public void testGsonTinyString(BenchmarkState state, Blackhole bh) {
        final String json = state.getGson().toJson(state.tinyString);
        bh.consume(json);
    }

    @Benchmark
    public void testGsonTinyMap(BenchmarkState state, Blackhole bh) {
        final String json = state.getGson().toJson(state.tinyMap);
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonTinyString(BenchmarkState state, Blackhole bh) throws JsonProcessingException {
        final byte[] json = state.getObjectMapper().writeValueAsBytes(state.tinyString);
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonTinyMap(BenchmarkState state, Blackhole bh) throws JsonProcessingException {
        final byte[] json = state.getObjectMapper().writeValueAsBytes(state.tinyMap);
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonWithAfterburnerTinyString(BenchmarkState state, Blackhole bh) throws JsonProcessingException {
        final byte[] json = state.getObjectMapperWithAfterburner().writeValueAsBytes(state.tinyString);
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonWithAfterburnerTinyMap(BenchmarkState state, Blackhole bh) throws JsonProcessingException {
        final byte[] json = state.getObjectMapperWithAfterburner().writeValueAsBytes(state.tinyMap);
        bh.consume(json);
    }

}
