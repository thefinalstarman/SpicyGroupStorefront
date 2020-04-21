package.org.spice.storefront

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
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import java.util.function.BiConsumer;

import org.spice.rest.RESTServlet;
import org.spice.rest.RESTException;
import org.spice.sql.Data;

public class Storefront extends RESTServlet {
    Data myDataSt = new Data();

    public void doSubmitOrder(HttpServletRequest req, HttpServletResponse response) {
        Map<String,String[]> params = getParams(req);
        
        // Will item parameters be here as well?
        String cname, caddress;
        String ccard;
        try {
            cname = readParam(req, "cname")[0];
            caddress = readParam(req, "caddress")[0];
            ccard = readParam(req, "ccard")[0];
        } catch(RESTException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        //TODO: Generate Unique ID Number!!

        Data.Person dataInsert = null;
        try{
            dataInsert = myDataSt.createPerson(cname,cid,caddress,ccard);
        } catch(SQLException e) {
            trySendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        JsonWriter writer = setupJson(response);

        try {
            writer.writeObject(dataInsert.toJson());
        } finally {
            writer.close();
        }

        //TODO: Export everything onto Orders table after! 
    }

    public void generateDiscountCode(HttpServletRequest req, HttpServletResponse response) {

        int discId = 


    }

}