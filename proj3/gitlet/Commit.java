package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

/** Represent a single commit.
 *  @author Qi Liu, Yaxin Yu
 */
public class Commit implements Serializable {

    /** Time that the commit is created. */
    private Date _time;
    /** Commit message. */
    private String _message;
    /** Represent the parent commit. */
    private String _parentId;
    /** Files that the parent commit is tracking. */
    private HashMap<String, String> _parentFiles;
    /** Files that this commit is tracking, mapping filenames to paths. */
    private HashMap<String, String> _files;
    /** Files that exist in the working direcotyr, but not tracked. */
    //private TreeSet<File> _untrackedFiles;
    /** Commit ID, which is a SHA-1 hash of the commit. */
    private String _id;

    /** Construct an initial commit. */
    public Commit() {
        _message = "initial commit";
        _time = new Date();
        _parentId = null;
        _parentFiles = null;
        _files = new HashMap<String, String>();
        //setUntracked();
        _id = generateIdInit();
    }

    /** Construct a new commit, with args MESSAGE, TIME, PARENTID,
     *  PARENTFILES. */
    public Commit(String message, Date time, String parentId,
                  HashMap<String, String> parentFiles) {
        _message = message;
        _time = time;
        _parentId = parentId;
        _parentFiles = parentFiles;
        setFiles();
        //setUntracked();
        _id = generateId();
    }

    /** Return a SHA-1 id for the initial commit. */
    private String generateIdInit() {
        return Utils.sha1(_message, Main.stdFormat(_time), "", "");
    }

    /** Return a SHA-1 id for this commit, which includes FILES, PARENTID,
     *  MESSAGE and TIME.  */
    private String generateId() {
        String files = "";
        for (String f : _files.keySet()) {
            files += f;
        }
        return Utils.sha1(_message, Main.stdFormat(_time), _parentId, files);
    }

    /** Set _files to inherit parentfiles, add staged files and delete
     *  removed files in this mapping. Add all files in the staging area
     *  to target directory. */
    private void setFiles() {
        _files = _parentFiles;

        File stagedDir = new File("./.gitlet/staged");
        for (File file : stagedDir.listFiles()) {
            String fileName = file.getName();
            Path from = Paths.get(file.getPath());
            String to = "./.gitlet/objects/" + Main.dirFormat(_time) + "/"
                        + fileName;
            _files.put(fileName, to);
            try {
                Files.copy(from, Paths.get(to));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File removedDir = new File("./.gitlet/removed");
        for (File file : removedDir.listFiles()) {
            String fileName = file.getName();
            _files.remove(fileName);
        }
    }

    /** Add all files that exist in the working directory but not tracked.
    private void setUntracked() {
        File wd = new File("./");
        for (File file : wd.listFiles()) {
            if (file.isFile() && !isTracking(file.getName())) {
                _untrackedFiles.add(file);
            }
        }
    }*/

    /** Return true if the commit is tracking FILENAME. */
    public boolean isTracking(String fileName) {
        return (_files.keySet().contains(fileName));
    }

    /** Return the ID of this commit. */
    String getId() {
        return _id;
    }

    /** Return the time of this commit. */
    Date getTime() {
        return _time;
    }

    /** Return _files. */
    HashMap<String, String> getFileMap() {
        return _files;
    }

    /** Return a tree set of all files tracked by this commit. */
    TreeSet<File> getFiles() {
        TreeSet<File> orderedF = new TreeSet<File>();
        for (String path : _files.values()) {
            File f = new File(path);
            orderedF.add(f);
        }
        return orderedF;
    }

    /** Return a treeset of all file names tracked by this commit.*/
    TreeSet<String> getFileNames() {
        TreeSet<String> orderedFN = new TreeSet<String>();
        for (String f : _files.keySet()) {
            orderedFN.add(f);
        }
        return orderedFN;
    }

    /** Return a string representing the path of FILE in this commit.
     *  If FILE is not tracked, return null. */
    String getPath(String file) {
        if (!isTracking(file)) {
            return null;
        } else {
            return _files.get(file);
        }
    }

    /** Return _untrackedFiles.
    TreeSet<File> getUntracked() {
        return _untrackedFiles;
    }*/

    /** Return the SHA-1 coded id of this commit's parent commit. */
    String getParentID() {
        return _parentId;
    }

    /** Return the commit message of this. */
    String getMessage() {
        return _message;
    }

}
