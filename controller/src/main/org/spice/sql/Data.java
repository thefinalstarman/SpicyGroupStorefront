package org.spice.sql;

import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.JsonObjectBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSetMetaData;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Data {
    private Connection con;
    private final String user = "testuser";
    private final String pass = "test623";
    private final String url = "jdbc:mysql://localhost:3306/storeback?serverTimezone=UTC";

    public static final String WILDCARD = "*";
    public static final String EQ = "=";
    public static final String LESS = "<";
    public static final String GREATER = ">";
    public static final String REGEX = "regexp";
    public static final String CURRENT_TIMESTAMP = "current_timestamp";

    public Data() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);
        } catch(SQLException | ClassNotFoundException ex) {
            Logger lgr = Logger.getLogger(this.getClass().getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            con = null;
        }
    }

    public static String wrap(Object o) {
        return "\"" + o.toString() + "\"";
    }

    public class Insert {
        private final String table;
        private HashMap<String,Object> values;

        protected Insert(String table) {
            this.table = table;
            this.values = new HashMap<String,Object>();
        }

        public Insert add(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public PreparedStatement build() throws SQLException {
            StringBuilder sb = new StringBuilder("insert into ");
            String[] keys = values.keySet().toArray(new String[0]);
            sb.append(table);
            sb.append("("); {
                int idx = 0;
                for(String key: keys) {
                    if(idx++ > 0) sb.append(",");
                    sb.append(key);
                }
            } sb.append(") values ("); {
                for(int idx = 0; idx < keys.length; idx++) {
                    if(idx > 0) sb.append(",");
                    sb.append("?");
                }
            } sb.append(")");

            PreparedStatement ret = con.prepareStatement(sb.toString(), PreparedStatement.RETURN_GENERATED_KEYS);

            for(int idx = 0; idx < keys.length; idx++) {
                ret.setObject(idx+1, values.get(keys[idx]));
            }

            return ret;
        }

        public int execute() throws SQLException {
            PreparedStatement st = build();
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if(rs.next()) return rs.getInt(1);
            return -1;
        }
    };

    public Insert insert(String tableName) { return new Insert(tableName); }

    public class Select {
        private ArrayList<String> tables;
        private ArrayList<String> values;
        private ArrayList<String> clauses;

        protected Select() {
            tables = new ArrayList<String>();
            values = new ArrayList<String>();
            clauses = new ArrayList<String>();
        }

        public Select addTable(String name) {
            tables.add(name);
            return this;
        }

        public Select addTable(String name, String abbrev) {
            return addTable(name + " " + abbrev);
        }

        public Select addValue(String name) {
            values.add(name);
            return this;
        }

        public Select addClause(String clause) {
            clauses.add(clause);
            return this;
        }

        public Select addClause(String left, String type, String right) {
            return addClause(left + " " + type + " " + right);
        }

        public PreparedStatement build() throws SQLException {
            StringBuilder sb = new StringBuilder("select ");
            {
                int idx = 0;
                for(String value: values) {
                    if(idx++ > 0) sb.append(",");
                    sb.append(value);
                }
            } sb.append(" from "); {
                int idx = 0;
                for(String table: tables) {
                    if(idx++ > 0) sb.append(",");
                    sb.append(table);
                }
            }
            {
                int idx = 0;
                for(String clause: clauses) {
                    if(idx++ > 0) sb.append(" and ");
                    else sb.append(" where ");
                    sb.append(clause);
                }
            }

            return con.prepareStatement(sb.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
        }

        public List<JsonObject> execute() throws SQLException {
            PreparedStatement st = build();
            ResultSet rs = st.executeQuery();

            ArrayList<JsonObject> ret = new ArrayList<JsonObject>();
            while(rs.next()) {
                ret.add(jsonFromResults(rs));
            }
            return ret;
        }
    }

    public Select select() { return new Select(); }

    public class Delete {
        private final String table;
        private ArrayList<String> clauses;

        protected Delete(String table) {
            this.table = table;
            clauses = new ArrayList<String>();
        }

        public Delete addClause(String clause) {
            clauses.add(clause);
            return this;
        }

        public Delete addClause(String left, String type, String right) {
            return addClause(left + " " + type + " " + right);
        }

        public PreparedStatement build() throws SQLException {
            StringBuilder sb = new StringBuilder("delete from ");
            sb.append(table);
            {
                int idx = 0;
                for(String clause: clauses) {
                    if(idx++ > 0) sb.append(" and ");
                    else sb.append(" where ");
                    sb.append(clause);
                }
            }

            return con.prepareStatement(sb.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
        }

        public int execute() throws SQLException {
            PreparedStatement st = build();
            return st.executeUpdate();
        }
    }

    public Delete delete(String table) { return new Delete(table); }

    public static JsonObject jsonFromResults(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();

        JsonObjectBuilder obj = Json.createObjectBuilder();
        for(int i = 1; i <= numColumns; i++) {
            String column_name = rsmd.getColumnName(i);

            rs.getObject(column_name);
            if(rs.wasNull())
                continue;

            if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
                obj.add(column_name, rs.getArray(column_name).toString());
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
                obj.add(column_name, rs.getInt(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                obj.add(column_name, rs.getBoolean(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                obj.add(column_name, rs.getBlob(column_name).toString());
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                obj.add(column_name, rs.getDouble(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                obj.add(column_name, rs.getFloat(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                obj.add(column_name, rs.getInt(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                obj.add(column_name, rs.getNString(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                obj.add(column_name, rs.getString(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                obj.add(column_name, rs.getInt(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                obj.add(column_name, rs.getInt(column_name));
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                obj.add(column_name, rs.getDate(column_name).toString());
            }
            else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                obj.add(column_name, rs.getTimestamp(column_name).toString());
            }
            else{
                obj.add(column_name, rs.getObject(column_name).toString());
            }
        }

        return obj.build();
    }
}
