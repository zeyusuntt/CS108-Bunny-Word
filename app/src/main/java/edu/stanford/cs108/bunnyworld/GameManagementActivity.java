package edu.stanford.cs108.bunnyworld;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameManagementActivity extends AppCompatActivity {

    private GameManagementView gameCatalogView;

    private static String DEFAULT_NEW_GAME_NAME = "New Game";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_game_management);
        gameCatalogView = findViewById(R.id.game_catalog_view);
    }

    public void onCreateGame(View view) {
        if (checkDefaultName()) {
            Toast.makeText(this, "Please rename existed 'New Game'!", Toast.LENGTH_SHORT).show();
        } else {
            Game game = new Game();
            game.setCurrentGameName(DEFAULT_NEW_GAME_NAME);
            saveGame(game);
            Game.setCurrGame(game);
        }
        gameCatalogView.invalidate();
    }

    public void onEditGame(View view) {
        if (gameCatalogView.getSelected() != null) {
            if (gameCatalogView.getSelected().equals("Default Bunny World.json")){
                Toast.makeText(this, "Default Game cannot be edited!", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                String selectedGameName = gameCatalogView.getSelected();
                Game game = loadGame(selectedGameName.substring(0, selectedGameName.length() - 5));
                Game.setCurrGame(game);
                startActivity(new Intent(this, PageManagementActivity.class));
            }
        }
    }

    public void onBack(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void onDeleteGame(View view) {
        if (gameCatalogView.getSelected() != null) {
            if (gameCatalogView.getSelected().equals("Default Bunny World.json")){
                Toast.makeText(this, "Default Game cannot be deleted!", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                deleteGame(gameCatalogView.getSelected());
                gameCatalogView.clearSelected();
                gameCatalogView.invalidate();
            }
        }
    }

    public void onChangeGameName(View view) {
        if (gameCatalogView.getSelected() != null) {
            if (gameCatalogView.getSelected().equals("Default Bunny World.json")){
                Toast.makeText(this, "Default Game cannot be edited!", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
            EditText newName = findViewById(R.id.game_new_name);
            if (newName.getText().toString().length() == 0) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
            } else if (checkDuplicateName(newName.getText().toString())) {
                Toast.makeText(this, "Name already exists!", Toast.LENGTH_SHORT).show();
            } else {
                // rename game
                String game = gameCatalogView.getSelected();

                File oldGameFile = getGameFile(game.substring(0, game.length() - 5));
                if (oldGameFile == null) {
                    return;
                }
                File gameDir = new File(this.getFilesDir(), "savedGame");
                File newNameFile = new File(gameDir, newName.getText().toString() + ".json");
                newNameFile.setWritable(true);
                oldGameFile.renameTo(newNameFile);

                newName.setText("");
            }
            gameCatalogView.invalidate();
            }
        }
    }

    private boolean checkDefaultName() {
        String[] fileNames = getFileNames();
        for (String fileName : fileNames) {
            String gameName = fileName.substring(0, fileName.length() - 5);
            if(gameName.equals(DEFAULT_NEW_GAME_NAME)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDuplicateName(String newGameName) {
        String[] fileNames = getFileNames();
        for (String fileName : fileNames) {
            String gameName = fileName.substring(0, fileName.length() - 5);
            if(gameName.equals(newGameName)) {
                return true;
            }
        }
        return false;
    }

    private String[] getFileNames() {
        File dir = new File(this.getFilesDir(), "savedGame");
        return dir.list();
    }

    private Game loadGame(String gameName) {
        Log.i("loadGame with name:", gameName);
        File gameFile = getGameFile(gameName);
        Game game = null;
        if (gameFile != null) {
            try {
                FileInputStream inputFile = new FileInputStream(gameFile);
                ObjectInputStream gameInput = new ObjectInputStream(inputFile);
                game = (Game) gameInput.readObject();
                gameInput.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            game.setCurrentGameName(gameFile.getName().substring(0, gameFile.getName().length() - 5));
            game.setCurrentPage(game.getFirstPage());
            return game;
        }
        return null;
    }

    private void saveGame(Game game) {
        // save in the files folder for the app
        File dir = this.getFilesDir();
        File gameDir = new File(dir, "savedGame");
        if (!gameDir.isDirectory()) {
            gameDir.mkdir();
        }

        File file = new File(gameDir, game.getCurrentGameName() + ".json");
        file.setWritable(true);
        try {
            FileOutputStream fileOutput = new FileOutputStream(file);
            ObjectOutputStream writer = new ObjectOutputStream(fileOutput);
            writer.writeObject(game);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteGame(String gameName) {
        File gameFile = getGameFile(gameName.substring(0, gameName.length() - 5));
        if (gameFile != null && gameFile.isFile()) {
            gameFile.delete();
        }
    }

    private File getGameFile(String gameName) {
        File dir = new File(this.getFilesDir(), "savedGame");
        File[] games = dir.listFiles();
        for (File file : games) {
            String game = file.getName();
            String gName = game.substring(0, game.length() - 5);
            if (gameName.equals(gName)) {
                return file;
            }
        }
        return null;
    }

}
