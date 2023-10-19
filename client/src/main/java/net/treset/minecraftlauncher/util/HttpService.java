package net.treset.minecraftlauncher.util;

import javafx.util.Pair;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HttpService {
    private final String baseUrl;
    private final List<Pair<String, String>> headers;

    public HttpService(String baseUrl) {
        this(baseUrl, List.of());
    }

    public HttpService(String baseUrl, List<Pair<String, String>> headers) {
        this.baseUrl = baseUrl;
        this.headers = headers;
    }

    protected Pair<HttpUtil.HttpStatusCode, byte[]> get(String route) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result;
        result = get(baseUrl + "/" + route, headers);
        if(result.getKey().getCode() < 200 || result.getKey().getCode() >= 300) {
            throw new IOException("The server returned an error code.\nStatus: " + result.getKey() + (result.getValue() != null && result.getValue().length != 0 ? "\nMessage: " + new String(result.getValue()) : ""));
        }
        return result;
    }

    protected Pair<HttpUtil.HttpStatusCode, byte[]> post(String route, byte[] data) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result;
        result = post(baseUrl + "/" + route, headers, data);
        if(result.getKey().getCode() < 200 || result.getKey().getCode() >= 300) {
            throw new IOException("The server returned an error code.\nStatus: " + result.getKey() + (result.getValue() != null && result.getValue().length == 0 ? "\nMessage: " + new String(result.getValue()) : ""));
        }
        return result;
    }

    private HttpClient client = HttpClient.newHttpClient();

    protected HttpClient getHttpClient() {
        if(client == null) {
            client = HttpClient.newHttpClient();
        }
        return client;
    }

    private Pair<HttpUtil.HttpStatusCode, byte[]> get(String url, List<Pair<String, String>> headers) throws IOException {
        HttpClient client = getHttpClient();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url));
        headers.forEach((pair) -> requestBuilder.header(pair.getKey(), pair.getValue()));
        HttpRequest request = requestBuilder
                .GET()
                .build();

        return makeRequest(client, request);
    }

    private Pair<HttpUtil.HttpStatusCode, byte[]> post(String url, List<Pair<String, String>> headers, byte[] data) throws IOException {
        return post(url, headers, "application/octet-stream", data);
    }


    private Pair<HttpUtil.HttpStatusCode, byte[]> post(String url, List<Pair<String, String>> headers, String contentType, byte[] data) throws IOException {
        HttpClient client = getHttpClient();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", contentType);
        headers.forEach((pair) -> requestBuilder.header(pair.getKey(), pair.getValue()));
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                .build();

        return makeRequest(client, request);
    }

    private Pair<HttpUtil.HttpStatusCode, byte[]> makeRequest(HttpClient client, HttpRequest request) throws IOException {
        HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            throw new IOException("The request was interrupted", e);
        } catch (Exception e) {
            throw new IOException("Failed to connect to server", e);
        }

        return new Pair<>(HttpUtil.HttpStatusCode.getById(response.statusCode()), response.body());
    }

    public enum HttpStatusCode {

        //1xx: Informational
        CONTINUE(100, "Continue"),
        SWITCHING_PROTOCOLS(101, "Switching Protocols"),
        PROCESSING(102, "Processing"),
        EARLY_HINTS(103, "Early Hints"),

        //2xx: Success
        OK(200, "OK"),
        CREATED(201, "Created"),
        ACCEPTED(202, "Accepted"),
        NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
        NO_CONTENT(204, "No Content"),
        RESET_CONTENT(205, "Reset Content"),
        PARTIAL_CONTENT(206, "Partial Content"),
        MULTI_STATUS(207, "Multi-Status"),
        ALREADY_REPORTED(208, "Already Reported"),
        IM_USED(226, "IM Used"),

        //3xx: Redirection
        MULTIPLE_CHOICES(300, "Multiple Choice"),
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        FOUND(302, "Found"),
        SEE_OTHER(303, "See Other"),
        NOT_MODIFIED(304, "Not Modified"),
        USE_PROXY(305, "Use Proxy"),
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        PERMANENT_REDIRECT(308, "Permanent Redirect"),

        //4xx: Client Error
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        PAYMENT_REQUIRED(402, "Payment Required"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
        REQUEST_TIMEOUT(408, "Request Timeout"),
        CONFLICT(409, "Conflict"),
        GONE(410, "Gone"),
        LENGTH_REQUIRED(411, "Length Required"),
        PRECONDITION_FAILED(412, "Precondition Failed"),
        REQUEST_TOO_LONG(413, "Payload Too Large"),
        REQUEST_URI_TOO_LONG(414, "URI Too Long"),
        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
        REQUESTED_RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
        EXPECTATION_FAILED(417, "Expectation Failed"),
        MISDIRECTED_REQUEST(421, "Misdirected Request"),
        UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
        LOCKED(423, "Locked"),
        FAILED_DEPENDENCY(424, "Failed Dependency"),
        TOO_EARLY(425, "Too Early"),
        UPGRADE_REQUIRED(426, "Upgrade Required"),
        PRECONDITION_REQUIRED(428, "Precondition Required"),
        TOO_MANY_REQUESTS(429, "Too Many Requests"),
        REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
        UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

        //5xx: Server Error
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        NOT_IMPLEMENTED(501, "Not Implemented"),
        BAD_GATEWAY(502, "Bad Gateway"),
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        GATEWAY_TIMEOUT(504, "Gateway Timeout"),
        HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
        VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
        INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
        LOOP_DETECTED(508, "Loop Detected"),
        NOT_EXTENDED(510, "Not Extended"),
        NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required"),

        //Unkown
        UNKNOWN(-1, "Unknown");

        private final int code;
        private final String name;

        HttpStatusCode(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return code + " " + name;
        }

        public static HttpStatusCode getById(int value) {
            for(HttpStatusCode status : values()) {
                if(status.code == value) return status;
            }
            return UNKNOWN;
        }
    }
}
