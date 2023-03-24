package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.renderscript.Script;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EditScriptView extends View {
    private String selectedScript;
    private Game game;
    private Shape shape;
    private List<String> scriptList = new ArrayList<>();
    private Paint textPaint;
    private Paint selectedTextPaint;
    private Paint scriptTextPaint;
    private int screenWidth;
    private int canvasWidth;
    private int screenHeight;
    private int canvasHeight;

    public EditScriptView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        game = Game.getCurrGame();
        shape = game.getCurrentPage().getSelectedShape();
        scriptList = shape.getAllScripts();
        init();
    }

    public void init() {
        selectedTextPaint = new Paint();
        selectedTextPaint.setColor(Color.BLUE);
        selectedTextPaint.setTextSize(40);
        selectedTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        selectedTextPaint.setUnderlineText(true);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);

        scriptTextPaint = new Paint();
        scriptTextPaint.setColor(Color.rgb(98, 0, 238));
        scriptTextPaint.setTextSize(50);
        scriptTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));

    }


    public void clearSelected() {
        selectedScript = null;
    }

    public String getSelectedScript() {
        return selectedScript;
    }

    public void setSelectedScript(String action) {
        selectedScript = action;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth =  screenWidth;
        int measuredHeight =  screenHeight;

        if (canvasWidth  > screenWidth) {
            measuredWidth = canvasWidth;
        }
        if (canvasHeight > screenHeight) {
            measuredHeight = canvasHeight;
        }
        this.setMeasuredDimension(measuredWidth, measuredHeight);

    }

    private RectF getRectF(int i) {
        float height = (float) (screenHeight/10);
        float width = screenWidth - 4 * height;
        float left = height;
        float top = height * (i * 2 + 1);
        return new RectF(left, top, left + width, top + height);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int UI = View.SYSTEM_UI_FLAG_FULLSCREEN;
        this.setSystemUiVisibility(UI);
        for (int i = 0; i < scriptList.size(); i++){
            String currScript = scriptList.get(i);
            Paint paint = currScript == selectedScript ? selectedTextPaint : textPaint;
            canvas.drawText(currScript,getRectF(i+1).left, getRectF(i+1).bottom, paint);
            if (i == scriptList.size() - 1) {
                canvasHeight = (int) Math.ceil(getRectF(i + 1).bottom) + 50;
            }
        }

        if (shape.scriptToString() != null) {
            String scriptString = shape.scriptToString();
            canvas.drawText(scriptString, getRectF(0).left, getRectF(0).bottom, scriptTextPaint);
            canvasWidth = (int) Math.ceil(scriptTextPaint.measureText(scriptString)) + 150;
        }
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                boolean flag = false;
                for (int i = 0; i < scriptList.size(); i++) {
                    RectF r = getRectF(i + 1);
                    if (x > r.left && x < r.right && y > r.top && y < r.bottom) {
                        setSelectedScript(scriptList.get(i));
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    clearSelected();
                }
                break;
        }
        invalidate();
        return true;
    }
}
