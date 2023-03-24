package edu.stanford.cs108.bunnyworld;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GameView extends View {
    private static Page currPage, prePage;
    private boolean changePage, isClick;
    private Paint possessionsLine;
    private static Game currGame;
    private static float possessionsBound;
    private int viewWidth, viewHeight;
    private static String gameName;
    private MediaPlayer mediaPlayer;
    private Shape clickedShape;
    private static Context context;
    private HashSet<Shape> enterSet = new HashSet<>();
    private HashSet<Shape> clickSet = new HashSet<>();
    private HashSet<Shape> dropSet = new HashSet<>();

    private HashSet<Page> pageSet = new HashSet<>();

    public static float size;

    private BitmapDrawable possessionDrawable, bagiconDrawble;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        // initialize animation
        startAnimator();
        GameView.context = getContext();
    }

    private void init(){
        possessionsLine = new Paint();
        possessionsLine.setColor(Color.BLACK);
        possessionsLine.setStyle(Paint.Style.STROKE);
        possessionsLine.setStrokeWidth(5.0f);

        // init possesion background image
        possessionDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.possessionbg);
        bagiconDrawble = (BitmapDrawable) getResources().getDrawable(R.drawable.bagicon);
    }

    public static void setGameName(String gameName) {
        GameView.gameName = gameName;
    }

    public static void setCurrGame(Game game) {
        currGame = game;
        currGame.setCurrentPage(currGame.getFirstPage());
        currPage = currGame.getCurrentPage();

    }

    public static Context getViewContext() {
        return context;
    }

    // Play BGM
    public void playBGM(String bgm){
        if (mediaPlayer != null) mediaPlayer.stop();
        if (bgm.equals("")) return;
        mediaPlayer = MediaPlayer.create(getContext(), getContext().getResources().getIdentifier(
                bgm, "raw", getContext().getPackageName()));
        mediaPlayer.start();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        currPage = currGame.getCurrentPage();
        // Draw the page again if change to a new page
        if (prePage == null || !prePage.getPageName().equalsIgnoreCase(currPage.getPageName())){
            if (prePage != null){
                changePage = true;
                clickedShape = null;
            }
            currPage.enter();
            enterSet.addAll(currPage.getEnterSet());
            TextView pointText = (TextView) ((Activity) getContext()).findViewById(R.id.points_textview);
            CharSequence newText = (CharSequence) (getPoints()+ "/" +getTotalPoints());
            pointText.setText(newText);
            if (!currGame.getCurrentPage().getPageName().equalsIgnoreCase(currPage.getPageName())){
                invalidate();
            }

            currPage.setPossessionsBound(possessionsBound);
            currPage.setViewHeight(viewHeight);
            currPage.setViewWidth(viewWidth);
            prePage = currPage;
            playBGM(currPage.getBGM());
        }

        // Draw possesion area
        canvas.drawLine(0.0f, possessionsBound, viewWidth, possessionsBound, possessionsLine);
        canvas.drawBitmap(possessionDrawable.getBitmap(),
                null, new RectF(0.0f, possessionsBound, 0.8f * viewWidth, viewHeight),
                null);
        canvas.drawBitmap(bagiconDrawble.getBitmap(),
                null, new RectF(0.8f * viewWidth, possessionsBound, viewWidth, viewHeight),
                null);

        // Draw green borders for shapes that has onDrop() methods for the selectedShape
        currPage.draw(canvas);

        if (clickedShape != null){
            for (Shape sh : currPage.getShapeList()){
                if (sh.isDroppable(clickedShape.getShapeName())){
                    sh.drawBorder();
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        possessionsBound = 0.7f * viewHeight;

        // System.out.println("GameMode: " + viewHeight);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility != View.VISIBLE){
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } else {
            playBGM(currPage.getBGM());
        }
        super.onWindowVisibilityChanged(visibility);
    }

    private Shape underShape;
    private boolean inShape = false;
    private float x1, y1, x2, y2;
    private float dx, dy;
    private long startTime = 0, endTime = 0;
    private float possessionEdgeWidth = 5;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                changePage = false;
                clickedShape = currPage.shapeSelected(x1, y1);
                startTime = System.currentTimeMillis();

                if (clickedShape != null && clickedShape.getVisible()) {
                    dx = x1 - clickedShape.getX();
                    dy = y1 - clickedShape.getY();
                    inShape = true;
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                x2 = event.getX();
                y2 = event.getY();
                if (!changePage && inShape && clickedShape.getVisible()
                        && clickedShape.getMovable()) {
                    currPage.update(x2 - dx, y2 - dy);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                endTime = System.currentTimeMillis();
                if ((endTime - startTime) > 200) {
                    isClick = false;
                } else {
                    changePage = false;
                    isClick = true;
                }

                if (clickedShape != null && inShape && !changePage) {
                    if (isClick) {
                        if (!clickedShape.isInPossession() && clickedShape.getVisible()) {
                            clickedShape.onClick();
                            clickSet.add(clickedShape);
                        }
                    } else {
                        boolean overlap = false;
                        float newX = x2 - dx;
                        float newY = y2 - dy;

                        if (!clickedShape.isInPossession() && (newX  + clickedShape.getWidth() / 2 < 0
                            || newY + clickedShape.getHeight() / 2 < 0
                            || newX + clickedShape.getWidth() / 2 > viewWidth
                            || newY + clickedShape.getHeight() / 2 > viewHeight)) {
                            // Case 1: out of the game area -> back to the original place
                            currPage.updateCurShape(x1 - dx, y1 - dy);
                        } else if (clickedShape.isInPossession() && (newX < 0 || newY < 0
                                    || newX + clickedShape.getInventoryShapeSize() / 2 > viewWidth
                                    || newY + clickedShape.getInventoryShapeSize() / 2 > viewHeight)) {
                            // Case 2: out of the possession area -> back to the original place
                            currPage.updateCurShape(x1 - dx, y1 - dy);
                        } else {
                            // System.out.println("BEFORE: shape number game area: " + currGame.getCurrentPage().getShapeList().size());
                            // System.out.println("BEFORE: shape number possession: " + currGame.getCurrentPage().getPossessions().size());

                            // Case 3: check if overlap with other shapes
                            float newRight = newX + clickedShape.getWidth();
                            float newBottom = newY + clickedShape.getHeight();

                            // Check if overlap with any shape in game area
                            List<Shape> tempList1 = new ArrayList<>(currPage.getShapeList());
                            for (Shape shape : tempList1) {
                                if (!shape.getShapeName().equals(clickedShape.getShapeName())
                                    && !isBackGround(shape)
                                    &&  !(newX > shape.getX() + shape.getWidth()
                                    || newY > shape.getY() + shape.getHeight()
                                    || newRight < shape.getX() || newBottom < shape.getY())) {
                                    overlap = true;
                                    underShape = shape;
                                    currPage.overlap(underShape, clickedShape, false, x1 - dx, y1 - dy);
                                }
                            }

                            // Check if overlap with any shape in possession area
                            List<Shape> tempList2 = new ArrayList<>(currPage.getPossessions());
                            for (Shape shape : tempList2) {
                                if (!shape.getShapeName().equals(clickedShape.getShapeName())
                                        && !isBackGround(shape)
                                        && !(newX > shape.getX() + shape.getInventoryShapeSize()
                                        || newY > shape.getY() + shape.getInventoryShapeSize()
                                        || newRight < shape.getX() || newBottom < shape.getY())) {
                                    overlap = true;
                                    underShape = shape;
                                    currPage.overlap(underShape, clickedShape, true, x1 - dx, y1 - dy);
                                }
                            }

//                            boolean inPossession = currPage.isInPossessions(newY, clickedShape.getHeight());
//                            if (inPossession && !clickedShape.isInPossession()) {
//                                currPage.getShapeList().remove(clickedShape);
//                                currPage.getPossessions().add(clickedShape);
//                            } else if (!inPossession && clickedShape.isInPossession()) {
//                                currPage.getShapeList().add(clickedShape);
//                                currPage.getPossessions().remove(clickedShape);
//                            }

                            boolean inPossession = currPage.isInPossessions(newY, clickedShape.getHeight());
                            if (inPossession) overlap = false;

                            if (!overlap) {
                                if (newY + clickedShape.getHeight() > possessionsBound
                                    && newY < possessionsBound) {
                                    if (newY + clickedShape.getHeight() / 2 < possessionsBound) {
                                        newY = possessionsBound - clickedShape.getHeight() - possessionEdgeWidth;
                                    } else {
                                        newY = possessionsBound + possessionEdgeWidth;
                                    }
                                }
                                if (currPage.getPossessionSize() > 21
                                        && inPossession && !clickedShape.isInPossession()) {
                                    currPage.updateCurShape(x1 - dx, y1 - dy);
                                    System.out.println("Possession is full....");
                                } else {
                                    currPage.updateCurShape(newX, newY);
                                }
                            }
                        }
                    }
                    invalidate();
                    dropSet.addAll(currPage.getDropSet());
                    TextView pointText = (TextView) ((Activity) getContext()).findViewById(R.id.points_textview);
                    CharSequence newText = (CharSequence) (getPoints()+ "/" +getTotalPoints());
                    pointText.setText(newText);
                }
                // System.out.println("shape number game area: " + currGame.getCurrentPage().getShapeList().size());
                // System.out.println("shape number possession: " + currGame.getCurrentPage().getPossessions().size());
                inShape = false;
                clickedShape = null;
        }
        return true;
    }

    private boolean isBackGround(Shape shape) {
        String name = shape.getImageName();
        int len = name.length();
        if (len < 2) return false;
        return name.substring(len - 2, len).equals("bg");
    }

    public int getPoints() {
        return 5 * (dropSet.size() + clickSet.size() + enterSet.size());
    }

    public int getTotalPoints() {
        return currGame.getTotalPoints();
    }

    public static void initBitmap(Shape shape, Context context) {
        int imageId = context.getResources().getIdentifier(
                shape.getImageName(), "drawable", context.getPackageName());
        shape.setBitmapDrawable((BitmapDrawable) context.getResources().getDrawable(imageId));
    }

    private void startAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.5f, 1.0f);
        animator.setDuration(2000);
        animator.setRepeatCount(100);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                size = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();

    }

    public boolean checkWinPage() {
        return currPage.getPageName().equals("winPage");
    }

    public static class Action implements Serializable {
        private String verb, modifier;

        public Action(String verb, String modifier) {
            this.verb = verb;
            this.modifier = modifier;
        }

        public String getModifier() {return modifier;}
        public void setModifier(String newModifier) { modifier = newModifier; }

        public String toString() {
            return verb + " " + modifier;
        }

        // play<sound-name>
        public void playSound(String modifier, Context context) {
            MediaPlayer mp = MediaPlayer.create(context
                    , context.getResources().getIdentifier(modifier, "raw",
                            context.getPackageName()));
            mp.start();
        }

        // Script primitives
        public void scriptActions(Shape shape) {
            if (!Game.isGameMode()) {
                return;
            }
            switch (verb) {
                case "goto":
                    // goto <page-name>
                    shape.getPageBelong().getParentGame().setCurrentPage(modifier);
                    break;
                case "play":
                    // play <sound-name>
                    playSound(modifier, GameView.getViewContext());
                    break;
                default:
                    Game currGame = shape.getPageBelong().getParentGame();
                    for (Page currPage : currGame.getPageBelong()) {
                        // Loop over possesions
                        for (Shape currShape : currPage.getPossessions()) {
                            if (currShape.getShapeName().equalsIgnoreCase(modifier)) {
                                if (verb.equals("hide")) {
                                    // hide an item in possession -> remove
                                    currShape.setInvisible();
                                }
                                else {
                                    currShape.setVisible(); // show
                                }
                            }
                        }
                        // Loop over shapes
                        for (Shape currShape : currPage.getShapeList()) {
                            if (currShape.getShapeName().equalsIgnoreCase(modifier)) {
                                if (verb.equals("hide")) {
                                    currShape.setInvisible(); // hide
                                }
                                else {
                                    currShape.setVisible(); // show
                                }
                            }
                        }
                    }
            }
        }
    }

}
