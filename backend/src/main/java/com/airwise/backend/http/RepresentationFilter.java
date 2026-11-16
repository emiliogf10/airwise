package com.airwise.backend.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

/** Bounded raw bytes before MVC/Jackson; Content-Length is only an early rejection hint. */
final class RepresentationFilter implements Filter {
    private final ErrorWriter errors;
    RepresentationFilter(ErrorWriter errors) { this.errors = errors; }
    @Override public void doFilter(ServletRequest incoming, ServletResponse outgoing, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) incoming;
        HttpServletResponse response = (HttpServletResponse) outgoing;
        if (!RoutePolicy.PUBLIC.matches(request)) { chain.doFilter(request, response); return; }
        try {
            String query = request.getQueryString();
            if (query != null && query.getBytes(StandardCharsets.UTF_8).length > 4096) throw ApiException.invalidRequest();
            HttpServletRequest body = request;
            if ("GET".equals(request.getMethod()) && (request.getContentLengthLong() > 0
                    || request.getHeader("Transfer-Encoding") != null)) throw ApiException.invalidRequest();
            if ("POST".equals(request.getMethod())) {
                if (query != null && !query.isEmpty()) throw ApiException.invalidRequest();
                requireJson(request);
                if (request.getContentLengthLong() > 65536) throw ApiException.failure(413, "PAYLOAD_TOO_LARGE");
                byte[] bytes;
                try (BodyReadDeadline deadline = BodyReadDeadline.open(request, response)) {
                    bytes = request.getInputStream().readNBytes(65537);
                    deadline.remainingMillis();
                }
                if (bytes.length > 65536) throw ApiException.failure(413, "PAYLOAD_TOO_LARGE");
                try {
                    StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes));
                } catch (CharacterCodingException e) { throw ApiException.invalidRequest(); }
                body = new BytesRequest(request, bytes);
            }
            if (!acceptsJson(request)) throw ApiException.failure(406, "NOT_ACCEPTABLE");
            chain.doFilter(body, response);
        } catch (ApiException e) { errors.write(request, response, e); }
        catch (IOException e) { errors.write(request, response, 400, "INVALID_REQUEST"); }
    }
    private static void requireJson(HttpServletRequest request) {
        String encoding = request.getHeader("Content-Encoding");
        if (encoding != null && !encoding.trim().equalsIgnoreCase("identity"))
            throw ApiException.failure(415, "UNSUPPORTED_MEDIA_TYPE");
        try {
            MediaType type = MediaType.parseMediaType(request.getContentType() == null ? "" : request.getContentType());
            if (!type.getType().equalsIgnoreCase("application") || !type.getSubtype().equalsIgnoreCase("json")
                    || (type.getCharset() != null && !type.getCharset().equals(StandardCharsets.UTF_8))
                    || type.getParameters().keySet().stream().anyMatch(p -> !p.equalsIgnoreCase("charset")))
                throw ApiException.failure(415, "UNSUPPORTED_MEDIA_TYPE");
        } catch (IllegalArgumentException e) { throw ApiException.failure(415, "UNSUPPORTED_MEDIA_TYPE"); }
    }
    static boolean acceptsJson(HttpServletRequest request) {
        List<String> values = Collections.list(request.getHeaders("Accept"));
        if (values.isEmpty()) return true;
        int specificity = -1; double quality = 0;
        try {
            for (MediaType type : MediaType.parseMediaTypes(values)) {
                if (!type.isCompatibleWith(MediaType.APPLICATION_JSON)) continue;
                // A charset/media parameter narrows the representation, not just its quality.
                if (type.getParameters().entrySet().stream().anyMatch(e -> !e.getKey().equalsIgnoreCase("q")
                        && !(e.getKey().equalsIgnoreCase("charset") && e.getValue().equalsIgnoreCase("UTF-8")))) continue;
                int current = type.isWildcardType() ? 0 : type.isWildcardSubtype() ? 1 : 2;
                if (current > specificity) { specificity = current; quality = type.getQualityValue(); }
                else if (current == specificity) quality = Math.max(quality, type.getQualityValue());
            }
        } catch (IllegalArgumentException e) { return false; }
        return quality > 0;
    }
    private static final class BytesRequest extends HttpServletRequestWrapper {
        private final byte[] bytes;
        BytesRequest(HttpServletRequest request, byte[] bytes) { super(request); this.bytes = bytes; }
        @Override public int getContentLength() { return bytes.length; }
        @Override public long getContentLengthLong() { return bytes.length; }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public BufferedReader getReader() { return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)); }
        @Override public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            return new ServletInputStream() {
                @Override public int read() { return input.read(); }
                @Override public int read(byte[] b, int off, int len) { return input.read(b, off, len); }
                @Override public boolean isFinished() { return input.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) { throw new IllegalStateException("Synchronous demo request"); }
            };
        }
    }
}
