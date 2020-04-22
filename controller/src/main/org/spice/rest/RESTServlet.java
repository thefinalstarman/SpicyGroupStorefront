package org.spice.rest;

import java.util.Map;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import java.util.function.BiConsumer;

public class RESTServlet extends HttpServlet {
    private Map<String,BiConsumer<HttpServletRequest, HttpServletResponse> > methods;

    protected JsonWriter setupJson(HttpServletResponse response) {
        try {
            response.setContentType("application/json;charset=UTF8");
            return Json.createWriter(response.getWriter());
        } catch(IOException e) {
            return null;
        }
    }

    protected Map<String,String[]> getParams(HttpServletRequest request) {
        return request.getParameterMap();
    }

    protected void registerMethod(String prefix,
                                  BiConsumer<HttpServletRequest,HttpServletResponse> method) {
        methods.put(prefix, method);
    }

    protected void listMethods(HttpServletRequest request,
                               HttpServletResponse response) {
        JsonWriter writer = setupJson(response);

        try {
            JsonObjectBuilder ret = Json.createObjectBuilder();
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for(String method: methods.keySet())
                arr.add(method);
            ret.add("methods", arr);

            writer.writeObject(ret.build());
        } finally {
            writer.close();
        }
    }

    protected void trySendError(HttpServletResponse response,
                                int code) {
        try {
            response.sendError(code);
        } catch(IOException e) {}
    }

    protected void trySendError(HttpServletResponse response, int code, String msg) {
        try {
            response.sendError(code, msg);
        } catch(IOException e) {}
    }

    static public <T> boolean isEmpty(T[] s) {
        return s == null || s.length == 0;
    }

    protected String[] readParam(HttpServletRequest request,
                                 String name,
                                 boolean required) throws RESTException {
        String[] ret = getParams(request).get(name);
        if(required && isEmpty(ret))
            throw new RESTException("Missing required parameter, " + name);
        return ret;
    }

    protected String[] readParam(HttpServletRequest request,
                                 String name) throws RESTException {
        return readParam(request, name, true);
    }

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {
        if(request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            listMethods(request, response);
            return;
        }

        String[] prefix = request.getPathInfo().split("/");
        if(prefix.length != 2) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BiConsumer<HttpServletRequest, HttpServletResponse> method = methods.get(prefix[1]);

        if(method == null) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        method.accept(request, response);
    }

    public RESTServlet() {
        methods = new HashMap<>();
    }
}
