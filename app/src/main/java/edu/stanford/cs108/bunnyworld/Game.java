package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game implements Serializable {
    private Page currentPage, firstPage;
    private static boolean gameMode = false;
    private static Game currGame;
    private static String loadedGameName = GameMode.DEFAULT_GAME_NAME;
    private List<Page> pageList;
    protected List<Shape> possessions = new ArrayList<>();
    private String currentGameName;
    private int id = 2;
    private int shapeDefaultId = 1;
    private Shape copyShape;

    /* Constructor */
    public Game() {
        this.pageList = new ArrayList<>();
        this.firstPage = new Page("page1", this);
        this.currentPage = this.firstPage;
        this.pageList.add(this.firstPage);
    }

    public static boolean isGameMode() {
        return gameMode;
    }

    public static void setGameMode(boolean gameMode) {
        Game.gameMode = gameMode;
    }

    /* Get GameMode parameters */
    public List<Page> getPageList() {return pageList;}
    // Any page in the page list
    public Page getPage(String pageName) {
        for (Page curPage : pageList) {
            if (curPage.getPageName().equals(pageName)) {
                return curPage;
            }
        }
        return null;
    }
    // Current Page
    public Page getCurrentPage(){return currentPage;}

    // First Page
    public Page getFirstPage(){return firstPage;}
    public void setFirstPage(Page page) {
        firstPage = page;
    }

    // Current Game
    public String getCurrentGameName(){return currentGameName;}

    /* Set GameMode parameters */
    public void setCurrentGameName(String currentGameName) {
        this.currentGameName = currentGameName;
    }

    public static String getLoadedGameName(){return loadedGameName;}

    public static void setLoadedGameName(String name) {
        loadedGameName = name;
    }

    public static Game getCurrGame(){return currGame;}

    /* Set GameMode parameters */
    public static void setCurrGame(Game game) {
        currGame = game;
        GameView.setCurrGame(game);
    }

    public void setCurrentPage(String pageName) {
        if (currentPage != null) {
            currentPage.isCurPage = false;
        }
//        System.out.println("try to set " + pageName + "as curPage");
        Page thisPage = getPage(pageName);
        thisPage.isCurPage = true;
        this.currentPage = thisPage;
    }

    public void setCurrentPage(Page newPage) {
        if (currentPage != null) {
            currentPage.isCurPage = false;
        }
        newPage.isCurPage = true;
        this.currentPage = newPage;
    }

    public void addPage(Page page) {
        this.pageList.add(page);
    }
    public Page addPage() {
        Page newPage = new Page("page" + id, this);
        id++;
        this.pageList.add(newPage);
        return newPage;
    }

    public List<Page> getPageBelong() {
        return pageList;
    }

    public void deletePage(Page page) {
        int index = pageList.indexOf(page);
        if ((index <= pageList.indexOf(currentPage) && pageList.indexOf(currentPage) != 0)) {
            currentPage = pageList.get(pageList.indexOf(currentPage) - 1);
        }
        if ((index <= pageList.indexOf(firstPage) && pageList.indexOf(firstPage) != 0)) {
            firstPage = pageList.get(pageList.indexOf(firstPage) - 1);;
        }
        pageList.remove(page);
    }

    public Shape getCopyShape() {
        if (copyShape == null) return null;
        return new Shape(copyShape);
    }

    public void setCopyShape(Shape shape) {
        this.copyShape = shape;
    }

    public int getShapeDefaultId() {
        return shapeDefaultId;
    }

    public void setShapeDefaultId(int id) {
        shapeDefaultId = id;
    }

    // Extension: Reset database
    public static void deleteGames(Context context) {
        // Get current game name list
        String [] gameList = getCurrGameList(context);

        if (gameList.length == 0) {
            Log.d("deleteGames", "The game list is empty");
            return;
        }

        for (String game : gameList) {
            if (!game.equals("Default Bunny World.json")) {
                File deleteFile = new File(context.getFilesDir() + "/savedGame", game);
                deleteFile.delete();
                Log.d("deleteGames", deleteFile + " is deleted");
            }
        }
    }

    public static String [] getCurrGameList(Context context) {
        String [] currGameList = null;

        try {
            currGameList = Game.getGameNames(context);
        }
        catch (Exception error){
            error.printStackTrace();
        }
        return currGameList;
    }

    public static String[] getGameNames(Context context) {
        File dir = new File(context.getFilesDir(), "savedGame");
        String[] gameNames = dir.list();
        return gameNames;
    }

    public void clearPossessions() {
        List<Shape> list = new ArrayList<>(currentPage.getPossessions());
        for (Shape shape : list) {
            currentPage.removeFromPossessions(shape);
        }
        currentPage.setIndex(0);
    }

    public int getTotalPoints() {
        int res = 0;
        for (Page page: pageList) {
            for (Shape shape: page.getShapeList()) {
                res += shape.getOnClickActions().isEmpty() ? 0:1;
                res += shape.getOnEnterActions().isEmpty() ? 0:1;
                res += shape.getOnDropActions().size();
            }
        }
        return 5 * res;
    }
}
