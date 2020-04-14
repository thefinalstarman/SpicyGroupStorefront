package org.spice.sms;

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

public class SMS extends RESTServlet {

    public void doAddProduct(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String name, category, _price;
        try {
            name = readParam(req, "name")[0];
            category = readParam(req, "category")[0];
            _price = readParam(req, "price")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        int price;
        try {
            price = Integer.parseInt(_price);
        } catch(NumberFormatException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // TODO: actually insert into the Products table,
        // grab the resulting pk and the fk for category

        JsonWriter writer = setupJson(response);

        try {
            JsonObjectBuilder resp = Json.createObjectBuilder();
            resp.add("id", 1);
            resp.add("name", name);
            resp.add("category", 1);
            resp.add("price", price);
            writer.writeObject(resp.build());
        } finally {
            writer.close();
        }
    }

    public void doDeleteProduct(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String _id;
        try {
            _id = readParam(req, "id")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        int id;
        try {
            id = Integer.parseInt(_id);
        } catch(NumberFormatException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // TODO: find the product first,
        // return the object and delete it, error if it doesn't exist

        JsonWriter writer = setupJson(response);

        try {
            JsonObjectBuilder resp = Json.createObjectBuilder();
            resp.add("id", id);
            resp.add("name", "test_name");
            resp.add("category", 1);
            resp.add("price", 1);
            writer.writeObject(resp.build());
        } finally {
            writer.close();
        }
    }

    public void doCustomerSearch(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String[] name, category, product;
        try {
            name = readParam(req, "name", false);
            category = readParam(req, "category", false);
            product = readParam(req, "category", false);
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // TODO: find the list of matching people

        JsonWriter writer = setupJson(response);

        try {
            JsonArrayBuilder resp = Json.createArrayBuilder();

            for(int i = 0; i < 2; i++) {
                JsonObjectBuilder person = Json.createObjectBuilder();
                person.add("id", i);

                if(!isEmpty(name)) {
                    person.add("name", name[i % name.length]);
                } else {
                    person.add("name", "test_name_" + i);
                }
                resp.add(person);
            }

            writer.writeArray(resp.build());
        } finally {
            writer.close();
        }
    }

    public SMS() {
        registerMethod("addProduct", this::doAddProduct);
        registerMethod("deleteProduct", this::doDeleteProduct);
        registerMethod("customerSearch", this::doCustomerSearch);
    }
}
