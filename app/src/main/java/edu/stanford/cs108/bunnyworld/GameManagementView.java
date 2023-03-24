package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.io.File;

public class GameManagementView extends View{

    private Paint selectedTextPaint;
    private Paint textPaint;
    private String selected;
    private String[] gameNames;

    public GameManagementView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public String[] getFileNames() {
        File dir = new File(this.getContext().getFilesDir(), "savedGame");
        return dir.list();
    }

    private void init() {
        selectedTextPaint = new Paint();
        selectedTextPaint.setColor(Color.BLUE);
        selectedTextPaint.setTextSize(60);
        selectedTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        selectedTextPaint.setUnderlineText(true);
        // selected one: blue, bold, underline
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(60);
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String gameName) {
        selected = gameName;
    }

    public void clearSelected() {
        setSelected(null);
    }

    private RectF getRectF(int i) {
        float recHeight = (float) (getHeight() / 9);
        float recWidth = (getWidth() - 4 * recHeight) / 3;
        float left = recHeight + (recHeight + recWidth) * (i / 4);
        float top = recHeight * (i % 4 * 2 + 1);
        return new RectF(left, top, left + recWidth, top + recHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        gameNames = getFileNames();
        for (int i = 0; i < gameNames.length; i++) {
            String gameName = gameNames[i];
            Paint paint = gameName.equals(selected) ? selectedTextPaint : textPaint;
            canvas.drawText(gameName.substring(0, gameName.length() - 5), getRectF(i).left, getRectF(i).bottom, paint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            boolean flag = false;
            for (int i = 0; i < gameNames.length; i++) {
                RectF r = getRectF(i);
                if (x > r.left && x < r.right && y > r.top && y < r.bottom) {
                    setSelected(gameNames[i]);
                    EditText newName = ((Activity) getContext()).findViewById(R.id.game_new_name);
                    if (newName != null) {
                        newName.setText(gameNames[i].substring(0, gameNames[i].length() - 5));
                    }
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                clearSelected();
            }
        }
        invalidate();
        return true;
    }
}
