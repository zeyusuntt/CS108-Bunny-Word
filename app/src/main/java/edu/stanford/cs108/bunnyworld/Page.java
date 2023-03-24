package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.util.Log;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Page implements Serializable {
    private String pageName;
    boolean isCurPage;
    private List<Shape> shapeList;
    private Shape selectedShape;
    private Game game;
    private float possessionsBound;
    private Shape backgroundImage;
    private String BGM = "";

    private float viewHeight, viewWidth;
    private float possessionHorinzontalGap;
    private float possessionVerticalGap;
    private float possessionShapeSize;
    private float possessionLeftMargin;
    private float possessionTopMargin;
    private float centerline;

    private HashSet<Shape> enterSet = new HashSet<>();
    private HashSet<Shape> dropSet = new HashSet<>();


    /* Constructor */
    public Page(String pageName, Game game) {
        this.pageName = pageName;
        this.shapeList = new ArrayList<>();
        this.game = game;
        this.backgroundImage = null;
    }

    public Page(String pageName) {
        this(pageName, null);
    }

    // Add shape to possessions
    public void addToPossessions(Shape shape) {
        Game.getCurrGame().possessions.add(shape);
        shape.setInPossession(true);
    }

    // Remove shape from possessions
    public void removeFromPossessions(Shape shape) {
        Game.getCurrGame().possessions.remove(shape);
    }

    // Add shape to shape list
    public void addShape(Shape shape) {
        shapeList.add(shape);
        shape.setInPossession(false);
    }

    // Remove shape from shape list
    public void removeShape(Shape shape) {
        List<Shape> tempList = new ArrayList<>();
        for (Shape s : shapeList) {
            if (s.getShapeName() != shape.getShapeName()) {
                tempList.add(s);
            }
        }
        this.shapeList = tempList;
    }

    // Draw background image, shapeList, possessions
    public void draw(Canvas canvas){
        if (this.backgroundImage != null){
            backgroundImage.draw(canvas);
        }
        for (Shape sh : shapeList){
            sh.draw(canvas);
        }
        for (Shape p : Game.getCurrGame().possessions){
            p.draw(canvas);
        }
    }

    // Trigger all on enter
    public void enter(){
        for (Shape sh : shapeList){
            if (sh.isEnterable()){
                sh.onEnter();
                enterSet.add(sh);
            }
        }
    }

    public Set<Shape> getEnterSet() {
        return enterSet;
    }


    // Set selectedShape
    public void setSelectedShape(Shape selectedShape) {
        unSetSelectedShape();
        selectedShape.setSelected(true);
        this.selectedShape = selectedShape;
    }

    // Unset selectedShape
    public void unSetSelectedShape() {
        selectedShape = null;
        for (Shape s : shapeList) {
            s.setSelected(false);
        }
    }

    // Check if shape is selected, return shape if founded or null
    public Shape shapeSelected(float x, float y){
        for (Shape sh : shapeList){
            // leave background image not selectable
            if (sh.getImageName().contains("_bg")) {
                continue;
            }
            // other shapes
            if (sh.containing(x, y)){
                unSetSelectedShape();
                setSelectedShape(sh);
                return sh;
            }
        }
        for (Shape p : Game.getCurrGame().possessions){
            if (p.containing(x, y)){
                unSetSelectedShape();
                setSelectedShape(p);
                return p;
            }
        }
        unSetSelectedShape();
        return null;
    }

    /*  Given the newX and newY of current shape,
     *  determine if the shape is moved from the possession to the game area
     *  or from the game area to the possession
     *  Update shapeList and possessions if necessary.
     *  Update shape position.
     */
    private static Map<Shape, Integer> shapeToIndex = new HashMap<>();
    private static Queue<Integer> availbleIndex = new PriorityQueue<>();
    public static int index;
    private float topX, topY;

    public void setIndex(int i) {
        index = i;
    }

    public int getPossessionSize() {
        return shapeToIndex.size();
    }

    public void updateCurShape(float newX, float newY) {
        // Corner case
        if (selectedShape == null || !selectedShape.getMovable()) {
            return;
        }

        // Update shapeList and possessions if necessary
        boolean newInPossessions = isInPossessions(newY, selectedShape.getHeight());
        if (!selectedShape.isInPossession() && newInPossessions) {
            // Case 1: move from game area to possessions area
            shapeList.remove(selectedShape);
            Game.getCurrGame().possessions.add(selectedShape);
            selectedShape.setInPossession(true);

            if (availbleIndex.isEmpty()) {
                index = Game.getCurrGame().possessions.size();
            } else {
                index = availbleIndex.poll();
            }
            shapeToIndex.put(selectedShape, index);

        } else if (selectedShape.isInPossession() && !newInPossessions) {
            // Case 2: move from possessions area to game area
            shapeList.add(selectedShape);
            Game.getCurrGame().possessions.remove(selectedShape);
            selectedShape.setInPossession(false);

            // TODO: Can we remove "if" here?
            if (shapeToIndex.containsKey(selectedShape)) {
                availbleIndex.offer(shapeToIndex.get(selectedShape));
                shapeToIndex.remove(selectedShape);
            }
        }

        // Update shape position
        if (selectedShape.isInPossession()) {
            // Case 1: In possession
            if (shapeToIndex.containsKey(selectedShape)) {
                int curIndex = shapeToIndex.get(selectedShape);
                int col = (int)Math.ceil((double) curIndex / 2);
                assignValues();
                topX = (float) (possessionLeftMargin + (possessionHorinzontalGap + possessionShapeSize) * (col - 1));
                topY = curIndex % 2 == 0 ? centerline + possessionVerticalGap:
                        centerline - possessionShapeSize - possessionVerticalGap;
                selectedShape.updatePosition(topX, topY);
            }
        } else {
            // Case 2: In game area
            selectedShape.updatePosition(newX, newY);
        }
    }

    private void assignValues() {
        centerline = viewHeight * 0.85f;
        possessionHorinzontalGap = viewWidth * 0.01f;
        possessionVerticalGap = viewHeight * 0.002f;
        possessionShapeSize = viewWidth * 0.055f;
        possessionLeftMargin = viewWidth * 0.040f;
        possessionTopMargin = viewHeight * 0.030f;
    }

    // Return true if the shape with newX, newY is in possesions area
    public boolean isInPossessions(float newY, float height) {
        return newY + height / 2 >= possessionsBound;
    }

    // Update the selectedShape position to (newX, newY)
    public void update(float newX, float newY) {
        if (selectedShape != null) {
            selectedShape.updatePosition(newX, newY);
        }
    }

    // When cur is overlap with pre, trigger the onDrop() event or do nothing
    public void overlap(Shape pre, Shape cur, boolean inPossession, float x, float y) {
        if (Game.isGameMode()
            && !inPossession && pre.isDroppable(cur.getShapeName())) {
            pre.onDrop(cur.getShapeName());
            dropSet.add(pre);
            return;
        }
        cur.updatePosition(x, y);
    }

    public HashSet<Shape> getDropSet(){
        return dropSet;
    }

    /* Set functions */
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public void setCurPage(boolean curPage) {
        isCurPage = curPage;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setPossessionsBound(float possessionsBound) {
        this.possessionsBound = possessionsBound;
    }

    public void setViewHeight(float viewHeight) {
        this.viewHeight = viewHeight;
    }

    public void setViewWidth(float viewWidth) {
        this.viewWidth = viewWidth;
    }

    // Extension: set background image
    public void setBackgroundImage(Shape backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    // Extension: set BGM
    public void setBGM(String BGM) {
        this.BGM = BGM;
    }

    /* Get functions */
    public String getPageName() {
        return pageName;
    }

    public List<Shape> getShapeList() {
        return shapeList;
    }

    public List<Shape> getPossessions() {
        return Game.getCurrGame().possessions;
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public Game getParentGame() {
        return game;
    }

    public Shape getBackgroundImage() {
        return backgroundImage;
    }

    public String getBGM() {
        return BGM;
    }
}
