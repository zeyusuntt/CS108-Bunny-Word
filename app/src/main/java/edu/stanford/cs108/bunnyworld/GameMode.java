package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameMode extends AppCompatActivity {

    protected static final String DEFAULT_GAME_NAME = "Default Bunny World";

    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);
        String loadedGameName = Game.getLoadedGameName();
        Game loadedGame = loadGame(loadedGameName);
        Log.d("loadGameName1", loadedGameName);
        if (loadedGame == null) {
            if (loadedGameName.equals(DEFAULT_GAME_NAME)) {
                createDefaultGame();
            }
        }
        Log.d("loadGameName", loadedGameName);
        loadedGame = loadGame(loadedGameName);
        Game.setCurrGame(loadedGame);
        // Clear the possesions from previous game
        Game.getCurrGame().clearPossessions();

        TextView pointView = findViewById(R.id.points_textview);
        CharSequence newText = (CharSequence) (0+ "/" +Game.getCurrGame().getTotalPoints());
        pointView.setText(newText);
        // timer
        TextView countdownTextView = findViewById(R.id.countdown_textview);

        countDownTimer = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long l) {
                GameView gameView = findViewById(R.id.gameView);
                if (!gameView.checkWinPage()) {
                    countdownTextView.setText("" + l/1000);
                }

            }

            @Override
            public void onFinish() {
                if (!Game.isGameMode()) {
                    System.out.println("harmless finished.....");
                    finish();
                    return;
                }
                Intent intent = new Intent(GameMode.this, LoseActivity.class);
                Bundle bundle = new Bundle();
                TextView pointText = findViewById(R.id.points_textview);
                bundle.putString("text", (String) pointText.getText());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        };
        countDownTimer.start();
//        GameView gameView = findViewById(R.id.gameView);
//        if (gameView.checkWinPage()) {
//            countDownTimer.cancel();
//        }

    }
    public void createDefaultGame() {
        Game game = new Game();
        game.setCurrentGameName(DEFAULT_GAME_NAME);
        Page startPage = game.getFirstPage();
        startPage.setPageName("startPage");

        // three pages corresponding to three gates
        Page firstGate = new Page("firstGate", game);
        Page secondGate = new Page("secondGate", game);
        Page thirdGate = new Page("thirdGate", game);
        Page winPage = new Page("winPage", game);

        // the start page: three gates
        Shape gate1_1 = new Shape(200.0f, 400.0f, 100, 100, "gate1_1", startPage);
        gate1_1.getAllScript("on click play woof goto firstGate;");
        Shape gate1_2 = new Shape(800.0f, 400.0f, 100, 100, "gate1_2",startPage);
        gate1_2.setInvisible();
        gate1_2.getAllScript("on click goto secondGate;");
        Shape gate1_3 = new Shape(1400.0f, 400.0f, 100, 100, "gate1_3",startPage);
        gate1_3.getAllScript("on click goto thirdGate;");
        Shape start_text = new Shape(200.0f, 100.0f, 100.0f, 100.0f, "start_text", startPage);
        start_text.setText("You are in a maze! Choose a passage!");
        startPage.addShape(gate1_1);
        startPage.addShape(gate1_2);
        startPage.addShape(gate1_3);
        startPage.addShape(start_text);

        // the first gate page: one gate + one bunny
        Shape gateF_1 = new Shape(200.0f, 400.0f, 100.0f, 100.0f,
                "gateF_1", firstGate);
        gateF_1.getAllScript("on click goto startPage;");
        Shape gateF_bunny = new Shape(500.0f, 30.0f, 400.0f, 400.0f,
                "gateF_bunny", firstGate);
        gateF_bunny.setImageName("mystic");
        gateF_bunny.getAllScript("on click hide carrot play munching;");
        gateF_bunny.getAllScript("on enter show gate1_2;");
        Shape gateF_text = new Shape(500.0f, 400.0f, 100.0f, 100.0f,
                "gateF_text", firstGate);
        gateF_text.setText("Mystic bunny!");
        firstGate.addShape(gateF_1);
        firstGate.addShape(gateF_bunny);
        firstGate.addShape(gateF_text);
        game.addPage(firstGate);

        // the second gate page: fire + one gate + one carrot
        Shape gateS_1 = new Shape(1000.0f, 400.0f, 100.0f, 100.0f,
                "gateS_1", secondGate);
        gateS_1.getAllScript("on click goto firstGate;");
        Shape gateS_fire = new Shape(200.0f, 30.0f, 400.0f, 400.0f,
                "gateS_fire", secondGate);
        gateS_fire.setImageName("fire");
        gateS_fire.getAllScript("on enter play fire;");
        Shape gateS_carrot = new Shape(1400.0f, 200.0f, 200.0f, 200.0f,
                "gateS_carrot", secondGate);
        gateS_carrot.setImageName("carrot");
        gateS_carrot.setShapeName("carrot");
        Shape gateS_text = new Shape(400f, 450.0f, 100.0f, 100.0f,
                "gateS_text", secondGate);
        gateS_text.setText("Fire!! Run Away!");
        secondGate.addShape(gateS_1);
        secondGate.addShape(gateS_fire);
        secondGate.addShape(gateS_carrot);
        secondGate.addShape(gateS_text);
        game.addPage(secondGate);

        // the third gate page: one gate + one evil bunny
        Shape gateT_1 = new Shape(1500.0f, 400.0f, 100.0f, 100.0f,
                "gateT_1", thirdGate);
        gateT_1.setInvisible();
        gateT_1.getAllScript("on click goto winPage;");
        Shape gateT_evil = new Shape(400.0f, 30.0f, 400.0f, 400.0f,
                "gateT_evil", thirdGate);
        gateT_evil.setImageName("death");
        gateT_evil.getAllScript("on enter play evillaugh;");
        gateT_evil.getAllScript("on drop carrot hide carrot play munch hide gateT_evil show gateT_1;");
        gateT_evil.getAllScript("on click play evillaugh;");
        Shape gateT_text = new Shape(150.0f, 420.0f, 200.0f, 100.0f,
                "gateT_text", secondGate);
        gateT_text.setText("You must appease the Bunny of Death!");
        thirdGate.addShape(gateT_1);
        thirdGate.addShape(gateT_evil);
        thirdGate.addShape(gateT_text);
        game.addPage(thirdGate);

        // the win page: many carrots
        Shape win_carrot1 = new Shape(400.0f, 100.0f, 300.0f, 300.0f,
                "win_carrot1", winPage);
        Shape win_carrot2 = new Shape(800.0f, 100.0f, 300.0f, 300.0f,
                "win_carrot2", winPage);
        Shape win_carrot3 = new Shape(1200.0f, 100.0f, 300.0f, 300.0f,
                "win_carrot3", winPage);
        win_carrot1.setImageName("carrot");
        win_carrot2.setImageName("carrot");
        win_carrot3.setImageName("carrot");
        Shape win_text = new Shape(600.0f, 400.0f, 200.0f, 100.0f,
                "win_text", secondGate);
        win_text.setText("You win! YAY!");
        win_text.getAllScript("on enter play hooray;");
        winPage.addShape(win_carrot1);
        winPage.addShape(win_carrot2);
        winPage.addShape(win_carrot3);
        winPage.addShape(win_text);
        game.addPage(winPage);

        saveGame(game);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Game loadGame(String gameName) {
        Log.i("GameView", "loadGame with name:" + gameName);
        File gameFile = getGameFile(gameName);
        Game game = null;
        if (gameFile != null) {
            try {
                FileInputStream inputFile = new FileInputStream(gameFile);
                ObjectInputStream gameInput = new ObjectInputStream(inputFile);
                game = (Game) gameInput.readObject();
                gameInput.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            assert game != null;
            game.setCurrentGameName(gameFile.getName().substring(0, gameFile.getName().length() - 5));
            game.setCurrentPage(game.getFirstPage());
            return game;
        }
        return null;
    }

    private File getGameFile(String gameName) {
        File dir = new File(this.getFilesDir(), "savedGame");
        File[] games = dir.listFiles();
        if (games != null) {
            for (File file : games) {
                String game = file.getName();
                String gName = game.substring(0, game.length() - 5);
                if (gameName.equals(gName)) {
                    return file;
                }
            }
        }
        return null;
    }

    public void onEndGame(View view) {
        countDownTimer.cancel();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}