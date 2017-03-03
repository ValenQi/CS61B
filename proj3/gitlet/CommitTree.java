package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

/** Represent a tree of all commits.
 *  @author Qi Liu, Yaxin Yu
 */
public class CommitTree implements Serializable {

    /** All branches, with a name mapping to the head commitID of the branch. */
    private HashMap<String, String> _branches;

    /** Active branch of this commitTree. */
    private String _activeBranch;

    /** Initialize a commitTree. */
    public CommitTree() {
        _branches = new HashMap<String, String>();
        _activeBranch = "master";
        Commit initialCommit = new Commit();
        saveCommit(initialCommit);
    }

    /** Return the commit id of the head commit. */
    String getHeadId() {
        return _branches.get(_activeBranch);
    }

    /** Return the head commit of this commitTree. */
    Commit getHead() {
        return (Commit) Utils.deserialize("./.gitlet/commits/"
                                          + getHeadId() + ".ser");
    }

    /** Save a commit C and update the head. */
    void saveCommit(Commit c) {
        _branches.put(_activeBranch, c.getId());
        Utils.serialize(c, "./.gitlet/commits/" + c.getId() + ".ser");
    }

    /** Add a new commit to this commit tree with MESSAGE and TIME
     *  Helper function of commit command. */
    void addNewCommit(String message, Date time) {
        String currentId = getHead().getId();
        HashMap<String, String> currentFiles = getHead().getFileMap();
        Commit newCommit
            = new Commit(message, time, currentId, currentFiles);
        saveCommit(newCommit);
    }

    /** Add a BRANCH to this commitTree. */
    void addBranch(String branch) {
        if (_branches.containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        _branches.put(branch, getHeadId());
    }

    /** Switch the head to BRANCH. */
    void switchBranch(String branch) {
        if (_branches.containsKey(branch)) {
            _activeBranch = branch;
        } else {
            System.out.println("No such branch exists.");
        }
    }

    /** Remove branch with name BRANCH from this CommitTree. */
    void removeBranch(String branch) {
        if (!_branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
        } else if (_activeBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            _branches.remove(branch);
        }
    }

    /** Return a commit based on ID. */
    Commit findCommit(String id) {
        return (Commit) Utils.deserialize("./.gitlet/commits/" + id + ".ser");
    }

    /** Return name of the current branch. */
    String getActiveBranch() {
        return _activeBranch;
    }

    /** Return a set of all existing branch names, ordered by branch names. */
    TreeSet<String> getAllBranches() {
        TreeSet<String> orderedB = new TreeSet<String>();
        for (String b : _branches.keySet()) {
            orderedB.add(b);
        }
        return orderedB;
    }

    /** Return the head commit of BRANCH. */
    Commit getBranchHead(String branch) {
        String id = _branches.get(branch);
        return (Commit) Utils.deserialize("./.gitlet/commits/" + id + ".ser");
    }

    /** Reset the head of BRANCH to COMMIT. */
    void resetBranchHead(String branch, Commit commit) {
        _branches.put(branch, commit.getId());
    }

    /** Return the earliest common ancestor of the current branch and
     *  BRANCH. */
    Commit findSplitPoint(String branch) {
        Commit c1 = getHead();
        Commit c2 = getBranchHead(branch);
        while (!c1.getId().equals(c2.getId())) {
            Date d1 = c1.getTime();
            Date d2 = c2.getTime();
            if (d1.before(d2)) {
                c2 = (Commit) Utils.deserialize("./.gitlet/commits/"
                                                + c2.getParentID() + ".ser");
            } else {
                c1 = (Commit) Utils.deserialize("./.gitlet/commits/"
                                                + c1.getParentID() + ".ser");
            }
        }
        return c1;
    }

}
