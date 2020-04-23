package org.spice.common;

import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import java.util.function.BiConsumer;

import org.spice.rest.RESTServlet;
import org.spice.rest.RESTException;
import org.spice.sql.*;

public class Common extends RESTServlet {
    Data myData = new Data();

    public void doListProducts(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);

        String[] name;
        try {
            name = readParam(req, "name", false);
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        List<JsonObject> products = null;
        try {
            Data.Select select = myData.select()
                .addTable(Products.TABLE)
                .addValue(Data.WILDCARD);

            if(!isEmpty(name)) {
                select.addClause(Products.NAME, Data.REGEX, Data.wrap(name[0]));
            }

            products = select.execute();
        } catch (SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            JsonArrayBuilder resp = Json.createArrayBuilder();
            for(JsonObject p: products)
                resp.add(p);
            writer.writeArray(resp.build());
        } finally {
            writer.close();
        }
    }

    public Common() {
        registerMethod("listProducts", this::doListProducts);
    }
}
