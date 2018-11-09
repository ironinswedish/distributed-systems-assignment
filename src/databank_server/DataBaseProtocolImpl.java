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
        System.out.println(username + " password kilk: "+password);
        String query = "SELECT paswoord,token FROM users WHERE login ='"+username+"'" ;
        try {
            System.out.println(st);
            ResultSet rs = st.executeQuery((query));
            if (rs.isBeforeFirst()) {
                if (rs.getString("paswoord").equals(password)) {
                    if (rs.getString("token") == null) {
                        generateKey(username);
                    }
                    registerUser("bert", "pikmin");
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

    public void generateKey(String username) throws SQLException{
        String key = "12345";
        String upd = "UPDATE users SET token = ? WHERE login = ? ";

            PreparedStatement prst = conn.prepareStatement(upd);
            prst.setString(1, key);
            prst.setString(2, username);
            prst.executeUpdate();
    }

    //register een user nog niet geimplementeerd in client en applicatiezijde
    @Override
    public String registerUser(String username,String hashedPassword){
        String ins = "INSERT INTO users(login,paswoord) VALUES(?,?)";
        if (checkUser(username)) {
            try {
                PreparedStatement prst = conn.prepareStatement(ins);
                prst.setString(1, username);
                prst.setString(2, hashedPassword);

                prst.executeUpdate();
                generateKey(username);
                return "ok";
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return "failed";

            }
        }else{
            return "user exists";
        }
    }

    //returns true indien user niet bestaat
    public boolean checkUser(String username) {
        String query = "SELECT login FROM users WHERE login ='" + username + "'";
        try{
            ResultSet rs = st.executeQuery((query));
            return !rs.isBeforeFirst();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
