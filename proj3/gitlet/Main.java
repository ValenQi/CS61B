package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Yaxin Yu, Qi Liu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (properCommandLine(args)) {
            switch (args[0]) {
            case "init":
                initialize();
                break;
            case "add":
                addCommand(args[1]);
                break;
            case "commit":
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                } else if (args[1].length() == 0) {
                    System.out.println("Please enter a commit message.");
                } else {
                    commitCommand(args[1]);
                }
                break;
            case "rm":
                removeCommand(args[1]);
                break;
            case "log":
                logCommand();
                break;
            case "global-log":
                globalLogCommand();
                break;
            case "find":
                findCommand(args[1]);
                break;
            case "status":
                statusCommand();
                break;
            case "checkout":
                checkoutCommand(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "branch":
                branchCommand(args[1]);
                break;
            case "rm-branch":
                removeBranchCommand(args[1]);
                break;
            case "reset":
                resetCommand(args[1], null);
                break;
            case "merge":
                mergeCommand(args[1]);
                break;
            default:
                return;
            }
        }
    }

    /** Return true if the commandline ARGS can be processed. */
    private static boolean properCommandLine(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return false;
        }
        String command = args[0];
        if (nonexistingCommand(command)) {
            System.out.println("No command with that name exists.");
            return false;
        }
        if (wrongOperandFormat(args)) {
            System.out.println("Incorrect operands.");
            return false;
        }
        if (!command.equals("init") && noGitLet()) {
            System.out.println("Not in an initialized gitlet directory.");
            return false;
        }
        return true;
    }

    /** Sets up a new gitlet system. Abort if a gitlet system
     *  already exists. */
    public static void initialize() {
        if (!noGitLet()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            File temp = new File(".gitlet");
            temp.mkdir();
            temp = new File(".gitlet/staged");
            temp.mkdir();
            temp = new File(".gitlet/removed");
            temp.mkdir();
            temp = new File(".gitlet/objects");
            temp.mkdir();
            temp = new File(".gitlet/commits");
            temp.mkdir();
            Utils.serialize(new CommitTree(), "./.gitlet/commitTree.ser");
        }
    }

    /** Adds a copy of FILE to the staging area in .gitlet.
     *  Do nothing if the current working version is identical
     *  to the version in the repository. */
    public static void addCommand(String file) {
        File toBeAdded = new File("./" + file);
        if (!toBeAdded.exists()) {
            System.out.println("File does not exist.");
        } else {
            CommitTree tree
                = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
            Commit curr = tree.getHead();
            File inRemoved = new File("./.gitlet/removed/" + file);
            if (inRemoved.exists()) {
                inRemoved.delete();
            }
            if (curr.isTracking(file) && sameInCommit(file, curr)) {
                return;
            }
            stage(file);
        }
    }

    /** Saves a snapshot of certain files in the current commit and staging
     *  area, creating a new commit with MESSAGE and adding the new commit
     *  to the commit tree. */
    public static void commitCommand(String message) {
        File stagedDir = new File("./.gitlet/staged");
        File removedDir = new File("./.gitlet/removed");
        if (stagedDir.list().length == 0 && removedDir.list().length == 0) {
            System.out.println("No changes added to the commit.");
        } else {
            CommitTree tree
                = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
            Date time = new Date();
            File targetDir = new File("./.gitlet/objects/" + dirFormat(time));
            targetDir.mkdir();
            tree.addNewCommit(message, time);
            emptyDir(stagedDir);
            emptyDir(removedDir);
            Utils.serialize(tree, "./.gitlet/commitTree.ser");
        }
    }

    /** Untrack FILE for next commit. Remove FILE from the working directory
     * if tracked in the current commit. Unstage FILE if it had been staged.*/
    public static void removeCommand(String file) {
        CommitTree tree
             = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        Commit curr = tree.getHead();

        if (!inStaged(file) && !curr.isTracking(file)) {
            System.out.println("No reason to remove the file");
        } else {
            if (curr.isTracking(file)) {
                untrack(file);
            }
            if (inStaged(file)) {
                new File("./.gitlet/staged/" + file).delete();
            }
        }
    }

    /** Print out information of the current head commit's history. */
    public static void logCommand() {
        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        Commit curr = tree.getHead();
        while (curr.getParentID() != null) {
            printCommitInfo(curr);
            curr = (Commit) Utils.deserialize("./.gitlet/commits/"
                                        + curr.getParentID() + ".ser");
        }
        printCommitInfo(curr);
    }

    /** Print out information about all commits ever made. */
    public static void globalLogCommand() {
        File commitDir = new File("./.gitlet/commits");
        for (String commit : commitDir.list()) {
            Commit curr = (Commit) Utils.deserialize(
                            "./.gitlet/commits/" + commit);
            printCommitInfo(curr);
        }
    }

    /** Prints out ids of all commits that have MESSAGE. */
    public static void findCommand(String message) {
        File commitDir = new File("./.gitlet/commits");
        boolean found = false;
        for (String commit : commitDir.list()) {
            Commit curr = (Commit) Utils.deserialize(
                            "./.gitlet/commits/" + commit);
            if (curr.getMessage().equals(message)) {
                System.out.println(curr.getId());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message");
        }
    }

    /** Print out all existing branches, names of all staged files,
     *  names of all removed files, names of all modified but unstaged
     *  files, and names of all untracked files. */
    public static void statusCommand() {
        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        Commit curr = tree.getHead();
        TreeSet<File> tracking = curr.getFiles();
        //TreeSet<File> untracked = curr.getUntracked();
        File staged = new File("./.gitlet/staged");
        File removed = new File("./.gitlet/removed");

        System.out.println("=== Branches ===");
        for (String b : tree.getAllBranches()) {
            if (b.equals(tree.getActiveBranch())) {
                System.out.println("*" + b);
            } else {
                System.out.println(b);
            }
        }

        System.out.println("\n=== Staged Files ===");
        printDirInOrder(staged);

        System.out.println("\n=== Removed Files ===");
        printDirInOrder(removed);

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        TreeSet<String> modNotStaged = new TreeSet<String>();
        for (File file : staged.listFiles()) {
            String fileName = file.getName();
            if (!inWD(fileName)) {
                modNotStaged.add(fileName + " (deleted)");
            } else if (!sameInWD(file)) {
                modNotStaged.add(fileName + " (modified)");
            }
        }
        for (File file : tracking) {
            String fileName = file.getName();
            if (!inStaged(fileName) && !inWD(fileName)
                && !inRemoved(fileName)) {
                modNotStaged.add(fileName + " (deleted)");
            } else if (!inStaged(fileName) && inWD(fileName)
                && !sameInWD(file)) {
                modNotStaged.add(fileName + " (modified)");
            }
        }
        /*for (File file : untracked) {
            String fileName = file.getName();
            if (inWD(fileName) && !inStaged(fileName) && !sameInWD(file)) {
                modNotStaged.add(fileName + " (modified)");
            }
        }*/
        for (String name : modNotStaged) {
            System.out.println(name);
        }

        System.out.println("\n=== Untracked Files ===");
        File wd = new File("./");
        for (File file : wd.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && !curr.isTracking(fileName)
                && !inStaged(fileName)) {
                System.out.println(fileName);
            }
        }
    }

    /** Depending on operands after the command checkout in ARGS,
     *  1. Place the given file in the working directory.
     *  2. Place the file in the commit with the given id in the wd.
     *  3. Place all files in the head commit of the given branch
     *     in the wd.*/
    public static void checkoutCommand(String[] args) {
        switch (args.length) {
        case 1:
            checkoutBranch(args[0]);
            break;
        case 2:
            checkoutFile(args[1]);
            break;
        case 3:
            checkoutCommitFile(args[0], args[2]);
            break;
        default:
            return;
        }
    }

    /** Handles first case of checkout command (a single FILENAME
     *  is passed in). */
    public static void checkoutFile(String fileName) {
        CommitTree tree =
            (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        Commit curr = tree.getHead();
        String path = curr.getPath(fileName);
        if (path == null) {
            System.out.println("File does not exist in that commit.");
        } else {
            try {
                Files.copy(Paths.get(path), Paths.get("./" + fileName),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Handles second case of checkout command (a single FILENAME
     *  and a commit ID are passed in. */
    public static void checkoutCommitFile(String id, String fileName) {
        File commit = new File("./.gitlet/commits/" + id + ".ser");
        if (!commit.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        Commit targetC = tree.findCommit(id);
        String path = targetC.getPath(fileName);
        if (path == null) {
            System.out.println("File does not exist in that commit.");
        } else {
            try {
                Files.copy(Paths.get(path), Paths.get("./" + fileName),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Handles third case of checkout command (a single BRANCH
     *  name is passed in). */
    public static void checkoutBranch(String branch) {
        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        if (tree.getActiveBranch().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
        } else if (!tree.getAllBranches().contains(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        Commit branchHead = tree.getBranchHead(branch);
        resetCommand(branchHead.getId(), branch);
    }

    /** Creates a new branch with the name BRANCH. The new
     *  branch points at the current head node. */
    public static void branchCommand(String branch) {
        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        tree.addBranch(branch);
        Utils.serialize(tree, "./.gitlet/commitTree.ser");
    }

    /** Deletes the branch with name BRANCH. */
    public static void removeBranchCommand(String branch) {
        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        tree.removeBranch(branch);
        Utils.serialize(tree, "./.gitlet/commitTree.ser");
    }

    /** Check out all files tracked by the commit with COMMITID.
     *  Remove tracked files not present in the given commit.
     *  Change the current branch's head to the given commit.
     *  Switch to BRANCH if given.
     *  Clear staging area. */
    public static void resetCommand(String commitID, String branch) {
        File commit = new File("./.gitlet/commits/" + commitID + ".ser");
        if (!commit.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        Commit curr = tree.getHead();
        Commit futureHead = (Commit) Utils.deserialize("./.gitlet/commits/"
                                                       + commitID + ".ser");
        Set<String> currFiles = curr.getFileNames();
        Set<String> futureFiles = futureHead.getFileNames();

        File wd = new File("./");
        for (File file : wd.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && !curr.isTracking(fileName)
                && futureHead.isTracking(fileName)) {
                System.out.println("There is an untracked file"
                     + "in the way; delete it or add it first.");
                return;
            }
        }

        for (String file : currFiles) {
            if (!futureHead.isTracking(file)) {
                File toBeDeleted = new File("./" + file);
                toBeDeleted.delete();
            }
        }
        for (String file : futureFiles) {
            copyFrom(file, futureHead);
        }
        emptyDir(new File("./.gitlet/staged"));
        emptyDir(new File("./.gitlet/removed"));

        if (branch != null) {
            tree.switchBranch(branch);
        } else {
            tree.resetBranchHead(tree.getActiveBranch(), futureHead);
        }
        Utils.serialize(tree, "./.gitlet/commitTree.ser");
    }

    /** Merge files from the given BRANCH into the current branch. */
    public static void mergeCommand(String branch) {
        File staged = new File("./.gitlet/staged");
        File removed = new File("./.gitlet/removed");
        if (staged.list().length != 0 || removed.list().length != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        if (tree.getActiveBranch().equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
        } else if (!tree.getAllBranches().contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        Commit curr = tree.getHead();
        Commit given = tree.getBranchHead(branch);
        Commit splitPoint = tree.findSplitPoint(branch);

        File wd = new File("./");
        for (File file : wd.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && !curr.isTracking(fileName)
                && given.isTracking(fileName)) {
                System.out.println("There is an untracked file"
                     + "in the way; delete it or add it first.");
                return;
            }
        }

        if (splitPoint.getId() == given.getId()) {
            System.out.println("Given branch is an ancestor of the current"
                               + "branch.");
            return;
        } else if (splitPoint.getId() == curr.getId()) {
            resetCommand(given.getId(), null);
            System.out.println("Current branch fast-forwarded.");
            return;
        } else {
            realMerge(branch);
        }
    }

    /** Merge files from the given BRANCH into the current branch when
     *  neither is the split point. */
    private static void realMerge(String branch) {
        CommitTree tree
            = (CommitTree) Utils.deserialize("./.gitlet/commitTree.ser");
        Commit curr = tree.getHead();
        Commit given = tree.getBranchHead(branch);
        Commit splitPoint = tree.findSplitPoint(branch);
        Set<String> currFiles = curr.getFileNames();
        Set<String> givenFiles = given.getFileNames();
        Set<String> inConflictFiles = new TreeSet<String>();

        for (String file : currFiles) {
            if (splitPoint.isTracking(file)
                && sameInCommits(file, curr, splitPoint)) {
                if (!given.isTracking(file)) {
                    untrack(file);
                } else if (!sameInCommit(file, given)) {
                    copyFrom(file, given);
                    stage(file);
                }
            } else if (given.isTracking(file)
                       && !sameInCommits(file, curr, given)
                       && (!splitPoint.isTracking(file)
                           || (!sameInCommits(file, curr, splitPoint)
                               && !sameInCommits(file, given, splitPoint)))) {
                inConflictFiles.add(file);
            } else if (splitPoint.isTracking(file)
                       && !sameInCommits(file, curr, splitPoint)
                       && !given.isTracking(file)) {
                inConflictFiles.add(file);
            }
        }
        for (String file : givenFiles) {
            if (!curr.isTracking(file) && !splitPoint.isTracking(file)) {
                copyFrom(file, given);
                stage(file);
            } else if (splitPoint.isTracking(file)
                       && !sameInCommits(file, splitPoint, given)
                       && !curr.isTracking(file)) {
                inConflictFiles.add(file);
            }
        }
        for (String file : inConflictFiles) {
            mergeFile(file, given);
        }

        if (inConflictFiles.isEmpty()) {
            Date time = new Date();
            File targetDir = new File("./.gitlet/objects/" + dirFormat(time));
            targetDir.mkdir();
            String message = "Merged " + tree.getActiveBranch()
                             + " with " + branch + ".";
            tree.addNewCommit(message, time);
            emptyDir(new File("./.gitlet/staged"));
            emptyDir(new File("./.gitlet/removed"));
        } else {
            System.out.println("Encountered a merge conflict.");
        }
        Utils.serialize(tree, "./.gitlet/commitTree.ser");
    }

    /** Return true if COMMAND does not exist. */
    public static boolean nonexistingCommand(String command) {
        return !ZERO_COM.contains(command)
               && !SINGLE_COM.contains(command)
               && !command.equals("checkout")
               && !command.equals("commit");
    }

    /** Return true if ARGS contains a command with the
     *  wrong number or format of operands. Assumes that
     *  the command is valid. */
    public static boolean wrongOperandFormat(String[] args) {
        String command = args[0];
        if (ZERO_COM.contains(command)) {
            return (args.length != 1);
        } else if (SINGLE_COM.contains(command)) {
            return (args.length != 2);
        } else if (command.equals("checkout")) {
            int l = args.length;
            if (l == 1 || l > 4) {
                return true;
            } else if (l == 2) {
                return false;
            } else {
                return !args[l - 2].equals("--");
            }
        }
        return false;
    }

    /** Return true if .gitlet subdirectory has not been
     *  initialized in the current working directory.*/
    private static boolean noGitLet() {
        File gl = new File(".gitlet");
        return !(gl.isDirectory() && gl.exists());
    }

    /** Return true if FILENAME is the same in the working directory and COMMIT.
     *  Assume it is tracked in COMMIT. */
    private static boolean sameInCommit(String fileName, Commit commit) {
        File f1 = new File("./" + fileName);
        File f2 = new File(commit.getPath(fileName));
        return Utils.sameContents(f1, f2);
    }

    /** Copy FILE to the staging area. */
    private static void stage(String file) {
        Path from = Paths.get("./" + file);
        Path to = Paths.get("./.gitlet/staged/" + file);
        try {
            Files.copy(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Return a string representing the current TIME as
     *  a valid directory name. */
    static String dirFormat(Date time) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return f.format(time);
    }

    /** Clear all files in DIR. */
    private static void emptyDir(File dir) {
        for (File f : dir.listFiles()) {
            f.delete();
        }
    }

    /** Untrack FILE and delete it if it exists in the working directory. */
    private static void untrack(String file) {
        File toUntrack = new File("./.gitlet/removed/" + file);
        try {
			toUntrack.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        if (inWD(file)) {
            File inWD = new File("./" + file);
            inWD.delete();
        }

    }

    /** Print out the id, the timeStamp, and commit message of C. */
    private static void printCommitInfo(Commit c) {
        System.out.println("===");
        System.out.println("Commit " + c.getId());
        Date time = c.getTime();
        System.out.println(stdFormat(time));
        System.out.println(c.getMessage() + "\n");
    }

    /** Return TIME in the standard log format. */
    static String stdFormat(Date time) {
        SimpleDateFormat std = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return std.format(time);
    }

    /** Print all file names in DIR in lexicographic order . */
    private static void printDirInOrder(File dir) {
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            System.out.println(file.getName());
        }
    }

    /** Return true if FILE is the same as it is in the working directory.
     *  Assume FILE exists in the working directory. */
    private static boolean sameInWD(File file) {
        File inWD = new File("./" + file.getName());
        return Utils.sameContents(inWD, file);
    }

    /** Return true if the file named FILENAME exists in .gitlet/staged. */
    private static boolean inStaged(String fileName) {
        File inStaged = new File("./.gitlet/staged/" + fileName);
        return inStaged.exists();
    }

    /** Return true if the file named FILENAME exists in .gitlet/removed. */
    private static boolean inRemoved(String fileName) {
        File inRemoved = new File("./.gitlet/removed/" + fileName);
        return inRemoved.exists();
    }

    /** Return true if the file named FILENAME exists in working directory. */
    private static boolean inWD(String fileName) {
        File inWD = new File("./" + fileName);
        return inWD.exists();
    }

    /** Copy FILE from commit FROM to the working directory.
     *  Overwrite the file if it exists in the working directory. */
    private static void copyFrom(String file, Commit from) {
        Path source = Paths.get(from.getPath(file));
        Path target = Paths.get("./" + file);
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Return true if FILENAME is the same in C1 and C2. */
    private static boolean sameInCommits(String fileName, Commit c1,
                                         Commit c2) {
        File f1 = new File(c1.getPath(fileName));
        File f2 = new File(c2.getPath(fileName));
        return Utils.sameContents(f1, f2);
    }

    /** Replace the contents of a FILE in conflict in GIVEN in the
     *  standard format. */
    private static void mergeFile(String file, Commit given) {
        File currFile = new File("./" + file);
        File givenFile = new File(given.getPath(file));
        byte[] currContents = Utils.readContents(currFile);
        byte[] givenContents = Utils.readContents(givenFile);
        String newFile = "<<<<<<< HEAD" + System.lineSeparator()
                         + new String(currContents)
                         + "=======" + System.lineSeparator()
                         + new String(givenContents)
                         + ">>>>>>>" + System.lineSeparator();
        Utils.writeContents(currFile, newFile.getBytes());
    }

    /** A set of commands that require no operands. */
    private static final ArrayList<String> ZERO_COM =
            new ArrayList<>(Arrays.asList("init", "log",
                            "global-log", "status"));
    /** A set of commands that require one operand. */
    private static final ArrayList<String> SINGLE_COM =
            new ArrayList<>(Arrays.asList("add", "rm",
                            "find", "branch", "rm-branch",
                            "reset", "merge"));
}
