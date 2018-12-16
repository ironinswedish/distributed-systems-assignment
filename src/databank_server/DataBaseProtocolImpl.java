package databank_server;

import Interfaces.DataBaseProtocol;
import Interfaces.DispatchProtocol;
import io.jsonwebtoken.*;
import shared_objects.Game;
import shared_objects.Image;
import shared_objects.Theme;
import shared_objects.User;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

public class DataBaseProtocolImpl extends UnicastRemoteObject implements DataBaseProtocol {

    static Connection conn;
    static Statement st;
    static String SERVERNAME = "database1";
    static ArrayList<Integer> databaseList;
    static HashMap<Integer, DataBaseProtocol> databaseRegistryMap = new HashMap<>();
    static ArrayList<String> primaryList = new ArrayList<>();
    static int backupserver = -1;
    static int servertobackup = -1;
    static int ownPort;
    static Registry databaseRegistry;

    static ArrayList<String> backupGames = new ArrayList<>();
    static ArrayList<Integer> availableThemas = new ArrayList<>();


    public DataBaseProtocolImpl(ArrayList<Integer> dblist) throws RemoteException {
        databaseList = dblist;
        SERVERNAME = "database" + dblist.size();
        ownPort = dblist.get(dblist.size() - 1);
        setupConnection();
        if (dblist.size() > 1) {
            System.out.println("entered");
            getDataToBackup();
            notifyPreviousDB();
        }


        //codevoorbeeld voor periodieke taak uit te voeren zoals updaten database
        /*
        String query = "SELECT userid, token, sessiontime FROM users";
        Timer timer = new Timer();


        timer.schedule(new TimerTask() {
            public void run() {

            }
        }, 0, 1000);*/
        //500 -> 0.5s
        //3600000 -> 1 uur

    }


    private void setupConnection() {
        conn = null;
        try {
            //url (pad) naar .sqlite database
            String url;
            if (ownPort > 14000) {
                System.out.println(ownPort);
                url = "jdbc:sqlite:memorydb2.sqlite";
            } else {
                url = "jdbc:sqlite:memorydb.sqlite";
            }


            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            st = conn.createStatement();

        } catch (SQLException e1) {
            e1.printStackTrace();
        }

    }
    //database logica**************************************************************************************

    private void getDataToBackup() {
        backupserver = databaseList.get(0);
        try {
            databaseRegistry = LocateRegistry.getRegistry("localhost", backupserver);
            databaseRegistryMap.put(backupserver, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
            databaseRegistryMap.get(backupserver).getBackupData(ownPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    private void notifyPreviousDB() {
        servertobackup = databaseList.get(databaseList.size() - 1);
        try {
            databaseRegistry = LocateRegistry.getRegistry("localhost", servertobackup);
            databaseRegistryMap.put(servertobackup, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
            databaseRegistryMap.get(servertobackup).setBackupServer(ownPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setBackupServer(int port) {
        backupserver = port;
        try {
            databaseRegistry = LocateRegistry.getRegistry("localhost", port);
            databaseRegistryMap.put(port, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void getBackupData(int port) {
        servertobackup = port;
        try {
            databaseRegistry = LocateRegistry.getRegistry("localhost", servertobackup);
            Game game;
            for (String gameId : backupGames) {
                game = getGameById(gameId);
                databaseRegistryMap.get(port).placeBackupGame(game);
            }
            backupGames = new ArrayList<>();
            databaseRegistryMap.get(port).setPreviewThemes(getPreviewThemes());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private Game getGameById(String id) {
        String query = "SELECT aantalspelers, themaid, zetnr, kaartlayout, volgorde, scores, applicationserver, status, currentplayer FROM spellen WHERE global_spelid ='" + id + "'";
        try {
            ResultSet rs = st.executeQuery(query);
            Game game = new Game(id, rs.getInt("aantalspelers"), rs.getInt("currentplayer"), rs.getString("applicationserver"));
            game.setTheme(rs.getInt("themaid"));
            game.setTurnCount(rs.getInt("zetnr"));
            game.setPlayOrderFromString(rs.getString("volgorde"));
            game.setScoresFromString(rs.getString("scores"));
            game.readCardMatrixFromString(rs.getString("kaartlayout"));
            game.setApplicatieServer(rs.getString("applicationserver"));
            game.setApplicatieServer(rs.getString("status"));

            return game;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void placeBackupGame(Game game) {
        backupGames.add(game.getGameId());
        createBackupGame(game);
    }

    private void getUserFromPrimary(String login) {
        User user = null;
        DataBaseProtocol dataprotocol;
        try {
            if (!primaryList.contains(login)) {
                for (Integer port : databaseList) {
                    if (port != ownPort) {
                        dataprotocol = databaseRegistryMap.get(port);
                        if (dataprotocol == null) {
                            databaseRegistry = LocateRegistry.getRegistry("localhost", port);
                            databaseRegistryMap.put(port, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
                        }
                        user = databaseRegistryMap.get(port).getUserPrimary(login);

                        if (user != null) {
                            updateUser(user);
                            primaryList.add(login);
                            break;
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User getUserPrimary(String login) {
        User user = null;
        if (primaryList.contains(login)) {
            user = getUser(login);
            primaryList.remove(login);
        }
        return user;
    }

    @Override
    public void updateUser(User user) {
        String upd = "UPDATE users SET login=?, paswoord = ?, token =?, sessiontime = ?, loggedin = ?, aantalwins =?, aantalgelijk =?, aantalverloren = ?, salt =?";
        PreparedStatement prst = null;
        try {
            prst = conn.prepareStatement(upd);
            prst.setString(1, user.getLogin());
            prst.setString(2, user.getPaswoord());
            prst.setString(3, user.getToken());
            prst.setInt(4, user.getSessiontime());
            prst.setBoolean(5, user.isLoggedin());
            prst.setInt(6, user.getAantalwins());
            prst.setInt(7, user.getAantalgelijk());
            prst.setInt(8, user.getAantalverloren());
            prst.setBytes(9, user.getSalt());
            prst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Theme getThema(int themaId) {
        try {
            if (availableThemas.contains(themaId)) {
                return getTheme(themaId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkTheme(int themaId) {
        DataBaseProtocol dataprot;
        Theme theme;
        if (!availableThemas.contains(themaId)) {
            for (Integer port : databaseList) {
                if (port != ownPort) {
                    try {
                        dataprot = databaseRegistryMap.get(port);
                        if (dataprot == null) {
                            databaseRegistry = LocateRegistry.getRegistry("localhost", port);
                            databaseRegistryMap.put(port, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
                        }
                        theme = databaseRegistryMap.get(port).getThema(themaId);
                        if (theme != null) {
                            updatePics(theme);
                            //setTheme(theme);
                            break;
                        }
                    } catch (AccessException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //User logica********************************************************************************************************//
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

    private User getUser(String login) {
        String query = "SELECT * FROM users WHERE login = '" + login + "'";

        try {
            ResultSet rs = st.executeQuery((query));
            User user = new User(login, rs.getString("paswoord"), rs.getString("token"), rs.getInt("sessiontime"), rs.getBoolean("loggedin"), rs.getInt("aantalwins"), rs.getInt("aantalgelijk"), rs.getInt("aantalverloren"), rs.getBytes("salt"));
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String[] login(String username, String password, String session) throws RemoteException {
        String[] result = new String[2];
        result[0] = "";
        result[1] = "";
        getUserFromPrimary(username);
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
                        new Thread(new BackupChangeThread(username)).start();
                        return result;
                    } else {
                        if (rs.getString("paswoord").equals(password)) {
                            if (rs.getString("token") == null) {
                                result[1] = generateKey(username);
                            }
                            result[0] = "ok";
                            new Thread(new BackupChangeThread(username)).start();

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
        String key = createJWT(username);
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
        prst.setString(2, session);
        prst.executeUpdate();
    }


    @Override
    public String[] registerUser(String username, String hashedPassword, byte[] salt) {
        String[] result = new String[2];
        result[0] = "";
        result[1] = "";

        getUserFromPrimary(username);
        String ins = "INSERT INTO users(login,paswoord,token,loggedin,salt) VALUES(?,?,?,?,?)";
        if (checkUser(username)) {
            try {

                String jwt = createJWT(username);

                PreparedStatement prst = conn.prepareStatement(ins);
                prst.setString(1, username);
                prst.setString(2, hashedPassword);
                prst.setString(3, jwt);
                prst.setBoolean(4, true);
                prst.setBytes(5, salt);


                prst.executeUpdate();
                result[1] = generateKey(username);
                result[0] = "ok";
                primaryList.add(username);
                new Thread(new BackupChangeThread(username)).start();
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
    public byte[] getSalt(String login) throws RemoteException {
        getUserFromPrimary(login);
        String query = "SELECT salt FROM users WHERE login ='" + login + "'";
        byte[] salt;
        try {
            ResultSet rs = st.executeQuery(query);
            if (rs.isBeforeFirst()) {

                salt = rs.getBytes("salt");
                return salt;
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void logout(String login, String session, boolean xButton) throws RemoteException {
        System.out.println("user logged out");
        System.out.println(xButton);
        System.out.println(session);

        getUserFromPrimary(login);
        String query = "SELECT userid,token FROM users WHERE login ='" + login + "'";


        try {
            ResultSet rs = st.executeQuery(query);
            if (rs.getString("token") != null && rs.getString("token").equals(session)) {

                setOffline(rs.getInt("userid"));
                resetToken(rs.getInt("userid"));


                new Thread(new BackupChangeThread(login)).start();
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

    @Override
    public Theme getTheme(String themeName) throws RemoteException {

        System.out.println("START TO GET THEME: " + themeName);
        try {

            String query = "SELECT themaid FROM thema WHERE beschrijving ='" + themeName + "'";
            ResultSet rs = st.executeQuery(query);

            int themeId = rs.getInt("themaid");
            Theme theme = getTheme(themeId);
            return theme;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Theme getTheme(int themeId) throws RemoteException {
        checkTheme(themeId);
        try {

            String query = "SELECT aantalpics,beschrijving FROM thema WHERE themaid ='" + themeId + "'";
            ResultSet rs = st.executeQuery(query);

            String beschrijving = rs.getString("beschrijving");
            int size = rs.getInt("aantalpics");
            System.out.println(themeId + " is themeid met size: " + size);


            query = "SELECT picnumber,number FROM picture WHERE themaid ='" + themeId + "'";
            rs = st.executeQuery(query);

            int picId;
            byte[] picData;
            HashMap<String, byte[]> cards = new HashMap<>();

            while (rs.next()) {
                //picData=rs.getString("number");
                picData = rs.getBytes("number");

                picId = rs.getInt("picnumber");

                cards.put(String.valueOf(picId), picData);

            }
            Theme t = new Theme(themeId, beschrijving, size, cards);
            return t;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int changeUsername(String usernameField, String login, String session) throws RemoteException {
        getUserFromPrimary(usernameField);
        if (!checkUser(usernameField)) {
            return -1;
        } else {
            try {
            /*String query = "SELECT token FROM users WHERE login ='" + login + "'";
            ResultSet rs = st.executeQuery(query);
            String token =rs.getString("token");*/
/*
                try {
                    Jws<Claims> jws = Jwts.parser().setSigningKey("pokemon1").parseClaimsJws(session);
                    System.out.println("BODY: " + jws.getBody());
                    if (!jws.getBody().get("sub").equals(login)) {
                        return -2;
                    }
                } catch (Exception e) {
                    return -2;
                }*/

                String upd = "UPDATE users SET login = ? WHERE login = ? ";
                System.out.println(login + " AND " + usernameField);
                PreparedStatement prst = null;

                prst = conn.prepareStatement(upd);
                prst.setString(1, usernameField);
                prst.setString(2, login);
                prst.executeUpdate();
                new Thread(new BackupChangeThread(usernameField)).start();

                return 1;


            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }

        }
    }

    @Override
    public int changePassword(String newPassword, String login, String session) throws RemoteException {
        getUserFromPrimary(login);
        /*
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey("pokemon1").parseClaimsJws(session);
            System.out.println("BODY: " + jws.getBody());
            if (!jws.getBody().get("sub").equals(login)) {
                return -2;
            }
        } catch (Exception e) {
            return -2;
        }*/


        String upd = "UPDATE users SET paswoord = ? WHERE login = ? ";
        System.out.println(login + " AND " + newPassword);
        PreparedStatement prst = null;
        try {
            prst = conn.prepareStatement(upd);
            prst.setString(1, newPassword);
            prst.setString(2, login);
            prst.executeUpdate();
            new Thread(new BackupChangeThread(login)).start();

            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;

    }

    public double[] getUserStats(String login, String session) throws RemoteException {
        double[] stats = {0, 0, 0};
        /*
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey("pokemon1").parseClaimsJws(session);
            System.out.println("BODY: " + jws.getBody());
            if (!jws.getBody().get("sub").equals(login)) {
                stats[0] = -2;
                return stats;
            }
        } catch (Exception e) {
            stats[0] = -2;
            return stats;
        }*/


        String query = "SELECT aantalwins,aantalgelijk,aantalverloren FROM users WHERE login ='" + login + "'";

        try {
            ResultSet rs = st.executeQuery(query);
            stats[0] = rs.getInt("aantalwins");
            stats[1] = rs.getInt("aantalgelijk");
            stats[2] = rs.getInt("aantalverloren");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(stats[0] + " " + stats[1] + " " + stats[2]);
        return stats;
    }

    @Override
    public HashMap<String, Integer> getAllRankings() throws RemoteException {
        HashMap<String, Integer> ranking = getRanking();
        DataBaseProtocol dataprot;
        for (Integer port : databaseList) {
            if (port != ownPort) {
                try {
                    dataprot = databaseRegistryMap.get(port);
                    if (dataprot == null) {
                        databaseRegistry = LocateRegistry.getRegistry("localhost", port);
                        databaseRegistryMap.put(port, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
                    }
                    ranking.putAll(databaseRegistryMap.get(port).getRanking());
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return ranking;
    }

    @Override
    public HashMap<String, Integer> getRanking() {
        String query = "SELECT aantalwins,aantalverloren,login FROM users";
        HashMap<String, Integer> ranking = new HashMap<>();
        int score;
        int verloren;
        String username;

        try {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                if (!primaryList.contains(rs.getString("login"))) {
                    score = rs.getInt("aantalwins");
                    verloren = rs.getInt("aantalverloren");
                    int totalsore = score - verloren;
                    username = rs.getString("login");
                    ranking.put(username, totalsore);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        SortedSet<String> keys = new TreeSet<String>(ranking.keySet());
        System.out.println("RANKING:");
        for (String key : keys) {
            System.out.println("rank: " + key + " " + ranking.get(key));
        }
        return ranking;
    }

    public boolean checkToken(String login, String session) throws RemoteException {
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey("pokemon1").parseClaimsJws(session);
            System.out.println("BODY: " + jws.getBody());
            if (!jws.getBody().get("sub").equals(login)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    //Game Logica ****************************************************************************************//
    @Override
    public Game createGame(Game game, String appserver, String login, String session) throws RemoteException {


        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey("pokemon1").parseClaimsJws(session);
            System.out.println("BODY: " + jws.getBody());
            if (!jws.getBody().get("sub").equals(login)) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        int aantalspelers = game.getPlayerCount();
        int aantalspectaters = game.getSpectaterCount();
        int themaid = game.getTheme();
        int zetnr = game.getTurnCount();
        String kaartlayout = game.cardMatrixToString();
        String volgorde = game.playOrderToString();
        String scores = game.scoresToString();
        String applicationserver = appserver;
        String status = game.getStatus();


        String ins = "INSERT INTO spellen(aantalspelers,aantalspectaters,themaid,zetnr,kaartlayout,volgorde,scores,applicationserver,status,currentplayer) VALUES(?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement prst;


        try {
            prst = conn.prepareStatement(ins);
            prst.setInt(1, aantalspelers);
            prst.setInt(2, aantalspectaters);
            prst.setInt(3, themaid);
            prst.setInt(4, zetnr);
            prst.setString(5, kaartlayout);
            prst.setString(6, volgorde);
            prst.setString(7, scores);
            prst.setString(8, applicationserver);
            prst.setString(9, status);
            prst.setInt(10, game.getCurrentplayer());

            prst.executeUpdate();

            game = setId(game, appserver);
            new Thread(new BackupChangeThread(game)).start();
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return game;
    }


    private void createBackupGame(Game game) {

        int aantalspelers = game.getPlayerCount();
        int aantalspectaters = game.getSpectaterCount();
        int themaid = game.getTheme();
        int zetnr = game.getTurnCount();
        String kaartlayout = game.cardMatrixToString();
        String volgorde = game.playOrderToString();
        String scores = game.scoresToString();
        String applicationserver = game.getApplicatieServer();
        String status = game.getStatus();


        String ins = "INSERT INTO spellen(aantalspelers,aantalspectaters,themaid,zetnr,kaartlayout,volgorde,scores,applicationserver,status,currentplayer,global_spelid) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement prst;


        try {
            prst = conn.prepareStatement(ins);
            prst.setInt(1, aantalspelers);
            prst.setInt(2, aantalspectaters);
            prst.setInt(3, themaid);
            prst.setInt(4, zetnr);
            prst.setString(5, kaartlayout);
            prst.setString(6, volgorde);
            prst.setString(7, scores);
            prst.setString(8, applicationserver);
            prst.setString(9, status);
            prst.setInt(10, game.getCurrentplayer());
            prst.setString(11, game.getGameId());

            prst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();

        }


    }

    @Override
    public void updateGame(Game game) throws RemoteException {
        String upd = "UPDATE spellen SET aantalSpelers = ?, aantalspectaters = ?, zetnr = ?,volgorde = ?, kaartlayout = ?, scores = ?, currentplayer = ?,status = ? WHERE global_spelid= ?";
        PreparedStatement prst;
        if (game.getStatus().equals("finished")) {
            System.out.println("deleting game");
            String del = "DELETE FROM spellen WHERE status = ? AND global_spelid = ?";
            try {
                prst = conn.prepareStatement(del);
                prst.setString(1, "started");
                prst.setString(2, game.getGameId());
                prst.executeUpdate();
                if (backupserver != -1) {
                    DataBaseProtocol dataBaseProtocol = databaseRegistryMap.get(backupserver);
                    if (dataBaseProtocol == null) {
                        databaseRegistry = LocateRegistry.getRegistry("localhost", backupserver);
                        databaseRegistryMap.put(backupserver, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
                    }
                    databaseRegistryMap.get(backupserver).placeBackupGame(game);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
            if (backupGames.contains(game.getGameId())) {
                backupGames.remove(game.getGameId());
            }
        } else {
            try {
                prst = conn.prepareStatement(upd);
                prst.setInt(1, game.getPlayerCount());
                prst.setInt(2, game.getSpectaterCount());
                prst.setInt(3, game.getTurnCount());
                prst.setString(4, game.playOrderToString());
                prst.setString(5, game.cardMatrixToString());
                prst.setString(6, game.scoresToString());
                prst.setInt(7, game.getCurrentplayer());
                prst.setString(8, game.getStatus());
                prst.setString(9, game.getGameId());
                prst.executeUpdate();
                if (backupserver != -1) {
                    DataBaseProtocol dataBaseProtocol = databaseRegistryMap.get(backupserver);
                    if (dataBaseProtocol == null) {
                        databaseRegistry = LocateRegistry.getRegistry("localhost", backupserver);
                        databaseRegistryMap.put(backupserver, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
                    }
                    databaseRegistryMap.get(backupserver).placeBackupGame(game);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ArrayList<Game> getPendingGames() throws RemoteException {
        ArrayList<Game> pendingGames = getGamesWithStatus("pending");
        DataBaseProtocol dataprot;
        for (Integer port : databaseList) {
            if (port != ownPort) {
                try {
                    dataprot = databaseRegistryMap.get(port);
                    if (dataprot == null) {
                        databaseRegistry = LocateRegistry.getRegistry("localhost", port);
                        databaseRegistryMap.put(port, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
                    }
                    pendingGames.addAll(databaseRegistryMap.get(port).getGamesWithStatus("pending"));
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Game game : pendingGames) {
            System.out.println(game.getGameId());

        }
        return pendingGames;
    }

    @Override
    public ArrayList<Game> getActiveGames() throws RemoteException {
        ArrayList<Game> activeGames = getGamesWithStatus("started");
        DataBaseProtocol dataprot;
        for (Integer port : databaseList) {
            if (port != ownPort) {
                try {
                    dataprot = databaseRegistryMap.get(port);
                    if (dataprot == null) {
                        databaseRegistry = LocateRegistry.getRegistry("localhost", port);
                        databaseRegistryMap.put(port, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));
                    }
                    activeGames.addAll(databaseRegistryMap.get(port).getGamesWithStatus("started"));
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Game game : activeGames) {
            System.out.println(game.getGameId());

        }
        return activeGames;
    }

    @Override
    public ArrayList<Game> getGamesWithStatus(String status) throws RemoteException {
        String query = "SELECT global_spelid, aantalspelers, currentplayer,applicationserver FROM spellen WHERE status ='" + status + "'";
        PreparedStatement prst;
        ResultSet rs;
        ArrayList<Game> pendingGameList = new ArrayList<>();
        try {
            prst = conn.prepareStatement(query);
            rs = prst.executeQuery();

            while (rs.next()) {
                if (!backupGames.contains(rs.getString("global_spelid"))) {
                    pendingGameList.add(new Game(rs.getString("global_spelid"), rs.getInt("aantalspelers"), rs.getInt("currentplayer"), rs.getString("applicationserver")));
                }
            }
            return pendingGameList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addLoss(String login) throws RemoteException {
        getUserFromPrimary(login);
        String get = "SELECT aantalverloren FROM users WHERE login ='" + login + "'";
        String upd = "UPDATE users SET aantalverloren = ? WHERE login = ? ";
        PreparedStatement prst;
        ResultSet rs;
        try {
            prst = conn.prepareStatement(get);
            rs = prst.executeQuery();
            int loss = rs.getInt("aantalverloren") + 1;
            prst = conn.prepareStatement(upd);
            prst.setInt(1, loss);
            prst.setString(2, login);
            prst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addWin(String login) throws RemoteException {
        getUserFromPrimary(login);
        String get = "SELECT aantalwins FROM users WHERE login ='" + login + "'";
        String upd = "UPDATE users SET aantalwins = ? WHERE login = ? ";
        PreparedStatement prst;
        ResultSet rs;
        try {
            prst = conn.prepareStatement(get);
            rs = prst.executeQuery();
            int win = rs.getInt("aantalwins") + 1;
            prst = conn.prepareStatement(upd);
            prst.setInt(1, win);
            prst.setString(2, login);
            prst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addDraw(String login) throws RemoteException {
        getUserFromPrimary(login);
        String get = "SELECT aantalgelijk FROM users WHERE login ='" + login + "'";
        String upd = "UPDATE users SET aantalgelijk = ? WHERE login = ? ";
        PreparedStatement prst;
        ResultSet rs;
        try {
            prst = conn.prepareStatement(get);
            rs = prst.executeQuery();
            int draw = rs.getInt("aantalgelijk") + 1;
            prst = conn.prepareStatement(upd);
            prst.setInt(1, draw);
            prst.setString(2, login);
            prst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public ArrayList<String> getThemeNames() throws RemoteException {
        String get = "select beschrijving from thema";
        ResultSet rs;
        PreparedStatement selectst;
        ArrayList<String> themeNames = new ArrayList<>();
        try {
            selectst = conn.prepareStatement(get);
            rs = selectst.executeQuery();
            while (rs.next()) {
                themeNames.add(rs.getString("beschrijving"));
            }
            return themeNames;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Theme> getPreviewThemes() throws RemoteException {
        String getId = "SELECT themaid, aantalpics, beschrijving FROM thema";
        String getThemes = "SELECT number FROM picture WHERE themaid = ? AND picnumber in (0,1,2,3)";
        ResultSet rs;
        PreparedStatement prst;
        ArrayList<Integer> themaIdList = new ArrayList<>();
        ArrayList<Theme> PreviewThemes = new ArrayList<>();
        HashMap<String, byte[]> cards;
        try {
            prst = conn.prepareStatement(getId);
            rs = prst.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("themaid"));
                PreviewThemes.add(new Theme(rs.getInt("themaid"), rs.getInt("aantalpics"), rs.getString("beschrijving")));
                themaIdList.add(rs.getInt("themaid"));
            }
            for (Integer id : themaIdList) {
                System.out.println(id);
                cards = new HashMap<>();
                prst = conn.prepareStatement(getThemes);
                prst.setInt(1, id);
                rs = prst.executeQuery();
                int i = 0;
                while (rs.next()) {
                    System.out.println("inside cardgetter");
                    cards.put(String.valueOf(i), rs.getBytes("number"));
                    i++;
                }
                PreviewThemes.get(id - 1).setCardMap(cards);
            }
            return PreviewThemes;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Game setId(Game game, String appserver) {
        String get = "select seq from sqlite_sequence where name = \"spellen\"";
        String upd = "UPDATE spellen SET global_spelid = ? WHERE spelid= ? ";
        ResultSet rs;
        PreparedStatement selectst;
        String kaartlayout = game.cardMatrixToString();
        String volgorde = game.playOrderToString();
        String applicationserver = appserver;
        String status = "pending";

        int spelid;

        String globalSpelId;

        try {
            selectst = conn.prepareStatement(get);
            rs = selectst.executeQuery();
            spelid = rs.getInt("seq");
            System.out.println(spelid);
            globalSpelId = SERVERNAME + "-" + spelid;
            game.setGameId(globalSpelId);
            selectst = conn.prepareStatement(upd);
            selectst.setString(1, globalSpelId);
            selectst.setInt(2, spelid);

            selectst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return game;
    }

    @Override
    public List<String> getThemesWithSize(int size) throws RemoteException {
        List<String> themas = new ArrayList<>();

        String query = "SELECT beschrijving FROM thema WHERE aantalpics >= " + size;
        PreparedStatement prst;
        ResultSet rs;
        try {
            prst = conn.prepareStatement(query);
            rs = prst.executeQuery();
            while (rs.next()) {
                themas.add(rs.getString("beschrijving"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return themas;

    }

    public String createJWT(String username) {

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.MINUTE, 30);
        cal.getTime(); // returns new date object, one hour in the future
        String compactJws = Jwts.builder().claim("id", 1).setSubject(username).setExpiration(cal.getTime()).signWith(SignatureAlgorithm.HS512, "pokemon1").compact();
        System.out.println("Gecreeerde string is: " + compactJws);
        return compactJws;
    }

    @Override
    public void setPreviewThemes(ArrayList<Theme> previewthemes) throws RemoteException {
        for (Theme thema : previewthemes) {
            setTheme(thema);
        }
    }

    private void setTheme(Theme theme) {

        String insThema = "INSERT INTO thema(themaid,aantalpics,beschrijving) Values(?,?,?)";
        String insPics = "INSERT INTO picture(themaid,name,number,picnumber) values(?,?,?,?)";

        PreparedStatement prst;


        try {
            prst = conn.prepareStatement(insThema);
            prst.setInt(1, theme.getThemeId());
            prst.setInt(2, theme.getSize());
            prst.setString(3, theme.getName());

            prst.executeUpdate();

            for (Map.Entry<String, byte[]> card : theme.getCardMap().entrySet()) {
                prst = conn.prepareStatement(insPics);
                prst.setInt(1, theme.getThemeId());
                prst.setString(2, theme.getName() + card.getKey());
                prst.setBytes(3, card.getValue());
                prst.setInt(4, Integer.parseInt(card.getKey()));

                prst.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void updatePics(Theme theme) {
        String insPics = "INSERT INTO picture(themaid,name,number,picnumber) values(?,?,?,?)";

        PreparedStatement prst;
        for (Map.Entry<String, byte[]> card : theme.getCardMap().entrySet()) {
            try {
                prst = conn.prepareStatement(insPics);
                prst.setInt(1, theme.getThemeId());
                prst.setString(2, theme.getName() + card.getKey());
                prst.setBytes(3, card.getValue());
                prst.setInt(4, Integer.parseInt(card.getKey()));

                prst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public class BackupChangeThread implements Runnable {

        String login;
        Game game;

        public BackupChangeThread(String login) {
            this.login = login;
            game = null;
        }

        public BackupChangeThread(Game game) {
            this.game = game;
            login = null;
        }

        @Override
        public void run() {
            if (backupserver != -1) {
                try {

                    DataBaseProtocol dataprot = databaseRegistryMap.get(backupserver);
                    if (dataprot == null) {

                        databaseRegistry = LocateRegistry.getRegistry("localhost", backupserver);
                        databaseRegistryMap.put(backupserver, (DataBaseProtocol) databaseRegistry.lookup("dataBaseService"));


                    }
                    if (login != null) {
                        databaseRegistryMap.get(backupserver).updateUser(getUser(login));
                    } else if (game != null) {
                        databaseRegistryMap.get(backupserver).placeBackupGame(game);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
