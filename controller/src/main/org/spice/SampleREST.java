package org.spice;

import java.util.Map;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import java.util.function.BiConsumer;

import org.spice.rest.RESTServlet;
import org.spice.rest.RESTException;

public class SampleREST extends RESTServlet {

    public void doHello(HttpServletRequest request, HttpServletResponse response) {
        JsonWriter writer = setupJson(response);

        String[] prefix = request.getPathInfo().split("/");

        Map<String,String[]> params = getParams(request);

        try {
            JsonObjectBuilder ret = Json.createObjectBuilder();
            ret.add("prefix", Arrays.toString(prefix));
            ret.add("test", "hello2");
            ret.add("clazz", getParams(request).getClass().toString());

            JsonArrayBuilder arr = Json.createArrayBuilder();
            for(String key: getParams(request).keySet()) arr.add(key);
            ret.add("parameter_names", arr);

            writer.writeObject(ret.build());
        } finally {
            writer.close();  // Always close the output writer
        }
    }

    public void doSum(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String a, b;
        try {
            a = readParam(req, "a")[0];
            b = readParam(req, "b")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        int A, B;
        try {
            A = Integer.parseInt(a);
            B = Integer.parseInt(b);
        } catch(NumberFormatException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonObjectBuilder resp = Json.createObjectBuilder();
            resp.add("result", A+B);
            writer.writeObject(resp.build());
        } finally {
            writer.close();
        }
    }

    public SampleREST() {
        registerMethod("hello", this::doHello);
        registerMethod("sum", this::doSum);
    }
}
