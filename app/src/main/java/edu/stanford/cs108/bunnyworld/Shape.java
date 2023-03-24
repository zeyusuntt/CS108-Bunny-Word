package edu.stanford.cs108.bunnyworld;

import static android.graphics.Color.rgb;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Shape implements Serializable {
    private Page pageBelong;
    private float x, y;
    private float width, height;
    //animation area
    private float animationLeft, animationTop;
    private static int id = 0;
    private String shapeName, text = "", imageName = "";
    private boolean isSelected = true, isVisible = true, isMovable = true, isAnimation = false;
    private boolean isInPossession = false;
    private transient Canvas canvas;
    private transient BitmapDrawable bitmapDrawable;
    private transient Paint boxOutlinePaint;    // draw shape bounding rect
    private transient Paint selectedOutlinePaint;
    private transient Paint transparentPaint;
    private float backgroundBoundary = 5;
    private int inventorySize = 85;

    private transient Paint textPaint;
    private transient Rect textBounds;
    private float outlineWidth = 5.0f;

    /* Set text style */
    private String fontType = "sans-serif-medium";
    private float textSize = 75.0f;
    private Boolean isBold = false;
    private Boolean isItalic = false;

    // text color
    private int textColor = Color.BLACK;
    private int red = 0;
    private int blue = 0;
    private int green = 0;

    private transient Paint.FontMetrics fontMetrics;

    private List<String> allScripts = new ArrayList<>();
    private List<GameView.Action> onClickActions = new ArrayList<>();
    private List<GameView.Action> onEnterActions = new ArrayList<>();
    private HashMap<String, List<GameView.Action>> onDropActions = new HashMap<>();



    /* Constructor */
    public Shape(float x, float y, float width, float height, String shapeName, String text,
                 boolean isSelected, Page pageBelong) {
        id++;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.animationLeft = x;
        this.animationTop = y;

        this.shapeName = shapeName.length() > 0 ? shapeName : "shape" + Integer.toString(id);
        this.text = text;
        this.isSelected = isSelected;
        this.pageBelong = pageBelong;
    }

    public Shape() {
        this.shapeName = shapeName != null ? shapeName : "shape" + Integer.toString(id);
        id++;
    }

    public Shape(float x, float y, float width, float height, String shapeName, Page pageBelong) {
        this(x, y, width, height, shapeName, "", false, pageBelong);
    }

    public Shape(Shape shape) {
        this.x = shape.getX();
        this.y = shape.getY();
        this.width = shape.getWidth();
        this.height = shape.getHeight();
        this.shapeName = shape.getShapeName();
        this.text = shape.getText();
        this.imageName = shape.getImageName();
        this.allScripts = new ArrayList<>(shape.getAllScripts());
        this.onClickActions = new ArrayList<>(shape.getOnClickActions());
        this.onEnterActions = new ArrayList<>(shape.getOnEnterActions());
        this.onDropActions = new HashMap<>(shape.getOnDropActions());
        this.pageBelong = shape.getPageBelong();

        this.textColor = shape.getTextColor();
        this.red = shape.getRed();
        this.blue = shape.getBlue();
        this.green = shape.getGreen();
        this.isInPossession = shape.isInPossession;
        this.isMovable = shape.getMovable();
        this.isSelected = shape.getSelected();
        this.fontType = shape.getFontType();
        this.textSize = shape.getTextSize();
        this.isBold = shape.getIsBold();
        this.isItalic = shape.getIsItalic();
        this.isAnimation = shape.getAnimation();
        this.outlineWidth = shape.getOutlineWidth();
        this.isVisible = shape.getVisible();
    }

    /*Initialize needed Paint*/
    private void initPaints() {
        this.boxOutlinePaint = new Paint();
        this.selectedOutlinePaint = new Paint();
        this.transparentPaint = new Paint();
        // text properties will be set in draw()
        this.textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.LEFT);
        fontMetrics = textPaint.getFontMetrics();

        this.textBounds = new Rect();

        selectedOutlinePaint.setStyle(Paint.Style.STROKE);
        selectedOutlinePaint.setColor(Color.BLUE);
        selectedOutlinePaint.setStrokeWidth(15.0f);

        boxOutlinePaint.setStyle(Paint.Style.STROKE);
        boxOutlinePaint.setColor(Color.LTGRAY);
        boxOutlinePaint.setStrokeWidth(5.0f);

        transparentPaint.setStyle(Paint.Style.FILL);
        transparentPaint.setColor(Color.YELLOW);
        transparentPaint.setAlpha(160);
    }

    /* Get Shape parameters */
    public float getX(){return x;}
    public float getY(){return y;}
    public float getWidth(){return width;}
    public float getHeight(){return height;}
    public String getText(){return text;}
    public String getShapeName() {return shapeName;}
    public float getInventoryShapeSize() {
        return inventorySize;
    }

    public String getImageName(){
        return this.imageName;
    }
    public Page getPageBelong() {return pageBelong;}
    public int getRed() {
        return red;
    }
    public int getBlue() {
        return blue;
    }
    public int getGreen() {
        return green;
    }


    /* Set Shape parameters */
    public void setName(String name) {
        this.shapeName = name;
    }
    public void setX(float x){this.x = x;}
    public void setY(float y){this.y = y;}
    public void setWidth(float width){this.width = width;}
    public void setHeight(float height){this.height = height;}
    public void updatePosition(float x, float y){
        this.x = x;
        this.y = y;
    }
    public void setText(String text){this.text = text;}
    public void setShapeName(String shapeName) {this.shapeName = shapeName;}
    public void setImageName(String imgName){
        this.imageName = imgName;
    }

    public void setOutlineWidth(float lineWidth) {
        this.outlineWidth = lineWidth;
    }

    public float getOutlineWidth() {
        return this.outlineWidth;
    }

    // Edit font settings
    public void setFontSize(float textSize) {
        this.textSize = textSize;
    }

    public void setFontStyle( String fontType, String fontStyle) {
        this.fontType = fontType;
        switch (fontStyle) {
            case "BOLD":
                this.isBold = true;
                break;
            case "ITALIC":
                this.isItalic = true;
                break;
            case "NORMAL":
                break;
        }
    }

    public void setFontColor(int red, int green, int blue) {
        this.textColor = Color.rgb(red, green, blue);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /* Get text style */
    public String getFontType() {return fontType;}

    public float getTextSize() {return textSize;}
    public Boolean getIsBold() {return isBold;}
    public Boolean getIsItalic() {return isItalic;}
    public int getTextColor(){return textColor;}


    // Visible Check
    public boolean getVisible(){return isVisible;}
    public void setVisible(){this.isVisible = true;}
    public void setInvisible() {this.isVisible = false;}

    // Movable Check
    public boolean getMovable(){return isMovable;}
    public void setMovable(){this.isMovable = true;}
    public void setUnMovable() {this.isMovable = false;}

    // Animation Check
    public boolean getAnimation(){return isAnimation;}
    public void enableAnimation(){this.isAnimation = true;}
    public void unableAnimation() {this.isAnimation = false;}

    // Selection Check
    public boolean getSelected() {return isSelected;};
    public void setSelected(boolean selected) {this.isSelected = selected;}

    // Containing Check
    public boolean containing(float x, float y){
        if (isInPossession) {
            return x >= this.x & x <= this.x + inventorySize & y >= this.y & y <= this.y + inventorySize;
        }
        return x >= this.x & x <= this.x + width & y >= this.y & y <= this.y + height;
    }

    // In Possession Check
    public boolean isInPossession(){
        return this.isInPossession;
    }
    public void setInPossession(boolean bool){
        this.isInPossession = bool;
    }

    public void setBitmapDrawable(BitmapDrawable drawable) {
        this.bitmapDrawable = drawable;
    }

    public void applyTextSettings(){
        if (text.length() == 0) return;
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        textPaint.setColor(textColor);
        int style = Typeface.NORMAL;
        if (getIsBold() && getIsItalic()) style = Typeface.BOLD_ITALIC;
        else if (getIsBold()) style = Typeface.BOLD;
        else if (getIsItalic()) style = Typeface.ITALIC;
        textPaint.setTypeface(Typeface.create(fontType, style));
        fontMetrics = textPaint.getFontMetrics();
        setWidth(textBounds.width());
        setHeight(fontMetrics.descent - fontMetrics.ascent);
    }

    private void updatePaints() {
        boxOutlinePaint.setStyle(Paint.Style.STROKE);
        boxOutlinePaint.setColor(Color.rgb(red, green, blue));
        boxOutlinePaint.setStrokeWidth(outlineWidth);
    }

    /*Calculate the animation area*/
    private void updateAnimationSize() {
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        float ani_width = width * (Game.isGameMode() ? GameView.size : EditShapeView.size);
        float ani_height = height * (Game.isGameMode() ? GameView.size : EditShapeView.size);
        animationLeft = centerX - ani_width / 2;
        animationTop = centerY - ani_height / 2;
    }

    /*Shape draw itself based on the its states*/
    public void draw(Canvas canvas){
        this.canvas = canvas;
        initPaints();
        updatePaints();
        // set drawable
        if (imageName.length() > 0) {
            if (Game.isGameMode()) {
                GameView.initBitmap(this, GameView.getViewContext());
            } else {
                EditShapeView.initBitmap(this, EditShapeView.getViewContext());
            }
        }
        // set animation size
        if (isAnimation) {
            updateAnimationSize();
        }
        // not in play game mode
        if (!Game.isGameMode()) {
            if (text.length() > 0) {
                applyTextSettings();
                canvas.drawText(text, x - textBounds.left, y - fontMetrics.ascent, textPaint);
            } else if (imageName.length() > 0) {
                Bitmap toDraw = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),
                        (int) width, (int) height, true);
                if (isAnimation) {
                    canvas.drawBitmap(toDraw, animationLeft, animationTop, null);
                } else {
                    canvas.drawBitmap(toDraw, x, y, null);
                }
            } else {
                canvas.drawRect(x, y, x + width, y + height, boxOutlinePaint);
            }

            if(!isVisible){
                canvas.drawRect(x, y, x + width, y + height, transparentPaint);
            }
            if(isSelected){
                canvas.drawRect(x, y, x + width, y + height, selectedOutlinePaint);
            }
            return;
        }

        if (!isVisible) return;
        // when in play game mode
        if (text.length() > 0) {
            if (isInPossession) {
                textPaint.setColor(Color.WHITE);
                canvas.drawText("T", x - textBounds.left, y - fontMetrics.ascent, textPaint);
            } else {
                canvas.drawText(text, x - textBounds.left, y - fontMetrics.ascent, textPaint);
            }

        } else if (imageName.length() > 0) {
            Bitmap toDraw;
            if (isInPossession) {
                toDraw = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),
                        inventorySize, inventorySize, true);
                canvas.drawBitmap(toDraw, x, y, null);
            } else {
                toDraw = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),
                        (int) width, (int) height, true);
                if (isAnimation) {
                    canvas.drawBitmap(toDraw, animationLeft, animationTop, null);
                } else {
                    canvas.drawBitmap(toDraw, x, y, null);
                }
            }
        } else {
            Paint rectPaint = isSelected ? selectedOutlinePaint : boxOutlinePaint;
            if (isInPossession) {
                canvas.drawRect(x - backgroundBoundary, y - backgroundBoundary,
                        x + inventorySize, y + inventorySize, rectPaint);
            } else {
                canvas.drawRect(x, y, x + width, y + height, rectPaint);
            }

        }
    }

    /*Draw its border*/
    public void drawBorder(){
        if(Game.isGameMode()) {
            canvas.drawRect(x, y, x + width, y + height, boxOutlinePaint);
        }
    }

    public void resize(float newX, float newY, float newWidth, float newHeight) {
        this.x = newX;
        this.y = newY;
        this.width = newWidth;
        this.height = newHeight;
    }

    /*
     * Action and Script part
     * Should create allScript, onDropScript, onClickScript, onEnterScript, and
     * */
    /*get all script, input is only the script for one trigger*/
    public void getAllScript(String script) {
        if (getTriggerScript(script, "on click")) {
            allScripts.add(script);
        }
        else if (getTriggerScript(script, "on enter")) {
            allScripts.add(script);
        }
        else if (getTriggerScript(script, "on drop")) {
            allScripts.add(script);
        }
    }

    public String scriptToString(){
        String scriptText = "";
        StringBuilder onClickb = new StringBuilder();
        for(GameView.Action action : getOnClickActions())
        {
            onClickb.append(action);
            onClickb.append(" ");
        }

        Set<String> uniqueEnter = new HashSet<>();
        for(GameView.Action action : getOnEnterActions())
        {
            uniqueEnter.add(action.toString());
        }
        StringBuilder onEnterb = new StringBuilder();
        for(String action : uniqueEnter)
        {
            onEnterb.append(action);
            onEnterb.append(" ");
        }
        StringBuilder onDropb = new StringBuilder();
        for (String key : getOnDropActions().keySet()) {
            StringBuilder temp = new StringBuilder();
            temp.append(key);
            temp.append(" ");
            for (GameView.Action item : getOnDropActions().get(key)){
                temp.append(item);
                temp.append(" ");
            }
            temp.append("; ");
            onDropb.append("on drop ");
            onDropb.append(temp);
        }
        String onClick = onClickb.toString();
        String onEnter = onEnterb.toString();
        String onDrop = onDropb.toString();

        if (onClick != "") scriptText += "on click "+ onClick + "; ";
        if (onEnter != "") scriptText += "on enter "+ onEnter + "; ";
        if (onDrop != "") scriptText += onDrop;
        return scriptText;
    }

    /*get scripts for three triggers.*/
    private boolean getTriggerScript(String script, String trigger) {
        if (!script.startsWith(trigger)) return false;
        // first get the actions for this trigger
        String[] actions = script.substring(trigger.length()+1, script.length()-1).split(" ");


        if (trigger.equals("on click")) {
            if (onClickActions.isEmpty()) {
                addActions(actions, onClickActions);
            }
        }

        if (trigger.equals("on enter")) {
            // if (!onEnterActions.isEmpty()) return false;
            addActions(actions,onEnterActions);
        }

        if (trigger.equals("on drop")) {
            addActions(actions, onDropActions);
        }
        return true;
    }

    // helper func for onClick and onEnter
    private void addActions(String[] actions, List<GameView.Action> list) {
        for (int i = 0; i < actions.length/2; i++) {
            list.add(new GameView.Action(actions[2*i], actions[2*i+1]));
        }
    }

    //helper func for OnDrop
    private void addActions(String[] actions, HashMap<String, List<GameView.Action>> map) {
        if (!map.containsKey(actions[0])) {
            map.put(actions[0], new ArrayList<>());
        }
        for (int i = 1; i <= actions.length/2; i++) {
            map.get(actions[0]).add(new GameView.Action(actions[2*i-1], actions[2*i]));
        }
    }

    // Set the onClickActions based on script
    public void setFirstOnClick(String script) {
        onClickActions.clear();
        String[] actions = script.substring("on click".length()+1, script.length()-1).split(" ");
        getTriggerScript(script, "on click");
    }

    /*clear the script and the corresponding actions*/
    public void clearScript(String script) {
        allScripts.remove(script);

        if (script.startsWith("on click")) {
            onClickActions.clear();
            for (String s : allScripts) {
                if (s.startsWith("on click")) {
                    String[] actions = s.substring("on click".length()+1, s.length()-1).split(" ");
                    addActions(actions, onClickActions);
                    // Only execute the first one!
                    break;
                }
            }
        }
        else if (script.startsWith("on enter")) {
            onEnterActions.clear();
            for (String s : allScripts) {
                if (s.startsWith("on enter")) {
                    String[] actions = s.substring("on enter".length()+1, s.length()-1).split(" ");
                    addActions(actions, onEnterActions);
                }
            }
        }
        else if (script.startsWith("on drop")) {
            String[] actions = script.substring("on drop".length()+1, script.length()-1).split(" ");
            if (!onDropActions.containsKey(actions[0])) return;

            String dropBy = actions[0];
            onDropActions.remove(actions[0]);
            for (String s : allScripts) {
                if (s.startsWith("on drop " + dropBy)) {
                    actions = s.substring("on drop".length()+1, s.length()-1).split(" ");
                    addActions(actions, onDropActions);
                }
            }

        }
    }

    // Discard: clear onClickActions or onEnterActions if necessary
    public void prepareToAddScript(String script) {
        allScripts.remove(script);

        List<String> list = new ArrayList<>(allScripts);
        if (script.startsWith("on click")) {
            if (onClickActions.size() > 0) {
                onClickActions.clear();
                for (String s : list) {
                    if (s.startsWith("on click")) allScripts.remove(s);
                }
            }
        }
    }

    public void onClick() {
        if (onClickActions.isEmpty()) return;
        for (int i = 0; i < onClickActions.size(); i++) {
            onClickActions.get(i).scriptActions(this);
        }
    }
    public void onEnter() {
        for (GameView.Action action: onEnterActions) {
            action.scriptActions(this);
        }
    }
    public void onDrop(String shapeName) {
        for (GameView.Action action: onDropActions.get(shapeName)) {
            action.scriptActions(this);
        }
    }

    public boolean isClickable() {return !onClickActions.isEmpty();}
    public boolean isEnterable() {return !onEnterActions.isEmpty();}
    public boolean isDroppable(String shapeName) {return onDropActions.containsKey(shapeName);};

    public List<GameView.Action> getOnClickActions() {return onClickActions;}
    public List<GameView.Action> getOnEnterActions() {return onEnterActions;}
    public HashMap<String, List<GameView.Action>> getOnDropActions() {return onDropActions;}
    public List<String> getAllScripts() {return allScripts;}
    public void setAllScripts(List<String> list) {
        allScripts = list;
    }
}
