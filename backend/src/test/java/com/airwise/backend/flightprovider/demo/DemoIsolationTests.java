package com.airwise.backend.flightprovider.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class DemoIsolationTests {

    @Test
    void compiledBusinessComponentHasNoTransportOrExternalProcessDependencies() throws Exception {
        Path classes = Path.of(DemoFlightProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var forbidden = List.of("java/net/", "javax/net/", "sun/net/",
                "java/nio/channels/SocketChannel", "java/nio/channels/DatagramChannel",
                "java/lang/ProcessBuilder", "org/apache/http/", "org/apache/hc/",
                "okhttp3/", "org/springframework/web/client/", "org/springframework/web/reactive/");
        int checked = 0;
        for (String module : List.of("airport", "flightsearch", "flightprovider", "flightcomparison")) {
            Path folder = classes.resolve("com/airwise/backend/" + module);
            try (var files = Files.walk(folder)) {
                for (var file : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                    String pool = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
                    for (var banned : forbidden) {
                        assertFalse(pool.contains(banned), file.getFileName() + " references " + banned);
                    }
                    checked++;
                }
            }
        }
        assertTrue(checked > 0, "The check must inspect compiled business classes");
    }

    @Test
    void domainValuesDoNotDependOnSpringOrPersistence() throws Exception {
        Path classes = Path.of(DemoFlightProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        int checked = 0;
        for (String module : List.of("airport", "flightsearch", "flightcomparison")) {
            try (var files = Files.walk(classes.resolve("com/airwise/backend/" + module + "/domain"))) {
                for (var file : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                    String pool = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
                    assertFalse(pool.contains("org/springframework/"), file.toString());
                    assertFalse(pool.contains("jakarta/persistence/"), file.toString());
                    assertFalse(pool.contains("javax/persistence/"), file.toString());
                    checked++;
                }
            }
        }
        assertTrue(checked > 0);
    }
}
