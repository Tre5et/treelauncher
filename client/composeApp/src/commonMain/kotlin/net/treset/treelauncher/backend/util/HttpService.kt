package net.treset.treelauncher.backend.util

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

open class HttpService @JvmOverloads constructor(
    private val baseUrl: String,
    private val headers: Array<Pair<String, String>> = arrayOf()
) {
    @Throws(IOException::class)
    protected operator fun get(vararg route: Any): Pair<HttpStatusCode, ByteArray> {
        return try {
            evaluateStatus(get("$baseUrl/${route.joinToString(separator = "/")}", headers))
        } catch (e: IOException) {
            throw IOException("Failed to connect to server: $baseUrl/${route.joinToString(separator = "/")}", e)
        }
    }

    @Throws(IOException::class)
    protected fun post(data: ByteArray, vararg route: Any): Pair<HttpStatusCode, ByteArray> {
        return try {
            evaluateStatus(post("$baseUrl/${route.joinToString(separator = "/")}", headers, data))
        } catch (e: IOException) {
            throw IOException("Failed to connect to server: $baseUrl/${route.joinToString(separator = "/")}", e)
        }
    }

    @Throws(IOException::class)
    protected fun post(contentType: String, data: ByteArray, vararg route: Any): Pair<HttpStatusCode, ByteArray> {
        return try {
            evaluateStatus(post("$baseUrl/${route.joinToString(separator = "/")}", headers, contentType, data))
        } catch (e: IOException) {
            throw IOException("Failed to connect to server: $baseUrl/${route.joinToString(separator = "/")}", e)
        }
    }

    @Throws(IOException::class)
    private fun evaluateStatus(result: Pair<HttpStatusCode, ByteArray>): Pair<HttpStatusCode, ByteArray> {
        if (result.first.code < 200 || result.first.code >= 300) {
            throw IOException("The server returned an error code.\nStatus: ${result.first}${if (result.second.isNotEmpty()) "\nMessage: ${String(result.second)}" else ""}")
        }
        return result
    }

    private var client: HttpClient? = null
    private val httpClient: HttpClient
        get() {
            return client?: HttpClient.newHttpClient()
        }

    @Throws(IOException::class)
    private operator fun get(url: String, headers: Array<Pair<String, String>>): Pair<HttpStatusCode, ByteArray> {
        val client = httpClient
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
        headers.forEach {
            requestBuilder.header(
                it.first,
                it.second
            )
        }
        val request = requestBuilder
            .GET()
            .build()
        return makeRequest(client, request)
    }

    @Throws(IOException::class)
    private fun post(
        url: String,
        headers: Array<Pair<String, String>>,
        data: ByteArray
    ): Pair<HttpStatusCode, ByteArray> = post(url, headers, "application/octet-stream", data)

    @Throws(IOException::class)
    private fun post(
        url: String,
        headers: Array<Pair<String, String>>,
        contentType: String,
        data: ByteArray
    ): Pair<HttpStatusCode, ByteArray> {
        val client = httpClient
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", contentType)
        headers.forEach {
            requestBuilder.header(
                it.first,
                it.second
            )
        }
        val request = requestBuilder
            .POST(HttpRequest.BodyPublishers.ofByteArray(data))
            .build()
        return makeRequest(client, request)
    }

    @Throws(IOException::class)
    private fun makeRequest(client: HttpClient, request: HttpRequest): Pair<HttpStatusCode, ByteArray> {
        val response: HttpResponse<ByteArray> = try {
            client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        } catch (e: InterruptedException) {
            throw IOException("The request was interrupted", e)
        } catch (e: Exception) {
            throw IOException("Failed to connect to server", e)
        }
        return Pair(HttpStatusCode.getById(response.statusCode()), response.body())
    }

    enum class HttpStatusCode(val code: Int, val text: String) {
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

        override fun toString(): String {
            return "$code $text"
        }

        companion object {
            fun getById(value: Int): HttpStatusCode {
                for (status in entries) {
                    if (status.code == value) return status
                }
                return UNKNOWN
            }
        }
    }
}
