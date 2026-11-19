package com.airwise.backend.flightprovider.admission;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class AdmissionIsolationTests {
    @Test
    void compiledAdmissionHasNoNetworkProcessSpringOrPersistenceDependencies() throws Exception {
        Path classes = Path.of(AdmissionService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var forbidden = List.of("java/net/", "javax/net/", "sun/net/", "java/nio/channels/SocketChannel",
                "java/nio/channels/DatagramChannel", "java/lang/ProcessBuilder",
                "org/apache/http/", "org/apache/hc/", "okhttp3/", "org/springframework/",
                "jakarta/persistence/", "java/sql/", "com/airwise/backend/flightprovider/demo/",
                "com/airwise/backend/flightsearch/");
        int inspected = 0;
        try (var files = Files.walk(classes.resolve("com/airwise/backend/flightprovider/admission"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                String pool = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
                for (String banned : forbidden) assertFalse(pool.contains(banned), file.getFileName() + " references " + banned);
                inspected++;
            }
        }
        assertTrue(inspected >= 5);
    }
}
