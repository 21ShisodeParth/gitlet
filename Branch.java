package gitlet;
import java.io.File;
import java.io.Serializable;

/**This class represents a branch head, which actually is a pointer to
 * a CommitObj.
 * @author Parth Shisode */
public class Branch implements Serializable {
    /** Folder holding Branches in /.gitlet. */
    static final File BRANCH_FOLDER = Utils.join(Main.DOT_GITLET, "branches");
    /**
     * Creates a new CommitObj with these parameters.
     * @param name String
     * @param commitSha String
     */
    public Branch(String name, String commitSha) {
        _name = name;
        _commitSha = commitSha;

        boolean c1 = Utils.plainFilenamesIn(BRANCH_FOLDER) == null;
        if (c1 || !Utils.plainFilenamesIn(BRANCH_FOLDER).contains(_name)) {
            saveBranch();
        }
    }

    /** @param newCom CommitObj
     * Reassigns the CommitObj SHA value. */
    public void reSha(CommitObj newCom) {
        _commitSha = newCom.getSha();
    }

    /** @return STRING
     * Getter method for _name. */
    public String getName() {
        return _name;
    }

    /** @return STRING
     * Getter method for _commitSha. */
    public String getCommitSha() {
        return _commitSha;
    }

    /** Creates a Branch within BRANCH_FOLDER. */
    public void saveBranch() {
        File branchFile = Utils.join(BRANCH_FOLDER, _name);
        Utils.writeObject(branchFile, this);
    }

    /** The _name of the Branch, assigned by the user. */
    private String _name;

    /** The SHA value of the CommitObj which this branch points to. */
    private String _commitSha;

}
