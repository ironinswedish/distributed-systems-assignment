package shared_objects;

public class User {

    private String login;
    private String paswoord;
    private String token;
    private int sessiontime;
    private boolean loggedin;
    private int aantalwins;
    private int aantalgelijk;
    private int aantalverloren;
    private byte[] salt;

    public User(){

    }

    public User( String login, String paswoord, String token, int sessiontime, boolean loggedin, int aantalwins, int aantalgelijk, int aantalverloren, byte[] salt) {
        this.login = login;
        this.paswoord = paswoord;
        this.token = token;
        this.sessiontime = sessiontime;
        this.loggedin = loggedin;
        this.aantalwins = aantalwins;
        this.aantalgelijk = aantalgelijk;
        this.aantalverloren = aantalverloren;
        this.salt = salt;
    }


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPaswoord() {
        return paswoord;
    }

    public void setPaswoord(String paswoord) {
        this.paswoord = paswoord;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getSessiontime() {
        return sessiontime;
    }

    public void setSessiontime(int sessiontime) {
        this.sessiontime = sessiontime;
    }

    public boolean isLoggedin() {
        return loggedin;
    }

    public void setLoggedin(boolean loggedin) {
        this.loggedin = loggedin;
    }

    public int getAantalwins() {
        return aantalwins;
    }

    public void setAantalwins(int aantalwins) {
        this.aantalwins = aantalwins;
    }

    public int getAantalgelijk() {
        return aantalgelijk;
    }

    public void setAantalgelijk(int aantalgelijk) {
        this.aantalgelijk = aantalgelijk;
    }

    public int getAantalverloren() {
        return aantalverloren;
    }

    public void setAantalverloren(int aantalverloren) {
        this.aantalverloren = aantalverloren;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}
