# Native Memory Tracking (NMT) of Container memory for Java Applications

## What does this Library do ?
This library adds custom java [native memory tracking](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr007.html) metrics to [Micrometer](https://micrometer.io/) (hence to the [Spring Boot actuator metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html)).  

## Why will I ever need Java Native Memory Tracking ?

In a bare-metal  or a virtualized VM environment, deploying an app to WebSphere, WebLogic or JBOSS results in a JVM running on a system with plenty of swap space. That means if they don’t have their memory settings quite right, the worst thing that will happen is that they use a bit of swap space. If it’s just a little swap, they probably won’t even notice it happening. When moving Java applications to container based Platforms like CloudFoundry there is a hard limit on total system memory. With container based systems, there’s much less forgiveness in the system. Exceeding the memory limit by even a byte will result in your app being killed.

Diagnosing and debugging OOM errors with Java applications in Cloud Foundry or in any container based platform like Cloud Foundry, Kubernetes or Docker is difficult. The OS level metrics are often confusing and don't provide any insight unless you are an expert in Linux system internals [spring-boot-memory-performance](https://spring.io/blog/2015/12/10/spring-boot-memory-performance). Like David Syer, I recommend relying on the diagnostics provided by the JVM to track down OOM memory leaks. The verbose GC logs provide insight into the heap portion of container memory. There are a variety of tools available { [HeapAnalyzer](http://www.eclipse.org/mat/), [gceasy](http://gceasy.io/), [pmat](http://ibm.co/1pUjktc) } to triage and analyze java heaps and verbose GC logs; however getting any insight into native aka non-heap portion of the memory is very difficult.  The native memory tracking introduced in the JDK from Java 8 provides valuable insight into the portion of the memory (iceberg) under the heap (water).  The key element of debugging native OOMs is to understand the metrics report and chart the trend-lines to understand the leaking contributor.

# Usage

## Include library in your project

```xml
<dependency>
    <groupId>io.github.glandais-eptica</groupId>
    <artifactId>nmt-metrics</artifactId>
    <version>2.0.4</version>
</dependency>
```

## Non Spring usage

Create a NMTMetrics instance

```java
public class MyProgram {

    private static final NMTMetrics NMT_METRICS = new NMTMetrics();
    
    public static void main(String[] args) {
        //...
    }
}
```

## Spring usage

Add a NMTMetrics bean in your context.
This code snippet will use Spring Micrometer registry.

```java
@Configuration
public class JvmMetricsConfiguration {
    @Bean
    @Lazy(false)
    public NMTMetrics nmtMetrics(MeterRegistry meterRegistry) {
        return new NMTMetrics(meterRegistry, Duration.ofSeconds(10L));
    }
}
```

## Start JVM with option

Start the JVM with command line option: `-XX:NativeMemoryTracking=summary`.

## Use NMT gauges

[Gauges](https://micrometer.io/docs/concepts#_gauges) are added to Micrometer :


* `jvm.memory.nmt.reserved` : Reserved memory gauges, total or per category (see [category list](src/main/java/com/marekcabaj/nmt/bean/NativeMemoryTrackingType.java))
    * `jvm.memory.nmt.reserved{category="total"}` : Total reserved memory
    * `jvm.memory.nmt.reserved{category="java.heap"}` : Reserved memory for Java instances
* `jvm.memory.nmt.committed` : Committed memory gauges, total or per category (see [category list](src/main/java/com/marekcabaj/nmt/bean/NativeMemoryTrackingType.java))
    * `jvm.memory.nmt.committed{category="total"}` : Total committed memory (the "real" memory usage of JVM process)
    * `jvm.memory.nmt.committed{category="java.heap"}` : Committed memory for Java instances

If metrics are exposed with Prometheus, `jvm_memory_nmt_committed_bytes{category="thread"}` will display thread memory usage for instance.

