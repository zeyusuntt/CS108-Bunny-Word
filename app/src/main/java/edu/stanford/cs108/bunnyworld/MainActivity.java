package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AdapterView.OnItemSelectedListener listener;

    private String selectedGameName = GameMode.DEFAULT_GAME_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<String> gameNameList = getFileNames();
        if (gameNameList.size() == 0) {
            gameNameList.add(GameMode.DEFAULT_GAME_NAME);
        }
        Spinner gameSpinner = findViewById(R.id.gameSpinner);
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, gameNameList);
        gameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameSpinner.setAdapter(gameAdapter);
        gameSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGameName = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedGameName = GameMode.DEFAULT_GAME_NAME;
            }
        });
    }

    public void onEditor(View view) {
        Game.setGameMode(false);

        Intent intent = new Intent(this, GameManagementActivity.class);
        startActivity(intent);
    }

    public void onGamePlayer(View view) {
        Game.setGameMode(true);
        Game.setLoadedGameName(selectedGameName);
        Intent intent = new Intent(this, GameMode.class);
        startActivity(intent);
    }

    // EXTENSION: Reset Database
    public void onReset(View view) {
        Game.setGameMode(false);

        try {
            Game.deleteGames(MainActivity.this);
        } catch (Exception error) {
            error.printStackTrace();
        }
        // Set Game spinner to only default game
        Spinner gameSpinner = findViewById(R.id.gameSpinner);
        List<String> defaultGame = Arrays.asList(GameMode.DEFAULT_GAME_NAME);
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultGame);
        gameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameSpinner.setAdapter(gameAdapter);
        Toast.makeText(this,"Database Reset.",
                Toast.LENGTH_SHORT).show();
    }

    private List<String> getFileNames() {
        File dir = new File(this.getFilesDir(), "savedGame");
        List<String> names = new ArrayList<>();
        if (dir.list() == null) {
            names.add(GameMode.DEFAULT_GAME_NAME);
        } else {
            for (String fileName : dir.list()) {
                names.add(fileName.substring(0, fileName.length() - 5));
            }
        }
        return names;
    }

    /*Send game via email*/
    public void onGameShare(View view) {
        Game.setGameMode(false);

        File gameFile = getGameFile(selectedGameName);
        if(gameFile == null) {
            Toast.makeText(this,"Selected file does not exist!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareFile = new Intent(Intent.ACTION_SEND);
        shareFile.setType("application/json");
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", gameFile);
        shareFile.putExtra(Intent.EXTRA_STREAM, uri);
        shareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing Game File...");
        shareFile.putExtra(Intent.EXTRA_TEXT, "Sharing Game File...");
        shareFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareFile, "Share File"));
        Intent chooser = Intent.createChooser(shareFile, "Share File");

        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser,
                PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(chooser);
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
}