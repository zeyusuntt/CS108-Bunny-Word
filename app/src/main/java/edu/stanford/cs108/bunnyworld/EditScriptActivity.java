package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditScriptActivity extends AppCompatActivity {
    private EditScriptView editScriptView;

    private Page curPage;
    private Shape curShape;
    private List<String> actionList = new ArrayList<>();
    private GameView.Action action;
    private String trigger, dropBy, verb, object;

    private transient Game game = Game.getCurrGame();
    private List<Page> pageList = game.getPageList();

    private final List<String> triggerList = Arrays.asList("on click", "on enter", "on drop");
    // TODO: more verbs -> animate/stopanimate
    private final List<String> verbList = Arrays.asList("goto", "play", "hide", "show");
    private final List<String> soundList = Arrays.asList("carrotcarrotcarrot", "evillaugh", "fire",
            "hooray", "munch", "munching", "woof");

    private List<String> shapesCreated = new ArrayList<>();
    private List<String> pagesCreated = new ArrayList<>();
    Spinner triggerSpinner, dropSpinner, verbSpinner, objectSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_script);

        editScriptView = (EditScriptView) findViewById(R.id.editScriptView);
        curPage = game.getCurrentPage();
        curShape = curPage.getSelectedShape();
        actionList = new ArrayList<>(curShape.getAllScripts());

        // TODO: remove script holder

        // set up triggerSpinner
        triggerSpinner = findViewById(R.id.triggerSpinner);
        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, triggerList);
        triggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        triggerSpinner.setAdapter(triggerAdapter);
        triggerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                trigger = adapterView.getItemAtPosition(i).toString();
                if (trigger.equals("on drop")) {
                    updateShapesCreated();
                } else {
                    shapesCreated.clear();
                }
                setSpinner(dropSpinner, shapesCreated);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // set up dropSpinner
        dropSpinner = findViewById(R.id.dropSpinner);
        dropSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dropBy = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // set up verbSpinner
        verbSpinner = findViewById(R.id.verbSpinner);
        ArrayAdapter<String> verbAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, verbList);
        verbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        verbSpinner.setAdapter(verbAdapter);
        verbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                verb = adapterView.getItemAtPosition(i).toString();
                if (verb.equals("goto")) {
                    updatePagesCreated();
                    setSpinner(objectSpinner, pagesCreated);
                } else if (verb.equals("play")) {
                    setSpinner(objectSpinner, soundList);
                } else if (verb.equals("hide") || verb.equals("show")) {
                    updateShapesCreated();
                    List<String> objectList = new ArrayList<>(shapesCreated);
                    if (!objectList.contains(curShape.getShapeName())) {
                        objectList.add(curShape.getShapeName());
                    }
                    setSpinner(objectSpinner, objectList);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // set up objectSpinner
        objectSpinner = findViewById(R.id.objectSpinner);
        objectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                object = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void onAdd(View view) {
        String script = "";
        if (dropBy == null) {
            script = trigger + " " + verb + " " + object + ";";
        } else {
            script = trigger + " " + dropBy + " " + verb + " " + object + ";";
        }
        dropBy = null;

        // System.out.println("Adding new Script '" + script +"' to " + curShape.getShapeName());
        // curShape.prepareToAddScript(script);
        curShape.getAllScript(script);

        // System.out.println("on click actions: " + curShape.getOnClickActions().size());
        saveGame(game);
        editScriptView.invalidate();
    }

    public void onUpdate(View view) {
        String oldScript = editScriptView.getSelectedScript();
        System.out.println(oldScript);
        if (oldScript == null) {
            Toast.makeText(this, "Please select a script.", Toast.LENGTH_SHORT).show();
            return;
        }
        String newScript = oldScript;

        if (oldScript.startsWith("on click")) {
            if (!trigger.equals("on click")) {
                Toast.makeText(this, "The trigger cannot be changed.", Toast.LENGTH_SHORT).show();
                return;
            }

            newScript = modifyScript(oldScript, verb, object);
            deleteHelper(oldScript);
            curShape.getAllScript(newScript);
        } else if (oldScript.startsWith("on enter")) {
            if (!trigger.equals("on enter")) {
                Toast.makeText(this, "The trigger cannot be changed.", Toast.LENGTH_SHORT).show();
                return;
            }

            newScript = modifyScript(oldScript, verb, object);
            deleteHelper(oldScript);
            curShape.getAllScript(newScript);
            System.out.println("onEnter scripts size: " + curShape.getAllScripts().size());
        } else if (oldScript.startsWith("on drop")) {
            if (!trigger.equals("on drop")) {
                Toast.makeText(this, "The trigger cannot be changed.", Toast.LENGTH_SHORT).show();
                return;
            }

            newScript = modifyOnDropScript(oldScript, dropBy, verb, object);
            deleteHelper(oldScript);
            curShape.getAllScript(newScript);
        }

        editScriptView.setSelectedScript(newScript);
        saveGame(game);
        editScriptView.invalidate();
    }

    private String modifyOnDropScript(String oldScript, String dropBy, String verb, String object) {
        String[] actions = oldScript.substring("on drop".length()+1,
                oldScript.length()-1).split(" ");

        // Check if the same action already exists
        if (actions[0].equals(dropBy)) {
            for (int i = 1; i < actions.length; i += 2) {
                if (actions[i].equals(verb) && actions[i + 1].equals(object)) {
                    Toast.makeText(this, "Nothing change...", Toast.LENGTH_SHORT).show();
                    return oldScript;
                }
            }
        }

        // Change the oldScript
        if (actions[0].equals(dropBy)) {
            if (verb.equals("hide") || verb.equals("show")) {
                return oldScript.substring(0, oldScript.length() - 1) + " " + verb + " " + object + ";";
            } else if (verb.equals("play") || verb.equals("goto")) {
                boolean findSameVerb = false;
                for (int i = 1; i < actions.length; i += 2) {
                    if (actions[i].equals(verb)) {
                        actions[i + 1] = object;
                        findSameVerb = true;
                        break;
                    }
                }
                if (!findSameVerb) {
                    return oldScript.substring(0, oldScript.length() - 1) + " " + verb + " " + object + ";";
                }
            }
        } else {
            // change the dropBy shape -> a new script
            actions = new String[]{dropBy, verb, object};
        }

        StringBuilder sb = new StringBuilder();
        for (String action : actions) {
            sb.append(action).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return oldScript.substring(0, "on drop".length()) + " " + sb.toString() + ";";
    }

    private String modifyScript(String oldScript, String verb, String object) {
        String[] actions = oldScript.substring("on click".length()+1,
                oldScript.length()-1).split(" ");

        // Check if the same action already exists
        for (int i = 0; i < actions.length; i += 2) {
            if (actions[i].equals(verb) && actions[i + 1].equals(object)) {
                Toast.makeText(this, "Nothing change...", Toast.LENGTH_SHORT).show();
                return oldScript;
            }
        }

        // Change the oldScript
        if (verb.equals("hide") || verb.equals("show")) {
            return oldScript.substring(0, oldScript.length() - 1) + " " + verb + " " + object + ";";
        } else if (verb.equals("play") || verb.equals("goto")) {
            boolean findSameVerb = false;
            for (int i = 0; i < actions.length; i += 2) {
                if (actions[i].equals(verb)) {
                    actions[i + 1] = object;
                    findSameVerb = true;
                }
            }
            if (!findSameVerb) {
                return oldScript.substring(0, oldScript.length() - 1) + " " + verb + " " + object + ";";
            }
        }

        // concatenate
        StringBuilder sb = new StringBuilder();
        for (String action : actions) {
            sb.append(action).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return oldScript.substring(0, "on click".length()) + " " + sb + ";";
    }


    public void onDelete(View view) {
        String script = editScriptView.getSelectedScript();
        if (script == null) {
            Toast.makeText(this, "Please select a script", Toast.LENGTH_SHORT).show();
            return;
        }

        deleteHelper(script);

        saveGame(game);
        editScriptView.invalidate();
    }

    private void deleteHelper(String script) {
        actionList.remove(script);
        editScriptView.clearSelected();

        // update the shape
        curShape.clearScript(script);
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, EditShapeActivity.class);
        startActivity(intent);
    }

    /* --------------------------------------------------------------------------------------------
     * ----------------------------------- Helper function ----------------------------------------
     * --------------------------------------------------------------------------------------------
     */

    // Check all shapes in the game, add all shapes except the curShape itself
    // to shapesCreated
    private void updateShapesCreated() {
        for (Page page : game.getPageList()) {
            ArrayList<Shape> shapes = new ArrayList<>(page.getShapeList());
            for (Shape shape : shapes) {
                if (!shapesCreated.contains(shape) &&
                        !curShape.getShapeName().equals(shape.getShapeName())) {
                    shapesCreated.add(shape.getShapeName());
                }
            }
        }
    }

    // Check all pages in the game, add all pagess (including curPage) to pagesCreated
    private void updatePagesCreated() {
        for (Page page : pageList) {
            if (!pagesCreated.contains(page.getPageName())) {
                pagesCreated.add(page.getPageName());
            }
        }
    }

    // Set the spinner values according to the given options list
    private void setSpinner(Spinner spinner, List<String> options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void saveGame(Game game) {
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

    public void onSetFirst(View view) {
        String script = editScriptView.getSelectedScript();
        if (script == null || script.length() == 0) {
            Toast.makeText(this, "You need to select an onClick() script.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!script.startsWith("on click")) {
            Toast.makeText(this, "You can only set onClick() script.", Toast.LENGTH_SHORT).show();
            return;
        }

        curShape.setFirstOnClick(script);
        editScriptView.setSelectedScript(script);

        saveGame(game);
        editScriptView.invalidate();
    }
}