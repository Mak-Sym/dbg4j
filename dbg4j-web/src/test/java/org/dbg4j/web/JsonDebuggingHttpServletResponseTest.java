

package org.dbg4j.web;

import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.dbg4j.web.JsonDebuggingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dbg4j.core.context.DebugContext;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class JsonDebuggingHttpServletResponseTest {

    JsonDebuggingHttpServletResponse jsonResponse;
    HttpServletResponse originalResponse;
    ServletOutputStream servletOutputStream;
    PrintWriter writer;

    @Before
    public void setUp() throws Exception {
        originalResponse = mock(HttpServletResponse.class);
        servletOutputStream = mock(ServletOutputStream.class);
        writer = mock(PrintWriter.class);

        jsonResponse = new JsonDebuggingHttpServletResponse(originalResponse);

        DebugContext.init(null);
    }

    @After
    public void tearDown() throws Exception {
        DebugContext.commit();
    }

    @Test
    public void testDoesApply() throws Exception {
        when(originalResponse.getContentType())
                        //1st call
                .thenReturn("application/xml")
                .thenReturn("application/xml")
                        //2nd call
                .thenReturn("application/json")
                .thenReturn("application/json")
                        //3rd call
                .thenReturn("application/json UTF-8")
                .thenReturn("application/json UTF-8")
                        //4th call
                .thenReturn(null)
                        //5th call
                .thenReturn(null);
        when(originalResponse.toString())
                        //1st call
                .thenReturn("")
                        //4th call
                .thenReturn("")
                        //5th call
                .thenReturn("HTTP/1.0\r\nContent-Type: application/json");

        assertFalse(jsonResponse.doesApply());
        assertTrue(jsonResponse.doesApply());
        assertTrue(jsonResponse.doesApply());
        assertFalse(jsonResponse.doesApply());
        assertTrue(jsonResponse.doesApply());
    }

    @Test
    public void testAppendDebugInfo() throws Exception {
        String jsonStr = jsonResponse.appendDebugInfo(JSON_RESPONSE);
        JSONObject json = new JSONObject(jsonStr);
        assertTrue(json.has(JsonDebuggingHttpServletResponse.DEFAULT_DEBUG_FIELD_NAME));

        String jsonArrayStr = jsonResponse.appendDebugInfo(HTTP_JSON_ARRAY_RESPONSE);
        JSONArray jsonArray = new JSONArray(jsonResponse.getResponseBody(jsonArrayStr));

        //original array has 3 elements, so 4th element is debug output
        assertEquals(4, jsonArray.length());
    }

    @Test
    public void testGetHttpHeaders() throws Exception {
        assertEquals(HTTP_HEADER, jsonResponse.getHttpHeaders(HTTP_JSON_RESPONSE));
    }

    @Test
    public void testGetResponseBody() throws Exception {
        assertEquals(JSON_ARRAY_RESPONSE, jsonResponse.getResponseBody(HTTP_JSON_ARRAY_RESPONSE));
    }

    @Test
    public void testIsValidJson() throws Exception {
        assertTrue(jsonResponse.isValidJson(JSON_RESPONSE));
        assertFalse(jsonResponse.isValidJson("not a json" + JSON_RESPONSE));
        assertFalse(jsonResponse.isValidJson(JSON_ARRAY_RESPONSE));
    }

    @Test
    public void testIsValidJsonArray() throws Exception {
        assertTrue(jsonResponse.isValidJsonArray(JSON_ARRAY_RESPONSE));
        assertFalse(jsonResponse.isValidJsonArray("not a json array" + JSON_ARRAY_RESPONSE));
        assertFalse(jsonResponse.isValidJsonArray(JSON_RESPONSE));
    }

    @Test
    public void testContainsHttpHeaders() throws Exception {
        assertTrue(jsonResponse.containsHttpHeaders(HTTP_JSON_ARRAY_RESPONSE));
        assertTrue(jsonResponse.containsHttpHeaders(HTTP_JSON_RESPONSE));
        assertFalse(jsonResponse.containsHttpHeaders(JSON_ARRAY_RESPONSE));
        assertFalse(jsonResponse.containsHttpHeaders("Not HTTP" + HTTP_JSON_RESPONSE));
    }

    public static final String JSON_RESPONSE = "{\"widget\": {\n" +
            "    \"window\": {\n" +
            "        \"title\": \"Sample Konfabulator Widget\",\n" +
            "        \"name\": \"main_window\",\n" +
            "        \"width\": 500,\n" +
            "        \"height\": 500\n" +
            "    },\n" +
            "    \"image\": { \n" +
            "        \"src\": \"Images/Sun.png\",\n" +
            "        \"name\": \"sun1\",\n" +
            "        \"hOffset\": 250,\n" +
            "        \"vOffset\": 250,\n" +
            "        \"alignment\": \"center\"\n" +
            "    },\n" +
            "    \"text\": {\n" +
            "        \"data\": \"Click Here\",\n" +
            "        \"size\": 36,\n" +
            "        \"style\": \"bold\",\n" +
            "        \"name\": \"text1\",\n" +
            "        \"hOffset\": 250,\n" +
            "        \"vOffset\": 100,\n" +
            "        \"alignment\": \"center\",\n" +
            "        \"onMouseUp\": \"sun1.opacity = (sun1.opacity / 100) * 90;\"\n" +
            "    }\n" +
            "}}";
    public static final String JSON_ARRAY_RESPONSE = "[\n" +
            "    {\"firstName\":\"John\", \"lastName\":\"Doe\"}, \n" +
            "    {\"firstName\":\"Anna\", \"lastName\":\"Smith\"}, \n" +
            "    {\"firstName\":\"Peter\", \"lastName\": \"Jones\"}\n" +
            "]";
    public static final String HTTP_HEADER = "HTTP/1.0 200 OK\n" +
            "Transfer-Encoding: UTF-8\n" +
            "Date: Sat, 28 Nov 2009 04:36:25 GMT\n" +
            "Server: LiteSpeed\n" +
            "Connection: close\n" +
            "X-Powered-By: W3 Total Cache/0.8\n" +
            "Pragma: public\n" +
            "Expires: Sat, 28 Nov 2009 05:36:25 GMT\n" +
            "Cache-Control: max-age=3600, public\n" +
            "Content-Type: text/html; charset=UTF-8\n" +
            "Last-Modified: Sat, 28 Nov 2009 03:50:37 GMT\n" +
            "Content-Encoding: application/json\n" +
            "Vary: Accept-Encoding, Cookie, User-Agent";
    public static final String CRLF = "\r\n";
    public static final String CRLFCRLF = CRLF + CRLF;

    public static final String HTTP_JSON_RESPONSE = HTTP_HEADER + CRLFCRLF + JSON_RESPONSE;
    public static final String HTTP_JSON_ARRAY_RESPONSE = HTTP_HEADER + CRLFCRLF + JSON_ARRAY_RESPONSE;
}
