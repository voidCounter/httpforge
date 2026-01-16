package com.httpforge.routing;

import com.httpforge.http.HttpRequest;
import com.httpforge.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class RouterTest {

    private Router router;

    @BeforeEach
    void setUp() {
        router = new Router();
    }

    @Test
    void testAddAndRouteSimpleGetRequest() {
        router.addRoute("GET", "/hello", request ->
            HttpResponse.ok("Hello World")
        );

        HttpRequest request = new HttpRequest("GET", "/hello", new HashMap<>(), "");
        HttpResponse response = router.route(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("Hello World", response.getBody());
    }

    @Test
    void testRouteNotFoundReturns404() {
        HttpRequest request = new HttpRequest("GET", "/nonexistent", new HashMap<>(), "");
        HttpResponse response = router.route(request);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("404 Not Found"));
        assertTrue(response.getBody().contains("/nonexistent"));
    }

    @Test
    void testRouteMethodMatching() {
        router.addRoute("GET", "/test", request ->
            HttpResponse.ok("GET response")
        );
        router.addRoute("POST", "/test", request ->
            HttpResponse.ok("POST response")
        );

        HttpRequest getRequest = new HttpRequest("GET", "/test", new HashMap<>(), "");
        HttpRequest postRequest = new HttpRequest("POST", "/test", new HashMap<>(), "");

        assertEquals("GET response", router.route(getRequest).getBody());
        assertEquals("POST response", router.route(postRequest).getBody());
    }

    @Test
    void testRouteCaseInsensitiveMethod() {
        router.addRoute("GET", "/test", request ->
            HttpResponse.ok("Success")
        );

        HttpRequest request = new HttpRequest("get", "/test", new HashMap<>(), "");
        HttpResponse response = router.route(request);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testRouteWithRequestData() {
        router.addRoute("POST", "/echo", request ->
            HttpResponse.ok("Echo: " + request.getBody())
        );

        HttpRequest request = new HttpRequest("POST", "/echo", new HashMap<>(), "test data");
        HttpResponse response = router.route(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("Echo: test data", response.getBody());
    }

    @Test
    void testMultipleRoutes() {
        router.addRoute("GET", "/", request -> HttpResponse.ok("Home"));
        router.addRoute("GET", "/about", request -> HttpResponse.ok("About"));
        router.addRoute("GET", "/contact", request -> HttpResponse.ok("Contact"));

        assertEquals("Home", router.route(
            new HttpRequest("GET", "/", new HashMap<>(), "")).getBody());
        assertEquals("About", router.route(
            new HttpRequest("GET", "/about", new HashMap<>(), "")).getBody());
        assertEquals("Contact", router.route(
            new HttpRequest("GET", "/contact", new HashMap<>(), "")).getBody());
    }

    @Test
    void testRouteHandlerCanAccessHeaders() {
        router.addRoute("GET", "/header-test", request -> {
            String userAgent = request.getHeader("User-Agent");
            return HttpResponse.ok("User-Agent: " + userAgent);
        });

        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "TestClient/1.0");

        HttpRequest request = new HttpRequest("GET", "/header-test", headers, "");
        HttpResponse response = router.route(request);

        assertEquals("User-Agent: TestClient/1.0", response.getBody());
    }
}

