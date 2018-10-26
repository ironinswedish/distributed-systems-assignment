package databank_server;

import java.sql.*;

public class database_server {
    static Connection conn;
    static Statement st;

    public static void start() {
        conn = null;
        try {
            //url (pad) naar .sqlite database
            String url = "jdbc:sqlite://C:/Users/jarne/downloads/memorydb.sqlite";
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            st = conn.createStatement();

        } catch (SQLException e1) {
            e1.printStackTrace();
        }

    }

    public static String getUsers(){
        String query = "SELECT * FROM users";
        try {
            ResultSet rs = st.executeQuery((query));
            return rs.getInt("userid")+"name: " + rs.getString("login") + "pasword: " + rs.getString("paswoord");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "query failed";

    }

    public static void end(){
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args){
        start();
        System.out.println(getUsers());
        end();
    }
}
