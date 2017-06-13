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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LargeSerializationBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        private ObjectMapper objectMapper;
        private ObjectMapper objectMapperWithAfterburner;
        private Gson gson;

        private List<Map<String, Object>> data;
        private Map<String, Object> item;

        @Setup(Level.Trial)
        public void doSetup() {
            objectMapper = new ObjectMapper();
            objectMapperWithAfterburner = new ObjectMapper().registerModule(new AfterburnerModule());
            gson = new Gson();

            item = new HashMap<>();
            item.put("string", "covfefe");
            item.put("bool", Boolean.FALSE);
            item.put("number", Integer.MAX_VALUE);

            final int count = 1_000_000;
            data = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                data.add(item);
            }
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

        public List<Map<String, Object>> getData() {
            return data;
        }
    }

    @Benchmark
    public void testGsonLargeMap(BenchmarkState state, Blackhole bh) {
        final String json = state.getGson().toJson(state.getData());
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonLargeMap(BenchmarkState state, Blackhole bh) throws JsonProcessingException {
        final byte[] json = state.getObjectMapper().writeValueAsBytes(state.getData());
        bh.consume(json);
    }

    @Benchmark
    public void testJacksonWithAfterburnerLargeMap(BenchmarkState state, Blackhole bh) throws JsonProcessingException {
        final byte[] json = state.getObjectMapperWithAfterburner().writeValueAsBytes(state.getData());
        bh.consume(json);
    }
}
