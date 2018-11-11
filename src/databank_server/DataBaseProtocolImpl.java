package databank_server;

import Interfaces.DataBaseProtocol;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class DataBaseProtocolImpl extends UnicastRemoteObject implements DataBaseProtocol {

    static Connection conn;
    static Statement st;


    public DataBaseProtocolImpl() throws RemoteException {
        setupConnection();

        //codevoorbeeld voor periodieke taak uit te voeren zoals updaten database

        String query = "SELECT userid, token, sessiontime FROM users";
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    ResultSet rs;
                    rs = st.executeQuery((query));
                    while (rs.next()) {
                        if (rs.getString("token") != null) {
                            if (rs.getInt("sessiontime") + 1 == 24) {
                                resetToken(rs.getInt("userid"));
                                setOffline(rs.getInt("userid"));
                            } else {
                                incrementSession(rs.getInt("userid"), rs.getInt("sessiontime"));
                            }
                        }

                        System.out.println(rs.getInt("userid") + "token: " + rs.getString("token") + "sessiontime:  " + rs.getString("sessiontime"));

                    }


                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
        //500 -> 0.5s
        //3600000 -> 1 uur

    }

    private void resetToken(int userid) throws SQLException {
        String upd = "UPDATE users SET token = ?,sessiontime = ? WHERE userid = ? ";

        PreparedStatement prst = conn.prepareStatement(upd);
        prst.setString(1, null);
        prst.setInt(2, -1);
        prst.setInt(3, userid);
        prst.executeUpdate();

    }

    private void setOffline(int userid) throws SQLException {
        String upd = "UPDATE users SET loggedin = ? WHERE userid = ? ";
        PreparedStatement prst = conn.prepareStatement(upd);
        prst.setBoolean(1, false);

        prst.setInt(2, userid);
        prst.executeUpdate();

    }
    private void incrementSession(int userid, int time) throws SQLException {
        String upd = "UPDATE users SET sessiontime = ? WHERE userid = ? ";

        PreparedStatement prst = conn.prepareStatement(upd);
        prst.setInt(1, time + 1);
        prst.setInt(2, userid);
        prst.executeUpdate();
    }

    private void setupConnection() {
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

    //wordt (nog) niet gebruikt
    private static String getUsers() {
        String query = "SELECT * FROM users";
        try {
            ResultSet rs = st.executeQuery((query));
            return rs.getInt("userid") + "name: " + rs.getString("login") + "pasword: " + rs.getString("paswoord");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "query failed";

    }

    //wordt (nog) niet gebruikt -> veilig afsluiten databaseserver
    private static void end() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public String[] login(String username, String password, String session) throws RemoteException {
        String[] result = new String[2];
        result[0] = "";
        result[1] = "";

        System.out.println(username + " password: " + password);
        String query = "SELECT paswoord,token,loggedin FROM users WHERE login ='" + username + "'";
        try {

            ResultSet rs = st.executeQuery((query));
            if (rs.isBeforeFirst()) {
                if (!rs.getBoolean("loggedin")) {
                    if (rs.getString("token") != null && rs.getString("token").equals(session)) {
                        System.out.println("heyo");
                        result[0] = "ok";

                        result[1] = session;
                        setUserOnline(session);
                        return result;
                    } else {
                        if (rs.getString("paswoord").equals(password)) {
                            if (rs.getString("token") == null) {
                                result[1] = generateKey(username);
                                session = result[1];
                            }
                            result[0] = "ok";

                            return result;
                        } else {
                            result[0] = "incorrect";
                            return result;
                        }
                    }


                } else {
                    result[0] = "user allready online";
                    return result;
                }
            } else {
                result[0] = "username does not exist";
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result[0] = "query failed";
        return result;
    }

    private String generateKey(String username) throws SQLException {
        String key = "12345";
        String upd = "UPDATE users SET token = ?,sessiontime = ? WHERE login = ? ";

        PreparedStatement prst = conn.prepareStatement(upd);
        prst.setString(1, key);
        prst.setInt(2, 0);
        prst.setString(3, username);
        prst.executeUpdate();

        setUserOnline(key);
        return key;
    }

    private void setUserOnline(String session) throws SQLException {
        String upd = "UPDATE users SET loggedin = ? WHERE token = ? ";

        PreparedStatement prst = conn.prepareStatement(upd);
        prst.setBoolean(1, true);
        prst.setString(2,session);
        prst.executeUpdate();
    }

    @Override
    public String[] registerUser(String username, String hashedPassword) {
        String[] result = new String[2];
        result[0] = "";
        result[1] = "";

        String ins = "INSERT INTO users(login,paswoord,loggedin,) VALUES(?,?,?)";
        if (checkUser(username)) {
            try {
                PreparedStatement prst = conn.prepareStatement(ins);
                prst.setString(1, username);
                prst.setString(2, hashedPassword);
                prst.setBoolean(3, true);


                prst.executeUpdate();
                result[1] = generateKey(username);
                result[0] = "ok";
                return result;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                result[0] = "failed";
                return result;

            }
        } else {
            result[0] = "user exists";
            return result;
        }
    }

    @Override
    public void logout(String session, boolean xButton) throws RemoteException {
        System.out.println("user logged out");
        System.out.println(xButton);
        System.out.println(session);
        String query = "SELECT userid FROM users WHERE token ='" + session + "'";

        try {

            if (xButton) {
                ResultSet rs = st.executeQuery(query);
                setOffline(rs.getInt("userid"));
            } else {
                ResultSet rs = st.executeQuery(query);
                resetToken(rs.getInt("userid"));
                setOffline(rs.getInt("userid"));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //returns true indien user niet bestaat
    private boolean checkUser(String username) {
        String query = "SELECT login FROM users WHERE login ='" + username + "'";
        try {
            ResultSet rs = st.executeQuery((query));
            return !rs.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
