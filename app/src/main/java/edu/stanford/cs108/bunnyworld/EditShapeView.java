package edu.stanford.cs108.bunnyworld;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;


public class EditShapeView extends View {

    private static Context context;
    private Game currGame;
    private Page currPage;

    private boolean isClick, inShape;
    private Shape clickedShape = null;
    private float x1, y1, x2, y2;
    private float dx, dy;
    private static float possessionsBound;
    public int viewWidth, viewHeight;
    private long startTime = 0, endTime = 0;
    private float possessionEdgeWidth = 5;

    private EditText xET, yET, widthET, heightET, shapeNameET, textET, fontET, lineWidthET;
    private CheckBox movableCB, visibleCB, animateCB;
    private Spinner shapeSpinner, fontSpinner, fontStyleSpinner, bgmSpinner;

    private SeekBar redSB, greenSB, blueSB;
    public static float size;

    private Paint possessionsLine;

    public EditShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currGame = Game.getCurrGame();
        currPage = currGame.getCurrentPage();
        EditShapeView.context = getContext();
        // unselect any shape at first
        currPage.unSetSelectedShape();
        // initialize animation
        startAnimator();

        possessionsLine = new Paint();
        possessionsLine.setColor(Color.BLACK);
        possessionsLine.setStyle(Paint.Style.STROKE);
        possessionsLine.setStrokeWidth(5.0f);
    }

    public static Context getViewContext() {
        return context;
    }

    private void renderEditView(Shape shape) {
        shapeSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.shapeSpinner);
        ArrayAdapter<String> spAdapter = (ArrayAdapter<String>) shapeSpinner.getAdapter();
        xET = (EditText) ((Activity) getContext()).findViewById(R.id.x);
        yET = (EditText) ((Activity) getContext()).findViewById(R.id.y);
        widthET = (EditText) ((Activity) getContext()).findViewById(R.id.width);
        heightET = (EditText) ((Activity) getContext()).findViewById(R.id.height);
        shapeNameET = (EditText) ((Activity) getContext()).findViewById(R.id.shapeName);
        textET = (EditText) ((Activity) getContext()).findViewById(R.id.text);
        fontET = (EditText) ((Activity) getContext()).findViewById(R.id.fontSize);
        fontSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.fontSpinner);
        ArrayAdapter<String> fontAdapter = (ArrayAdapter<String>) fontSpinner.getAdapter();
        fontStyleSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.fontStyleSpinner);
        ArrayAdapter<String> fontStyleAdapter = (ArrayAdapter<String>) fontStyleSpinner.getAdapter();
        visibleCB = (CheckBox) ((Activity) getContext()).findViewById(R.id.visible);
        movableCB = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        animateCB = (CheckBox) ((Activity) getContext()).findViewById(R.id.animate);
        redSB = (SeekBar) ((Activity) getContext()).findViewById(R.id.red);
        greenSB = (SeekBar) ((Activity) getContext()).findViewById(R.id.green);
        blueSB = (SeekBar) ((Activity) getContext()).findViewById(R.id.blue);
        lineWidthET = (EditText) ((Activity) getContext()).findViewById(R.id.lineWidth);
        bgmSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.bgmSpinner);
        ArrayAdapter<String> bgmAdapter = (ArrayAdapter<String>) bgmSpinner.getAdapter();

        xET.setText(String.valueOf(shape.getX()));
        yET.setText(String.valueOf(shape.getY()));
        widthET.setText(String.valueOf(shape.getWidth()));
        heightET.setText(String.valueOf(shape.getHeight()));
        shapeNameET.setText(shape.getShapeName());

        int cur_bgm = currPage.getBGM().length();
        if (cur_bgm == 0) {
            currPage.setBGM("");
        }
        else {
            currPage.setBGM(currPage.getBGM());
        }

        String shapeType = currPage.getSelectedShape().getImageName().length() == 0 ?
                "box" : currPage.getSelectedShape().getImageName();
        if (shape.getText().length() > 0) {
            shapeType = "text";
            textET.setText(shape.getText());
            fontET.setText(String.valueOf(shape.getTextSize()));
            fontSpinner.setSelection(fontAdapter.getPosition(shape.getFontType()));
        } else {
            textET.setText("");
        }
        shapeSpinner.setSelection(spAdapter.getPosition(shapeType));
        bgmSpinner.setSelection(bgmAdapter.getPosition(currPage.getBGM()));
        textET.setEnabled(shapeType.equals("text"));
        fontET.setEnabled(shapeType.equals("text"));
        fontSpinner.setEnabled(shapeType.equals("text"));
        fontStyleSpinner.setEnabled(shapeType.equals("text"));
        lineWidthET.setEnabled(shapeType.equals("box"));

        redSB.setProgress(shape.getRed());
        greenSB.setProgress(shape.getGreen());
        blueSB.setProgress(shape.getBlue());
        movableCB.setChecked(shape.getMovable());
        visibleCB.setChecked(shape.getVisible());
        animateCB.setChecked(shape.getAnimation());
        fontET.setText(String.valueOf(shape.getTextSize()));
        lineWidthET.setText(String.valueOf(shape.getOutlineWidth()));
    }

    private void resetEditView() {
        shapeSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.shapeSpinner);
        bgmSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.bgmSpinner);
        xET = (EditText) ((Activity) getContext()).findViewById(R.id.x);
        yET = (EditText) ((Activity) getContext()).findViewById(R.id.y);
        widthET = (EditText) ((Activity) getContext()).findViewById(R.id.width);
        heightET = (EditText) ((Activity) getContext()).findViewById(R.id.height);
        shapeNameET = (EditText) ((Activity) getContext()).findViewById(R.id.shapeName);
        textET = (EditText) ((Activity) getContext()).findViewById(R.id.text);
        fontET = (EditText) ((Activity) getContext()).findViewById(R.id.fontSize);
        fontSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.fontSpinner);
        fontStyleSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.fontStyleSpinner);
        lineWidthET = (EditText) ((Activity) getContext()).findViewById(R.id.lineWidth);
        visibleCB = (CheckBox) ((Activity) getContext()).findViewById(R.id.visible);
        movableCB = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        animateCB = (CheckBox) ((Activity) getContext()).findViewById(R.id.animate);
        redSB = (SeekBar) ((Activity) getContext()).findViewById(R.id.red);
        greenSB = (SeekBar) ((Activity) getContext()).findViewById(R.id.green);
        blueSB = (SeekBar) ((Activity) getContext()).findViewById(R.id.blue);
        ArrayAdapter<String> spAdapter = (ArrayAdapter<String>) shapeSpinner.getAdapter();
        ArrayAdapter<String> bgmAdapter = (ArrayAdapter<String>) bgmSpinner.getAdapter();
        ArrayAdapter<String> fontAdapter = (ArrayAdapter<String>) fontSpinner.getAdapter();
        ArrayAdapter<String> fontStyleAdapter = (ArrayAdapter<String>) fontStyleSpinner.getAdapter();

        xET.setText("10.0");
        yET.setText("10.0");
        widthET.setText("200.0");
        heightET.setText("200.0");
        shapeNameET.setText("");
        textET.setText("");
        fontET.setText("");
        visibleCB.setChecked(true);
        movableCB.setChecked(false);
        animateCB.setChecked(false);
        redSB.setProgress(128);
        greenSB.setProgress(128);
        blueSB.setProgress(128);
        shapeSpinner.setSelection(spAdapter.getPosition("box"));
        bgmSpinner.setSelection(bgmAdapter.getPosition(currPage.getBGM()));
        lineWidthET.setText("");
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Shape sh : currPage.getShapeList()) {
            sh.draw(canvas);
        }

        // Draw possessionsBound
        currPage.setPossessionsBound(possessionsBound);
        canvas.drawLine(0.0f, possessionsBound, viewWidth, possessionsBound, possessionsLine);
    }

    public static void initBitmap(Shape shape, Context context) {
        int imageId = context.getResources().getIdentifier(
                shape.getImageName(), "drawable", context.getPackageName());
        shape.setBitmapDrawable((BitmapDrawable) context.getResources().getDrawable(imageId));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;

        // Zhihan: 1.22f is trying to make the viewHeight in Editmode
        // the same as the GameMode
        // TODO: change this weird and hardcoded 1.22f
        possessionsBound = 0.7f * viewHeight * 1.22f;
    }

    private Shape newShape;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();

                currPage.unSetSelectedShape();
                clickedShape = currPage.shapeSelected(x1, y1);
                startTime = System.currentTimeMillis();

                if (clickedShape != null) {
                    // undo
                    EditShapeActivity.beforeMoveShapeStack.push(clickedShape);
                    newShape = new Shape(clickedShape);
                    currGame.getCurrentPage().removeShape(clickedShape);
                    currGame.getCurrentPage().addShape(newShape);

                    dx = x1 - clickedShape.getX();
                    dy = y1 - clickedShape.getY();
                    inShape = true;
                    renderEditView(clickedShape);
                } else {
                    currPage.unSetSelectedShape();
                    resetEditView();
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                x2 = event.getX();
                y2 = event.getY();
                if (inShape) {
                    newShape.updatePosition(x2 - dx, y2 - dy);
                    renderEditView(newShape);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                endTime = System.currentTimeMillis();
                isClick = endTime - startTime <= 200;

                // undo
                if (clickedShape != null && inShape && isClick
                        && !EditShapeActivity.beforeMoveShapeStack.isEmpty()) {
                    currGame.getCurrentPage().removeShape(newShape);
                    currGame.getCurrentPage().addShape(EditShapeActivity.beforeMoveShapeStack.pop());
                }

                if (clickedShape != null && inShape && !isClick) {
                    boolean overlap = false;
                    float newX = x2 - dx;
                    float newY = y2 - dy;

                    if (newX  + clickedShape.getWidth() / 2 < 0
                        || newY + clickedShape.getHeight() / 2 < 0
                        || newX + clickedShape.getWidth() / 2 > viewWidth
                        || newY + clickedShape.getHeight() / 2 > possessionsBound) {
                        // Case 1: out of the game area -> back to the original place
                        clickedShape.updatePosition(x1 - dx, y1 - dy);

                        // undo
                        if (!EditShapeActivity.beforeMoveShapeStack.isEmpty()) {
                            currGame.getCurrentPage().removeShape(newShape);
                            currGame.getCurrentPage().addShape(EditShapeActivity.beforeMoveShapeStack.pop());
                        }
                    } else {
                        // Case 2: check if overlap with other shapes
                        float newRight = newX + clickedShape.getWidth();
                        float newBottom = newY + clickedShape.getHeight();

                        // Check if overlap with any shape in game area exclude background
                        List<Shape> tempList1 = new ArrayList<>();

                        if (currPage.getBackgroundImage() != null) {
                            // Case 1: Exist Background image
                            for (int i = 0; i < currPage.getShapeList().size() - 1; i++) {
                                tempList1.add(i, currPage.getShapeList().get(i+1));
                            }
                        }
                        else {
                            // Case 2: Does not exist background image
                            tempList1 = new ArrayList<>(currPage.getShapeList());
                        }

                        for (Shape shape : tempList1) {
                            if (!shape.getShapeName().equals(clickedShape.getShapeName())
                                && !(newX > shape.getX() + shape.getWidth()
                                || newY > shape.getY() + shape.getHeight()
                                || newRight < shape.getX() || newBottom < shape.getY())) {
                                overlap = true;
                                clickedShape.updatePosition(x1 - dx, y1 - dy);
                            }
                        }

                        if (!overlap) {
                            if (newY + newShape.getHeight() > possessionsBound
                                    && newY < possessionsBound) {
                                if (newY + newShape.getHeight() / 2 < possessionsBound) {
                                    newY = possessionsBound - newShape.getHeight() - possessionEdgeWidth;
                                }
                            }
                            newShape.updatePosition(newX, newY);

                            // undo
                            EditShapeActivity.actionStack.push("move");
                            EditShapeActivity.afterMoveShapeStack.push(newShape);
                        } else if (!EditShapeActivity.beforeMoveShapeStack.isEmpty()){
                            // undo
                            currGame.getCurrentPage().removeShape(newShape);
                            currGame.getCurrentPage().addShape(EditShapeActivity.beforeMoveShapeStack.pop());
                        }
                    }

                    newShape.setSelected(true);
                    currGame.getCurrentPage().setSelectedShape(newShape);
                    renderEditView(newShape);
                    invalidate();
                }
                inShape = false;
                clickedShape = null;
        }

        return true;
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
}
