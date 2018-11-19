package application_server;

import Interfaces.ApplicationProtocol;
import Interfaces.DataBaseProtocol;
import Interfaces.DispatchProtocol;
import shared_objects.Game;
import shared_objects.Move;
import shared_objects.Theme;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ApplicationProtocolImpl extends UnicastRemoteObject implements ApplicationProtocol {

    public static Registry databankServer;
    public static DataBaseProtocol dataTransfer;

    private HashMap<String, Game> gameMap = new HashMap<>();


    public ApplicationProtocolImpl() throws RemoteException {
        setupConnection();
    }

    public void setupConnection() {
        try {
            databankServer = LocateRegistry.getRegistry("localhost", 1499);
            dataTransfer = (DataBaseProtocol) databankServer.lookup("dataBaseService");

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
    public String[] register(String username, String password) throws RemoteException {
        System.out.println("username: " + username + "password: " + password);
        return dataTransfer.registerUser(username, password);
    }



    @Override
    public int changeUsername(String usernameField, String login)throws RemoteException{
        return dataTransfer.changeUsername(usernameField,login);
    }

    public int changePassword(String newPassword, String login) throws RemoteException{
        return dataTransfer.changePassword(newPassword,login);
    }

    @Override
    public double[] getUserStats(String login) throws RemoteException{
        return  dataTransfer.getUserStats(login);
    }

    @Override
    public HashMap<String, Integer> getRanking() throws RemoteException{
        return dataTransfer.getRanking();
    }

    //Game logica********************************************************************************************
    @Override
    public Game createGame(int playerTotal, String login, int gridTotal, String chosenThemeName) throws RemoteException {
        Theme chosenTheme = dataTransfer.getTheme(chosenThemeName);
        Game game = new Game(playerTotal, gridTotal, login, chosenTheme);
        game = dataTransfer.createGame(game, "server1");
        gameMap.put(game.getGameId(), game);
        System.out.println(game.getGameId());
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

            }else{
                game.increasePlayer();
            }

            System.out.println(game.cardMatrixToString());
            System.out.println(lastMove.getCardid1()+" "+lastMove.getCardid2());
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
        if(game.getLastMove()!=null){
            System.out.println(game.getLastMove().getCardid1()+" "+game.getLastMove().getCardid2());
        }
        while(gameMap.get(gameId).equals(game)){
            System.out.println(gameMap.get(gameId).equals(game));
            if(game.getLastMove()!=null) {
                System.out.println(game.getLastMove().getCardid1() + " " + game.getLastMove().getCardid2());
                System.out.println(gameMap.get(gameId).getLastMove().getCardid1() + " " + gameMap.get(gameId).getLastMove().getCardid2());
            }
            wait();
            System.out.println(gameMap.get(gameId).equals(game));

        }

        return gameMap.get(gameId);
    }

    @Override
    public Game joinGame(String gameId, String login) throws RemoteException {

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
        return dataTransfer.getGamesWithStatus("pending");
    }

    public ArrayList<Game> getStartedGames() throws RemoteException {
        return dataTransfer.getGamesWithStatus("started");
    }

    @Override
    public void quitGame(Game game, String login) throws RemoteException {
        Game game2 = gameMap.get(game.getGameId());
        game2.removeUser(login);

        dataTransfer.updateGame(game2);
        dataTransfer.addLoss(login);
        notifyOtherPlayers(game2);


    }


    private synchronized void notifyOtherPlayers(Game game) {
        //for (int i = 0; i < game.getCurrentplayer()+1; i++) {
            notifyAll();
        System.out.println("notify");
       // }

    }


}
