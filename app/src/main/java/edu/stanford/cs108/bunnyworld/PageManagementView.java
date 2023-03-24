package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.util.List;

public class PageManagementView extends View {
    private Paint unselectedTextPaint;
    private Paint selectedTextPaint;
    private Page selected;
    private Game game;
    // selected page: underline, bold, blue
    // first page: black outline
    public PageManagementView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        game = Game.getCurrGame();
        init();
    }

    private void init() {
        selectedTextPaint = new Paint();
        selectedTextPaint.setColor(Color.BLUE);
        selectedTextPaint.setTextSize(60);
        selectedTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        selectedTextPaint.setUnderlineText(true);
        // selected one: blue, bold, underline

        unselectedTextPaint = new Paint();
        unselectedTextPaint.setColor(Color.BLACK);
        unselectedTextPaint.setTextSize(60);
    }

    public Page getSelected() {
        return selected;
    }

    public void setSelected(Page page) {
        selected = page;
        game.setCurrentPage(page);
    }

    public void clearSelected() {
        selected = null;
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

        int UI = View.SYSTEM_UI_FLAG_FULLSCREEN;
        this.setSystemUiVisibility(UI);
        List<Page> pageList = game.getPageList();
        for (int i = 0; i < pageList.size(); i++) {
            Page page = pageList.get(i);
            String text = page.getPageName() + (page == game.getFirstPage() ?  "**" : "");
            Paint textPaint = page == selected ? selectedTextPaint : unselectedTextPaint;
            canvas.drawText(text, getRectF(i).left + 20,
                    getRectF(i).bottom - 15, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                boolean flag = false;
                List<Page> pageList = game.getPageList();
                for (int i = 0; i < pageList.size(); i++) {
                    RectF r = getRectF(i);
                    if (x > r.left && x < r.right && y > r.top && y < r.bottom) {
                        setSelected(pageList.get(i));
                        EditText newName = ((Activity) getContext()).findViewById(R.id.page_new_name);
                        if (newName != null) {
                            String pageName = pageList.get(i).getPageName();
                            newName.setText(pageName);
                        }
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
