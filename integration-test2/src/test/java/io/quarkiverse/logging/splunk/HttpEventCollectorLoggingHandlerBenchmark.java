package io.quarkiverse.logging.splunk;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@State(Scope.Benchmark)
public class HttpEventCollectorLoggingHandlerBenchmark {

    SplunkResource resource;

    Logger logger;

    @Setup
    public void setup() {
        resource = new SplunkResource();
        Map<String, String> splunkInfo = resource.start();

        StringBuffer config = new StringBuffer();
        config.append("handlers = com.splunk.logging.HttpEventCollectorLoggingHandler").append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.url=").append(splunkInfo.get("quarkus.log.handler.splunk.url")).append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.token=29fe2838-cab6-4d17-a392-37b7b8f41f75").append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.batch_interval = 10").append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.batch_size_count = 10").append("\n");

        System.out.println(config);
        try {
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(config.toString().getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger = Logger.getLogger(HttpEventCollectorLoggingHandlerTest.class.getName());
    }

    @TearDown
    public void tearDown() {
        LogManager.getLogManager().reset();
        resource.stop();
    }

    @Benchmark
    @Warmup(iterations = 1, time = 5, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    @Threads(10)
    public void benchSequentialLogs() {
        logger.info("log message");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .forks(1)
                .addProfiler(JavaFlightRecorderProfiler.class, "configName=profile")
                //        .jvmArgs("-Xmx512m", "-Xms512m"
                //            "-Djmh.stack.profiles=" + destinationFolder,
                //            "-Djmh.executor=FJP",
                //            "-Djmh.fr.options=defaultrecording=true,settings=" + profile)
                .include(HttpEventCollectorLoggingHandlerBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
