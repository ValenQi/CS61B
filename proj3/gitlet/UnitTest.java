package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Qi Liu, Yaxin Yu
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** Test serialize and deseiralize in Utils. */
    @Test
    public void testSerialization() {
    }

    /** Test sameContents in Utils. */
    @Test
    public void testSameContents() {
        String name1 = "first.txt";
        String name2 = "second.txt";
        String name3 = "third.txt";
        String content1 = "This is the first file.";
        String content2 = "This is the second file.";
        File f1 = new File(name1);
        File f2 = new File(name2);
        File f3 = createFile(name3, content2);

        assertTrue(Utils.sameContents(f1, f2));
        assertFalse(Utils.sameContents(f1, f3));
    }

    /** Test the commit class. */
    @Test
    public void testCommit() {}

    /** Test the commitTree class. */
    @Test
    public void testCommitTree() {}

    //Test helper functions in Main//

    /** Test nonExistingCommand. */
    @Test
    public void testNonExistingCommand() {}

    /** Test wrongOperandFormat. */
    @Test
    public void testWrongOperandFormat() {}

    /** Test emptyDir. */
    @Test
    public void testEmptyDir() {}

    /** Test printDirInOrder. */
    @Test
    public void testPrintDirInOrder() {}

    /** Create a file with NAME and CONTENTS. */
    private static File createFile(String name, String contents) {
        File f = new File(name);
        try {
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

}


