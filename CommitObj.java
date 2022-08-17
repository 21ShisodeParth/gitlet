
package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.Date;
import java.text.SimpleDateFormat;

/** This class is the representation for a commit, which would be created by the
 * commit() function within Main.java.
 * @author Parth Shisode */
public class CommitObj implements Serializable {
    /** The folder holding commits in /.gitlet. */
    static final File COMMIT_FOLDER = Utils.join(Main.DOT_GITLET, "commits");

    /** Creates a new initial commit. */
    public CommitObj() {
        _message = "initial commit";
        _date = "Wed Dec 31 16:00:00 1969 -0800";
        _parent1Sha = null;
        _parent2Sha = null;
        _blobMap = new TreeMap<>();

        String s;
        s = Utils.sha1(_message, _date, _blobMap.toString());
        _cSha = s;

        saveCommit();
    }

    /** Creates a new CommitObj with these parameters.
     * @param message STRING
     * @param parent1Sha STRING
     * @param parent2Sha String[]. */
    public CommitObj(String message, String parent1Sha, String... parent2Sha) {
        _message = message;
        _parent1Sha = parent1Sha;
        if (parent2Sha.length == 1) {
            _parent2Sha = parent2Sha[0];
        } else {
            _parent2Sha = null;
        }
        _date = date();
        _blobMap = createMap();
        String s;
        s = Utils.sha1(_message, _date, _blobMap.toString(), _parent1Sha);
        _cSha = s;

        if (!Utils.plainFilenamesIn(COMMIT_FOLDER).contains(_cSha)) {
            saveCommit();
        }
    }

    /** @return TREEMAP<FILE, STRING>
     * ASSUMES that Main.S_ADD_FOLDER and WORK_DIR
     * and S_REM_FOLDER is NOT empty since a commit can
     * only occur if something is inside the staging area.
     * Create the _blobMap object.*/
    private TreeMap<File, String> createMap() {
        File cF = Utils.join(COMMIT_FOLDER, _parent1Sha);
        CommitObj par = Utils.readObject(cF, CommitObj.class);

        TreeMap<File, String> map = par.getBlobMap();
        File t; Blob b;

        for (String fName : Utils.plainFilenamesIn(Main.S_ADD_FOLDER)) {
            t = Utils.join(Main.S_ADD_FOLDER, fName);
            b = Utils.readObject(t, Blob.class);
            map.put(b.getBlobFile(), b.getSha());
        }

        for (String fName : Utils.plainFilenamesIn(Main.S_REM_FOLDER)) {
            map.remove(Utils.join(Main.WORK_DIR, fName));
        }
        return map;
    }

    /** @return STRING
     * Function for generating a new date-time. */
    private String date() {
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("E MMM dd HH:mm:ss y Z");
        return f.format(d);
    }

    /** Function for saving a CommitObj to COMMIT_FOLDER. */
    public void saveCommit() {
        File commitFile = Utils.join(COMMIT_FOLDER, _cSha);
        Utils.writeObject(commitFile, this);
    }

    /** @return TREEMAP<FILE, STRING>
     * Getter method for _blobMap. */
    public TreeMap<File, String> getBlobMap() {
        return _blobMap;
    }

    /** @return String
     * Getter method for _message. */
    public String getMessage() {
        return _message;
    }

    /** @return String
     * Getter method for _parent1Sha. */
    public String getParent1Sha() {
        return _parent1Sha;
    }

    /** @return String
     * Getter method for _cSha. */
    public String getSha() {
        return _cSha;
    }

    /** @return String
     * Getter method for _date. */
    public String getDate() {
        return _date;
    }

    /** @return String
     * Getter method for _parent2Sha. */
    public String getParent2Sha() {
        return _parent2Sha;
    }

    /** Represents the pairing between each file and content via _blobMap. */
    private final TreeMap<File, String> _blobMap;

    /** Is the commit message. */
    private final String _message;

    /** Is the SHA value with "commit_" at the start. */
    private final String _cSha;

    /** Is the SHA of parent1. */
    private final String _parent1Sha;

    /** Is the SHA of parent2. */
    private final String _parent2Sha;

    /** Is the date the commit was created. */
    private final String _date;
}
