package application_server;

import Interfaces.ApplicationProtocol;
import Interfaces.DataBaseProtocol;
import Interfaces.DispatchProtocol;
import shared_objects.Game;
import shared_objects.Image;
import shared_objects.Move;
import shared_objects.Theme;

import java.lang.reflect.Array;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ApplicationProtocolImpl extends UnicastRemoteObject implements ApplicationProtocol {

    public static Registry databankServer;
    public static DataBaseProtocol dataTransfer;
    public static DispatchProtocol dispatchProtocol;
    public static ArrayList<Theme> cachedThemes = new ArrayList<>();
    public static ArrayList<Theme> cachedPreviews = new ArrayList<>();
    public static Registry dispatcher;
    public static ArrayList<Integer> applicationList;
    public static int ownPort;
    public static int databaseport;


    private HashMap<String, Game> gameMap = new HashMap<>();


    public ApplicationProtocolImpl(ArrayList<Integer> applicationList, int databaseport) throws RemoteException {
        this.applicationList = applicationList;
        ownPort = applicationList.get(applicationList.size() - 1);
        System.out.println(ownPort);
        this.databaseport = databaseport;
        setupConnection();

    }

    public void setupConnection() {
        try {
            databankServer = LocateRegistry.getRegistry("localhost", databaseport);
            dataTransfer = (DataBaseProtocol) databankServer.lookup("dataBaseService");
            dispatcher = LocateRegistry.getRegistry("localhost", 1299);
            dispatchProtocol = (DispatchProtocol) dispatcher.lookup("dispatchService");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void logout(String login, String session, boolean xButton) throws RemoteException {
        System.out.println("user logged out");
        dataTransfer.logout(login, session, xButton);
    }

    @Override
    public String respond() throws RemoteException {
        return "hello";
    }

    @Override
    public String[] login(String username, String password, String session) throws RemoteException {
        System.out.println("username: " + username + "password: " + password);
        return dataTransfer.login(username, password, session);
    }

    @Override
    public String[] register(String username, String password, byte[] salt) throws RemoteException {
        System.out.println("username: " + username + "password: " + password);
        return dataTransfer.registerUser(username, password, salt);
    }


    @Override
    public int changeUsername(String usernameField, String login, String session) throws RemoteException {
        return dataTransfer.changeUsername(usernameField, login, session);
    }

    public int changePassword(String newPassword, String login, String session) throws RemoteException {
        return dataTransfer.changePassword(newPassword, login, session);
    }

    @Override
    public double[] getUserStats(String login, String session) throws RemoteException {
        return dataTransfer.getUserStats(login, session);
    }

    @Override
    public HashMap<String, Integer> getRanking() throws RemoteException {
        return dataTransfer.getAllRankings();
    }

    @Override
    public ArrayList<String> getThemeNames() throws RemoteException {
        return dataTransfer.getThemeNames();
    }

    @Override
    public ArrayList<Theme> getPreviewThemes() throws RemoteException {
        if(cachedPreviews.isEmpty()){
        ArrayList<Theme> t= dataTransfer.getPreviewThemes();
        cachedPreviews=t;
        return t;
        }
        else{
            return cachedPreviews;
        }
    }

    @Override
    public int getOwnPort() {
        return ownPort;
    }


    //Game logica********************************************************************************************
    @Override
    public Game createGame(int playerTotal, String login, int gridTotal, String chosenThemeName, String session) throws RemoteException {

        Theme chosenTheme = null;
        for (Theme theme : cachedThemes) {
            if (theme.getName().equals(chosenThemeName)) {
                chosenTheme = theme;
                break;
            }
        }

        if (chosenTheme == null) {
            chosenTheme = dataTransfer.getTheme(chosenThemeName);
            System.out.println("THEME ID: " + chosenTheme.getThemeId());

            //Plus caching van het gekozen theme en zijn afbeeldingen
            cachedThemes.add(chosenTheme);
        }


        Game game = new Game(playerTotal, gridTotal, login, chosenTheme.getThemeId(), chosenTheme.getSize());
        game = dataTransfer.createGame(game, "server-"+ownPort, login, session);
        if (game == null) {
            System.out.println("deze game is null");
            return null;
        }

        gameMap.put(game.getGameId(), game);
        System.out.println(game.getGameId());

        dispatchProtocol.incrementGame(ownPort);
        return game;
    }

    @Override
    public Game processTurn(Move lastMove, String login, String gameId) throws RemoteException {
        Game game = gameMap.get(gameId);
        System.out.println("nextTurn geregistreerd");

        if (game.getPlayorder()[game.getCurrentplayer()].equals(login)) {
            System.out.println(game.cardMatrixToString());

            if (lastMove.getCardid1() == lastMove.getCardid2()) {
                game.getCardMatrix()[lastMove.getRow1()][lastMove.getColumn1()] = "1" + String.valueOf(lastMove.getCardid1());
                game.getCardMatrix()[lastMove.getRow2()][lastMove.getColumn2()] = "1" + String.valueOf(lastMove.getCardid2());
                game.increaseScore();

            } else {
                game.increasePlayer();
            }

            System.out.println(game.cardMatrixToString());
            System.out.println(lastMove.getCardid1() + " " + lastMove.getCardid2());
            game.setLastMove(lastMove);
            game.increaseTurnCount();

            if (game.checkFinished()) {
                game.setStatus("finished");
                game.calculateEndResults();
                System.out.println("losers");
                for (String loser : game.getLosers()) {
                    game.getEndResult().put(loser, "lose");
                    System.out.println(loser);
                    dataTransfer.addLoss(loser);
                }
                System.out.println("winners");
                if (game.getWinners().size() > 1) {
                    for (String winner : game.getWinners()) {
                        game.getEndResult().put(winner, "draw");
                        System.out.println(winner);
                        dataTransfer.addDraw(winner);
                    }
                } else {
                    game.getEndResult().put(game.getWinners().get(0), "win");
                    System.out.println(game.getWinners().get(0));

                    dataTransfer.addWin(game.getWinners().get(0));
                }
                dataTransfer.updateGame(game);
                dispatchProtocol.decreaseGame(ownPort, databaseport);
                gameMap.remove(game.getGameId());
            }

            dataTransfer.updateGame(game);
            notifyOtherPlayers(game);
            return game;
        }
        return null;
    }

    @Override
    public synchronized Game gameChanged(String gameId) throws RemoteException, InterruptedException {
        System.out.println("gamechanged aangevraagd");
        Game game = new Game(gameMap.get(gameId));
        if (game.getLastMove() != null) {
            System.out.println(game.getLastMove().getCardid1() + " " + game.getLastMove().getCardid2());
        }
        while (gameMap.get(gameId).equals(game)) {
            System.out.println(gameMap.get(gameId).equals(game));
            if (game.getLastMove() != null) {
                System.out.println(game.getLastMove().getCardid1() + " " + game.getLastMove().getCardid2());
                System.out.println(gameMap.get(gameId).getLastMove().getCardid1() + " " + gameMap.get(gameId).getLastMove().getCardid2());
            }
            wait();
            System.out.println(gameMap.get(gameId).equals(game));

        }

        return gameMap.get(gameId);
    }

    @Override
    public Game joinGame(String gameId, String login, String session) throws RemoteException {

        if (!dataTransfer.checkToken(login, session)) {
            return null;
        }

        Game game = gameMap.get(gameId);


        game.addPlayer(login);
        dataTransfer.updateGame(game);
        if (game.getPlayerCount() > 0) {
            notifyOtherPlayers(game);
        }

        return game;
    }

    @Override
    public Game spectateGame(String gameId, String login) throws RemoteException {
        Game game = gameMap.get(gameId);


        game.addSpectater();
        dataTransfer.updateGame(game);

        return game;
    }

    @Override
    public ArrayList<Game> getPendingGames() throws RemoteException {
        return dataTransfer.getPendingGames();
    }

    public ArrayList<Game> getStartedGames() throws RemoteException {
        return dataTransfer.getActiveGames();
    }

    @Override
    public void quitGame(Game game, String login) throws RemoteException {
        Game game2 = gameMap.get(game.getGameId());
        if(game2!=null) {
            game2.removeUser(login);

            dataTransfer.updateGame(game2);
            dataTransfer.addLoss(login);
            notifyOtherPlayers(game2);

        }
    }

    @Override
    public List<String> getThemesWithSize(int size) throws RemoteException {
        System.out.println("Size is; " + size);
        List<String> getThemesWithSize = dataTransfer.getThemesWithSize(size);
        System.out.println("Size van lijst is: " + getThemesWithSize.size());
        return getThemesWithSize;
    }


    private synchronized void notifyOtherPlayers(Game game) {
        //for (int i = 0; i < game.getCurrentplayer()+1; i++) {
        notifyAll();
        System.out.println("notify");
        // }

    }

    @Override
    public byte[] getSalt(String login) throws RemoteException {
        return dataTransfer.getSalt(login);
    }

    @Override
    public Theme getTheme(int themeId) throws RemoteException {
        Theme chosenTheme = null;
        for (Theme theme : cachedThemes) {
            if (theme.getThemeId() == themeId) {
                chosenTheme = theme;
                return chosenTheme;
            }
        }
        chosenTheme = dataTransfer.getTheme(themeId);
        cachedThemes.add(chosenTheme);
        return chosenTheme;
    }

}
