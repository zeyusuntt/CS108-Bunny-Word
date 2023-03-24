package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
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
import java.util.Stack;

public class EditShapeActivity extends AppCompatActivity {
    private transient Game game = Game.getCurrGame();
    private String shapeName, imageName, text, bgmName;
    private float left = 1.0f, top = 1.0f, height = 100.0f, width = 100.0f;
    private EditShapeView editShapeView;
    private CheckBox movableCheck;
    private CheckBox visibleCheck;
    private CheckBox animateCheck;

    // Type
    private final List<String> typeList = Arrays.asList("tech_bg", "strange_bg", "tree_bg",
            "sun_bg", "house_bg", "box","text","carrot","carrot01","carrot02"
            ,"carrot03","carrot04","carrot05","death","duck", "fire","mystic", "rabbit01", "rabbit02",
            "rabbit03", "boba");
    private ArrayAdapter<String> spAdapter;
    private Spinner shapeSpinner;

    // BGM
    private final List<String> bgmList = Arrays.asList("", "christmas", "corporate", "dance",
            "enjoy", "maldives", "tropical");
    private ArrayAdapter<String> bgmAdapter;
    private Spinner bgmSpinner;

    // Font family, style and color
    private float fontSize;
    private String fontType;
    private String fontStyle;
    // Shape border line width
    private float lineWidth;

    private final List<String> fontStyleList = Arrays.asList("NORMAL","BOLD","ITALIC");
    private final List<String> fontList = Arrays.asList("sans-serif","sans-serif-black","sans-serif-light",
            "sans-serif-medium","sans-serif-smallcaps","sans-serif-thin","sans-serif-condensed",
            "sans-serif-condensed-light","sans-serif-condensed-medium","serif","serif-monospace",
            "casual","cursive");
    private ArrayAdapter<String> fontNameAdapter;
    private ArrayAdapter<String> fontStyleAdapter;
    private Spinner fontNameSpinner;
    private Spinner fontStyleSpinner;
    private AdapterView.OnItemSelectedListener listener;
    private int red = 0, green = 0, blue = 0;

    // undo - stacks
    public static Stack<String> actionStack = new Stack<>();
    private static Stack<Shape> addedShapeStack = new Stack<>();
    private static Stack<Shape> deletedShapeStack = new Stack<>();
    private static Stack<Shape> beforeEditedShapeStack = new Stack<>();
    private static Stack<Shape> afterEditedShapeStack = new Stack<>();
    public static Stack<Shape> beforeMoveShapeStack = new Stack<>();
    public static Stack<Shape> afterMoveShapeStack = new Stack<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_editor_mode);

        editShapeView = (EditShapeView) findViewById(R.id.editShapeView);
        movableCheck = (CheckBox) findViewById(R.id.movable);
        // Get type of the shape
        shapeSpinner = findViewById(R.id.shapeSpinner);
        spAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout,typeList);
        spAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
        shapeSpinner.setAdapter(spAdapter);

        EditText textET = (EditText) findViewById(R.id.text);
        EditText fontET = (EditText) findViewById(R.id.fontSize);
        fontStyleSpinner = (Spinner) findViewById(R.id.fontStyleSpinner);
        EditText lineWidthET = (EditText) findViewById(R.id.lineWidth);
        shapeSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = adapterView.getItemAtPosition(i).toString();
                if (type.equals("box")||type.equals("text")){
                    imageName = "";
                } else{
                    imageName = type;
                }

                textET.setEnabled(type.equals("text"));
                fontET.setEnabled(type.equals("text"));
                fontNameSpinner.setEnabled(type.equals("text"));
                fontStyleSpinner.setEnabled(type.equals("text"));
                lineWidthET.setEnabled(type.equals("box"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Get selected font family
        fontNameSpinner = findViewById(R.id.fontSpinner);
        fontNameAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout,fontList);
        fontNameAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
        fontNameSpinner.setAdapter(fontNameAdapter);
        fontNameSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fontType = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Get selected font style
        fontStyleSpinner = findViewById(R.id.fontStyleSpinner);
        fontStyleAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout, fontStyleList);
        fontStyleAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
        fontStyleSpinner.setAdapter(fontStyleAdapter);
        fontStyleSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fontStyle = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Get bgm
        bgmSpinner = findViewById(R.id.bgmSpinner);
        bgmAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout, bgmList);
        bgmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        bgmSpinner.setAdapter(bgmAdapter);
        bgmSpinner.setOnItemSelectedListener(listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bgmName = adapterView.getItemAtPosition(i).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void onAdd(View view){
        // If a shape is selected, add cannot be done
        // Is this page the current page??
        if(game.getCurrentPage().getSelectedShape() != null) {
            Toast.makeText(this,"Please unselect the current shape",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Get bgm
        if ( (bgmName != "" && game.getCurrentPage().getBGM().length() == 0) || game.getCurrentPage().getBGM() != bgmName) {
            //add new bgm
            game.getCurrentPage().setBGM(bgmName);

            // undo
            actionStack.push("add");
            if (bgmName != "") {
                Toast.makeText(this, "Added " + bgmName + " successfully!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Get all value from user
        EditText xEdit = (EditText) findViewById(R.id.x);
        EditText yEdit = (EditText) findViewById(R.id.y);
        EditText widthEdit = (EditText) findViewById(R.id.width);
        EditText heightEdit = (EditText) findViewById(R.id.height);
        EditText nameEdit = (EditText) findViewById(R.id.shapeName);
        EditText textEdit = (EditText) findViewById(R.id.text);
        EditText fontEdit = (EditText) findViewById(R.id.fontSize);
        EditText lineWidthEdit = (EditText) findViewById(R.id.lineWidth);

        visibleCheck = (CheckBox) findViewById(R.id.visible);
        movableCheck = (CheckBox) findViewById(R.id.movable);
        animateCheck = (CheckBox) findViewById(R.id.animate);

        SeekBar redBar = (SeekBar) findViewById(R.id.red);
        SeekBar greenBar = (SeekBar) findViewById(R.id.green);
        SeekBar blueBar = (SeekBar) findViewById(R.id.blue);

        red = redBar.getProgress();
        green = greenBar.getProgress();
        blue = blueBar.getProgress();

        shapeName = nameEdit.getText().toString();
        if (shapeName.length() == 0) {
            int defaultId = game.getShapeDefaultId();
            shapeName = "shape" + defaultId;
            game.setShapeDefaultId(defaultId + 1);
        }
        // Name check for duplication
        if (sameNameExists(shapeName)) {
            Toast.makeText(this,"Name already exists! Please change another.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Get position of the shape
        if (xEdit.getText().toString().length() != 0){
            left = Float.parseFloat(xEdit.getText().toString());
        }

        if (yEdit.getText().toString().length() != 0){
            top = Float.parseFloat(yEdit.getText().toString());
        }

        if (widthEdit.getText().toString().length() != 0){
            width = Float.parseFloat(widthEdit.getText().toString());
        }

        if (heightEdit.getText().toString().length() != 0) {
            height = Float.parseFloat(heightEdit.getText().toString());
        }

        // Get text of the shape
        if (textEdit.getText().toString().length() == 0){
            text = "";
        } else {
            text = textEdit.getText().toString();
        }

        if (imageName.contains("_bg")){
            // Create shape and set newly added shape as unselected
            Log.d("background", "imageName = "  + imageName);
            Log.d("background", "shapeName = "  + shapeName);

            float background_height = (float) (editShapeView.viewHeight * 0.85);

            Shape background = new Shape(0, 0, editShapeView.viewWidth,
                    background_height, "", text, false, game.getCurrentPage());

            game.getCurrentPage().setBackgroundImage(background);
            // Set image if not null
            if (imageName.length() != 0) {
                background.setImageName(imageName);
            }
            // Change shape name if set
            if (shapeName.length() != 0) {
                background.setName(shapeName);
            }

            // Add background image to the first of the shapeList
            List<Shape> shapeList = game.getCurrentPage().getShapeList();
            shapeList.add(0,background);
            background.setUnMovable();

            // undo
            actionStack.push("add");
            addedShapeStack.push(background);

            Toast.makeText(this, "Added " + background.getShapeName() + "(background) successfully!", Toast.LENGTH_SHORT).show();
        }
        else {
            // Create shape and set newly added shape as selected
            Shape currShape = new Shape(left, top, width, height, "", text,
                    true, game.getCurrentPage());

            // Set image if not null
            if (imageName.length() != 0) {
                currShape.setImageName(imageName);
            }

            // Change shape name if set
            if (shapeName.length() != 0) {
                currShape.setName(shapeName);
            }

            game.getCurrentPage().addShape(currShape);

            // Set newly added shape as selected on current page
            game.getCurrentPage().setSelectedShape(currShape);

            // Change font size if set
            if (fontEdit.getText().toString().length() != 0) {
                fontSize = Float.parseFloat(fontEdit.getText().toString());
                currShape.setFontSize(fontSize);
            }

            if (lineWidthEdit.getText().toString().length() != 0) {
                lineWidth = Float.parseFloat(lineWidthEdit.getText().toString());
                currShape.setOutlineWidth(lineWidth);
            }

            // Set font family and style
            if (fontType.length() != 0 && fontStyle.length() != 0) {
                currShape.setFontStyle(fontType, fontStyle);
            }

            // Set font color
            currShape.setFontColor(red, green, blue);

            if (visibleCheck.isChecked()) {
                currShape.setVisible();
            } else {
                currShape.setInvisible();
            }

            if (movableCheck.isChecked()) {
                currShape.setMovable();
            } else {
                currShape.setUnMovable();
            }

            if (animateCheck.isChecked()) {
                currShape.enableAnimation();
            } else {
                currShape.unableAnimation();
            }

            // undo
            setEditView(currShape);

            actionStack.push("add");
            addedShapeStack.push(currShape);
            Toast.makeText(this, "Added " + currShape.getShapeName() + " successfully!", Toast.LENGTH_SHORT).show();
        }

        saveGame(game);
        editShapeView.invalidate();
    }

    // Update a shape according to all editViews
    public void onUpdate(View view) {
        Shape selectedShape = game.getCurrentPage().getSelectedShape();
        if (selectedShape == null) {
            Toast.makeText(this, "Please select a shape to update.", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText nameEdit = (EditText) findViewById(R.id.shapeName);
        if (nameEdit.getText().toString().length() == 0) {
            Toast.makeText(this, "Name cannot be empty when you update!", Toast.LENGTH_SHORT).show();
            return;
        } else if (sameNameDiffShapeExist(nameEdit.getText().toString(), selectedShape)) {
            Toast.makeText(this, "Name already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        // undo
        beforeEditedShapeStack.push(selectedShape);
        Shape newShape = new Shape(selectedShape);

        game.getCurrentPage().removeShape(selectedShape);
        game.getCurrentPage().addShape(newShape);

        // Update Column 1: x, y, width, height
        EditText xEdit = (EditText) findViewById(R.id.x);
        EditText yEdit = (EditText) findViewById(R.id.y);
        EditText widthEdit = (EditText) findViewById(R.id.width);
        EditText heightEdit = (EditText) findViewById(R.id.height);
        float newX = xEdit.getText().toString().length() == 0 ? newShape.getX() :
                    Float.parseFloat(xEdit.getText().toString());
        float newY = yEdit.getText().toString().length() == 0 ? newShape.getY() :
                    Float.parseFloat(yEdit.getText().toString());
        float newWidth = widthEdit.getText().toString().length() == 0 ? newShape.getWidth() :
                    Float.parseFloat(widthEdit.getText().toString());
        float newHeight = heightEdit.getText().toString().length() == 0 ? newShape.getHeight() :
                    Float.parseFloat(heightEdit.getText().toString());
        newShape.resize(newX, newY, newWidth, newHeight);

        // Update Colum 2: color and border line width
        red = ((SeekBar) findViewById(R.id.red)).getProgress();
        green = ((SeekBar) findViewById(R.id.green)).getProgress();
        blue = ((SeekBar) findViewById(R.id.blue)).getProgress();
        newShape.setFontColor(red, green, blue);
        EditText lineWidthEdit = (EditText) findViewById(R.id.lineWidth);
        if (lineWidthEdit.getText().toString().length() > 0) {
            newShape.setOutlineWidth(Float.parseFloat(lineWidthEdit.getText().toString()));
        }
        // Update Colum 3: text content, font, fontSize, fontStyle
        EditText textContentEdit = (EditText) findViewById(R.id.text);
        if (textContentEdit.getText().toString().length() > 0) {
            text = textContentEdit.getText().toString();
        } else {
            text = "";
        }
        newShape.setText(text);
        EditText fontSizeEdit = (EditText) findViewById(R.id.fontSize);
        if (fontSizeEdit.getText().toString().length() > 0) {
            newShape.setFontSize(Float.parseFloat(fontSizeEdit.getText().toString()));
        }
        String newFontType = fontNameSpinner.getSelectedItem().toString();
        String newFontStyle = fontStyleSpinner.getSelectedItem().toString();
        if (newFontType.length() > 0 && newFontStyle.length() > 0) {
            newShape.setFontStyle(newFontType, newFontStyle);
        }

        // Update Column 4: shapeName, visible, movable, animation
        String preName = selectedShape.getShapeName();
        String newName = nameEdit.getText().toString();
        scriptReferenceCheck(preName, newName);
        newShape.setShapeName(newName);

        CheckBox visibleCheck = (CheckBox) findViewById(R.id.visible);
        if (visibleCheck.isChecked()) {
            newShape.setVisible();
        } else {
            newShape.setInvisible();
        }
        CheckBox movableCheck = (CheckBox) findViewById(R.id.movable);
        if (movableCheck.isChecked()) {
            newShape.setMovable();
        } else {
            newShape.setUnMovable();
        }

        CheckBox animateCheck = (CheckBox) findViewById(R.id.animate);

        if (animateCheck.isChecked()) {
            newShape.enableAnimation();
        } else {
            newShape.unableAnimation();
        }

        // Update Column 5: shapeType
        String newType = shapeSpinner.getSelectedItem().toString();
        if (!newType.equals("box") && !newType.equals("text")) {
            newShape.setImageName(newType);
        } else {
            newShape.setImageName("");
        }

        game.getCurrentPage().setSelectedShape(newShape);

        // undo
        actionStack.push("edit");
        afterEditedShapeStack.push(newShape);

        Toast.makeText(this, "Update " + newShape.getShapeName() + " successfully!", Toast.LENGTH_SHORT).show();
        saveGame(game);
        editShapeView.invalidate();
    }

    // Error Checking: change all relevant scripts when you change the shapeName
    private void scriptReferenceCheck(String preName, String newName) {
        if (!newName.equals(preName)) {
            for (Page page : game.getPageList()) {
                for (Shape shape : page.getShapeList()) {
                    List<String> temp = new ArrayList<>();
                    for (String script : shape.getAllScripts()) {
                        temp.add(script.replace(preName, newName));
                    }
                    shape.setAllScripts(temp);

                    // on click
                    for (GameView.Action action : shape.getOnClickActions()) {
                        if (action.getModifier().equals(preName)) {
                            action.setModifier(newName);
                        }
                    }

                    // on enter
                    for (GameView.Action action : shape.getOnEnterActions()) {
                        if (action.getModifier().equals(preName)) {
                            action.setModifier(newName);
                        }
                    }

                    // on drop
                    if (shape.getOnDropActions().containsKey(preName)) {
                        List<GameView.Action> preNameActions = shape.getOnDropActions().get(preName);
                        shape.getOnDropActions().remove(preName);
                        shape.getOnDropActions().put(newName, preNameActions);
                    }
                    for (String droby : shape.getOnDropActions().keySet()) {
                        for (GameView.Action action : shape.getOnDropActions().get(droby)) {
                            if (action.getModifier().equals(preName)) {
                                action.setModifier(newName);
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete the selected shape
    public void onDelete(View view) {
        Shape seletedShape = game.getCurrentPage().getSelectedShape();
        if (seletedShape != null) {
            game.getCurrentPage().removeShape(seletedShape);

            // undo stacks
            actionStack.push("delete");
            deletedShapeStack.push(seletedShape);
        }

        game.getCurrentPage().unSetSelectedShape();
        saveGame(game);
        editShapeView.invalidate();
        resetInput();

        Toast.makeText(this,"Deleted successfully. \nCheck relevant scripts by yourself!",
                Toast.LENGTH_LONG).show();
    }

    public void onUndo(View view) {
        if (actionStack.isEmpty()) {
            Toast.makeText(this,"But you have done nothing...",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        game.getCurrentPage().unSetSelectedShape();
        switch (actionStack.pop()) {
            case "add":
                game.getCurrentPage().removeShape(addedShapeStack.peek());
                game.getCurrentPage().setSelectedShape(addedShapeStack.pop());
                break;
            case "delete":
                game.getCurrentPage().addShape(deletedShapeStack.peek());
                game.getCurrentPage().setSelectedShape(deletedShapeStack.pop());
                break;
            case "edit":
                String preName = afterEditedShapeStack.peek().getShapeName();
                String newName = beforeEditedShapeStack.peek().getShapeName();
                game.getCurrentPage().removeShape(afterEditedShapeStack.pop());
                game.getCurrentPage().addShape(beforeEditedShapeStack.peek());
                game.getCurrentPage().setSelectedShape(beforeEditedShapeStack.peek());

                scriptReferenceCheck(preName, newName);

                break;
            case "move":
                game.getCurrentPage().removeShape(afterMoveShapeStack.pop());
                game.getCurrentPage().addShape(beforeMoveShapeStack.peek());
                game.getCurrentPage().setSelectedShape(beforeMoveShapeStack.pop());
                break;
        }

        saveGame(game);
        editShapeView.invalidate();
        resetInput();
    }

    public void onBack(View view) {
        saveGame(game);
        Toast.makeText(this,"Game save.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PageManagementActivity.class);
        startActivity(intent);
    }


    public void onScript(View view) {
        saveGame(game);
        if (game.getCurrentPage().getSelectedShape() == null) {
            Toast.makeText(this,"Please select a shape.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EditScriptActivity.class);
        startActivity(intent);
    }

    public void onCopy(View view) {
        Shape selectedShape = game.getCurrentPage().getSelectedShape();
        if (selectedShape != null) {
            game.setCopyShape(selectedShape);
        }
    }

    public void onPaste(View view) {
        Shape copyShape = game.getCopyShape();
        if (copyShape == null) {
            Toast.makeText(this,"Copy a shape first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String copyShapeName = copyShape.getShapeName();
        while (sameNameExists(copyShapeName)) {
            copyShapeName = "copy_" + copyShapeName;
        }
        copyShape.setShapeName(copyShapeName);
        game.getCurrentPage().setSelectedShape(copyShape);
        game.getCurrentPage().addShape(copyShape);
        setEditView(copyShape);

        // undo
        actionStack.push("add");
        addedShapeStack.push(copyShape);

        saveGame(game);
        editShapeView.invalidate();
    }

    /* --------------------------------------------------------------------------------------------
     * ----------------------------------- Helper function ----------------------------------------
     * --------------------------------------------------------------------------------------------
     */

    // Set all EditView to the values of selected shape
    private void setEditView(Shape shape) {
        // Column 1
        EditText xEdit = (EditText) findViewById(R.id.x);
        xEdit.setText(Float.toString(shape.getX()));
        EditText yEdit = (EditText) findViewById(R.id.y);
        yEdit.setText(Float.toString(shape.getY()));
        EditText widthEdit = (EditText) findViewById(R.id.width);
        widthEdit.setText(Float.toString(shape.getWidth()));
        EditText heightEdit = (EditText) findViewById(R.id.height);
        heightEdit.setText(Float.toString(shape.getHeight()));

        // Column 2
        SeekBar redBar = (SeekBar) findViewById(R.id.red);
        redBar.setProgress(shape.getRed());
        SeekBar blueBar = (SeekBar) findViewById(R.id.blue);
        blueBar.setProgress(shape.getBlue());
        SeekBar greenBar = (SeekBar) findViewById(R.id.green);
        greenBar.setProgress(shape.getGreen());

        // Column 3
        EditText textContentEdit = (EditText) findViewById(R.id.text);
        textContentEdit.setText(shape.getText());
        EditText fontSizeEdit = (EditText) findViewById(R.id.fontSize);
        if (shape.getText().length() > 0) {
            fontSizeEdit.setText(Float.toString(shape.getTextSize()));
        } else {
            fontSizeEdit.setText("");
        }
        ArrayAdapter<String> fontAdapter = (ArrayAdapter<String>) fontNameSpinner.getAdapter();
        fontNameSpinner.setSelection(fontAdapter.getPosition(shape.getFontType()));

        // Column 4
        EditText nameEdit = (EditText) findViewById(R.id.shapeName);
        nameEdit.setText(shape.getShapeName());
        CheckBox visibleCheck = (CheckBox) findViewById(R.id.visible);
        visibleCheck.setChecked(shape.getVisible());
        CheckBox movableCheck = (CheckBox) findViewById(R.id.movable);
        movableCheck.setChecked(shape.getMovable());
        // TODO: Animation property in Shape.class
        //  CheckBox animationCheck = (CheckBox) findViewById(R.id.animate);

        // Column 5
        Spinner shapeSpinner = (Spinner) findViewById(R.id.shapeSpinner);
        ArrayAdapter<String> shapeAdapter = (ArrayAdapter<String>) shapeSpinner.getAdapter();
        shapeSpinner.setSelection(spAdapter.getPosition(game.getCurrentPage().getSelectedShape().getShapeName()));
    }

    // Check if the name already exist in the game (including all pages)
    private boolean sameNameExists(String curName) {
        for (Page page : game.getPageList()) {
            for (Shape shape : page.getShapeList()) {
                if (shape.getShapeName().equals(curName)) return true;
            }
        }
        return false;
    }

    // Check if the name already exist in a different shape in the game (including all pages)
    private boolean sameNameDiffShapeExist(String curName, Shape curShape) {
        for (Page page : game.getPageList()) {
            for (Shape shape : page.getShapeList()) {
                if (shape.getShapeName().equals(curName) && shape != curShape) return true;
            }
        }
        return false;
    }

    // Reset all EditViews and CheckBox to default values
    private void resetInput() {
        EditText xEdit = (EditText) findViewById(R.id.x);
        EditText yEdit = (EditText) findViewById(R.id.y);
        EditText widthEdit = (EditText) findViewById(R.id.width);
        EditText heightEdit = (EditText) findViewById(R.id.height);
        EditText textEdit = (EditText) findViewById(R.id.text);
        EditText fontSizeEdit = (EditText) findViewById(R.id.fontSize);
        EditText nameEdit = (EditText) findViewById(R.id.shapeName);
        CheckBox visibleCheck = (CheckBox) findViewById(R.id.visible);
        CheckBox movableCheck = (CheckBox) findViewById(R.id.movable);
        CheckBox animationCheck = (CheckBox) findViewById(R.id.animate);

        xEdit.setText("10.0");
        yEdit.setText("10.0");
        widthEdit.setText("200");
        heightEdit.setText("200");
        textEdit.setText("");
        fontSizeEdit.setText("");
        nameEdit.setText("");
        visibleCheck.setChecked(true);
        movableCheck.setChecked(false);
        animationCheck.setChecked(false);
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

    public static void clearUndoStacks() {
        // Empty the stacks
        actionStack = new Stack<>();
        addedShapeStack = new Stack<>();
        deletedShapeStack = new Stack<>();
        beforeEditedShapeStack = new Stack<>();
        afterEditedShapeStack = new Stack<>();
        beforeMoveShapeStack = new Stack<>();
        afterMoveShapeStack = new Stack<>();
    }
}