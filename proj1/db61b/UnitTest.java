package db61b;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import ucb.junit.textui;

/** A test of classes Row, Column, Table, Condition and TableIterator.
 *  @author Qi Liu
 */
public class UnitTest {

    @Test
    public void testTable() {
        String name = "enrolled";
        String[] columnTitles = new String[]{"SID", "CCN", "Grade"};
        Table t = new Table(name, columnTitles);

        assertEquals(3, t.numColumns());
        assertEquals("enrolled", t.name());
        assertEquals("CCN", t.title(1));
        assertEquals(2, t.columnIndex("Grade"));

        Row row1 = new Row(new String[] {"101", "21228", "B"});
        Row row2 = new Row(new String[] {"102", "21231", "A"});
        assertEquals(row1.size(), 3);
        assertEquals("101", row1.get(0));
        assertEquals("21231", row2.get(1));
        assertTrue(row1.equals(row1));
        assertFalse(row1.equals(row2));

        assertTrue(t.add(row1));
        assertFalse(t.add(row1));
        assertTrue(t.add(row2));
        assertEquals(2, t.size());

        TableIterator ti = t.tableIterator();
        List<TableIterator> i = new ArrayList<TableIterator>();
        i.add(ti);
        Column col1 = new Column(t, columnTitles[0]);
        col1.resolve(i);
        assertEquals("SID", col1.name());
        assertEquals("101", col1.value());
        ti.next();
        assertEquals("102", col1.value());
        ti.next();
        assertFalse(ti.hasRow());
        ti.reset();
        assertEquals("101", col1.value());

        Column col2 = new Column(t, columnTitles[1]);
        col2.resolve(i);
        List<Column> columns = new ArrayList<Column>();
        columns.add(col1);
        columns.add(col2);
        Row row3 = new Row(columns);
        assertEquals("101", row3.get(0));
        assertEquals("21228", row3.get(1));

        Condition con1 = new Condition(col1, "=", col2);
        Condition con2 = new Condition(col1, "<", col2);
        assertFalse(con1.test());
        assertTrue(con2.test());
    }

    /* Run the unit tests in this file. */
    public static void main(String... args) {
        System.exit(textui.runClasses(UnitTest.class));
    }
}
