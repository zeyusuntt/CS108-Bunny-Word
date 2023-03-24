package edu.stanford.cs108.bunnyworld;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Stack;

public class PageManagementActivity extends AppCompatActivity {

    private PageManagementView pageManagementView;
    private Game game;
    private Stack<String> actionStack = new Stack<>();
    private Stack<Page> addedPageStack = new Stack<>();
    private Stack<Page> deletedPageStack = new Stack<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_management);
        pageManagementView = findViewById(R.id.page_catalog_view);
        game = Game.getCurrGame();
        // set edit mode
        Game.setGameMode(false);
        actionStack = new Stack<>();
        addedPageStack = new Stack<>();
        deletedPageStack = new Stack<>();
    }

    public void onCreatePage(View view) {
        Page addedPage = game.addPage();
        actionStack.push("add");
        addedPageStack.push(addedPage);
        saveGame(game);
        pageManagementView.invalidate();
    }

    public void onEditPage(View view) {
        if (pageManagementView.getSelected() == null) {
            Toast.makeText(this, "You should select a page!", Toast.LENGTH_SHORT).show();
        }
        else {
            EditShapeActivity.clearUndoStacks();
            game.setCurrentPage(pageManagementView.getSelected().getPageName());
            startActivity(new Intent(this, EditShapeActivity.class));
        }
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, GameManagementActivity.class);
        startActivity(intent);
    }

    public void onSetFirstPage(View view) {
        if (pageManagementView.getSelected() == null) {
            Toast.makeText(this, "You should select a page!", Toast.LENGTH_SHORT).show();
        }
        else {
            game.setFirstPage(pageManagementView.getSelected());
            saveGame(game);
            pageManagementView.invalidate();
        }
    }

    public void onDeletePage(View view) {
        if (pageManagementView.getSelected() == null) {
            Toast.makeText(this, "You should select a page!", Toast.LENGTH_SHORT).show();
        }
        else {
            if (game.getPageList().size() == 1) {
                Toast.makeText(this, "You should have at least one page!", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ifFirst = pageManagementView.getSelected().equals(game.getFirstPage());
            game.deletePage(pageManagementView.getSelected());
            if (ifFirst) {
                game.setFirstPage(game.getPageList().get(0));
            }
            actionStack.push("delete");
            deletedPageStack.push(pageManagementView.getSelected());
            pageManagementView.clearSelected();
            saveGame(game);
            pageManagementView.invalidate();
        }
    }

    public void onChangePageName(View view) {
        if (pageManagementView.getSelected() == null) {
            Toast.makeText(this, "You should select a page!", Toast.LENGTH_SHORT).show();
        }
        else {
            EditText newName = findViewById(R.id.page_new_name);
            String changeName = newName.getText().toString();
            for (int i = changeName.length() - 1; i >= 0; i--) {
                char curChar = changeName.charAt(i);
                if (!Character.isDigit(curChar)) {
                    break;
                }
                if (Character.isDigit(changeName.charAt(i))) {
                    Toast.makeText(this, "Invalid Names!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (newName.getText().toString().length() == 0) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
            } else if (checkDuplicateName(newName.getText().toString())) {
                Toast.makeText(this, "Name already exists!", Toast.LENGTH_SHORT).show();
            } else {
                pageManagementView.getSelected().setPageName(newName.getText().toString());
                newName.setText("");
                saveGame(game);
            }
            pageManagementView.invalidate();
        }
    }

    public void onUndoPages(View view) {
        if (actionStack.isEmpty()) {
            return;
        }
        switch (actionStack.pop()) {
            case "add":
                game.deletePage(addedPageStack.pop());
                break;
            case "delete":
                game.addPage(deletedPageStack.pop());
                break;
        }
        saveGame(game);
        pageManagementView.invalidate();

        if (game.getFirstPage() == null) {
            game.setFirstPage(game.getPageList().get(0));
        }
    }

    private boolean checkDuplicateName(String newName){
        for(Page p : game.getPageList()){
            String currName = p.getPageName();
            if(currName.equals(newName)){
                return true;
            }
        }
        return false;
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
}
