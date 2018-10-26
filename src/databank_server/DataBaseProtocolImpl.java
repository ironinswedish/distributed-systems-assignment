package databank_server;

import Interfaces.DataBaseProtocol;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

public class DataBaseProtocolImpl extends UnicastRemoteObject implements DataBaseProtocol {

    static Connection conn;
    static Statement st;


    public DataBaseProtocolImpl() throws RemoteException {setupConnection();}

    public void setupConnection(){
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

    @Override
    public String login(String username, String password) throws RemoteException {
        System.out.println(username + " password: "+password);
        String query = "SELECT * FROM users WHERE login ='"+username+"'" ;
        try {
            System.out.println(st);
            ResultSet rs = st.executeQuery((query));
            if (rs.isBeforeFirst()) {
                if (rs.getString("paswoord").equals(password)) {
                    return "ok";
                } else {
                    return "incorrect";
                }
            } else return "username does not exist";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "query failed";
    }

}
