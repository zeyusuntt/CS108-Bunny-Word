package edu.stanford.cs108.bunnyworld;

import org.junit.Test;
import static org.junit.Assert.*;

import android.graphics.Paint;


public class BasicTest {
    @Test
    public void testShape() {
        Shape shape = new Shape();
        shape.setX(3.0f);
        shape.setY(2.0f);
        shape.setWidth(4.0f);
        shape.setHeight(5.0f);
        assertEquals(0, Float.compare(shape.getX(), 3.0f));
        assertEquals(0, Float.compare(shape.getY(), 2.0f));
        assertEquals(0, Float.compare(shape.getHeight(), 5.0f));
        assertEquals(0, Float.compare(shape.getWidth(), 4.0f));
        assertEquals("shape0", shape.getShapeName());
    }

    @Test
    public void testPage() {
        Shape s1 = new Shape();
        Shape s2 = new Shape();
        Page page1 = new Page("testPage1");
        Page page2 = new Page("testPage2");
        assertFalse(page1.isCurPage);
        assertFalse(page2.isCurPage);
        page2.setCurPage(true);
        assertFalse(page1.isCurPage);
        assertTrue(page2.isCurPage);
        assertEquals(0, page1.getShapeList().size());
        page1.addShape(s1);
        assertEquals(1, page1.getShapeList().size());
        page1.addToPossessions(s2);
        assertEquals(1, page1.getPossessions().size());
        assertEquals("testPage1", page1.getPageName());
    }

    @Test
    public void testGame() {
        Game game = new Game();
        assertEquals(1, game.getPageList().size());
        assertEquals(game.getPage("page1").getPageName(), game.getCurrentPage().getPageName());
        game.setCurrentGameName("testGame");
        assertEquals("testGame", game.getCurrentGameName());
        Page newPage = new Page("page");
        game.addPage(newPage);
        assertEquals(game.getPageBelong().size(), 2);
    }
}
