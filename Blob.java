package gitlet;

import java.io.File;
import java.io.Serializable;

/** This class simply is the representation for the content within a file.
 * @author Parth Shisode */
public class Blob implements Serializable {

    /** BLOB_FOLDER is a folder holding Blobs in /.gitlet. */
    static final File BLOB_FOLDER = Utils.join(Main.DOT_GITLET, "blobs");

    /** Create a Blob with these parameters.
     * @param f FILE */
    public Blob(File f) {
        _info = Utils.readContents(f);
        _bSha = Utils.sha1(_info);
        _blobFile = f;

        boolean c1 = Utils.plainFilenamesIn(BLOB_FOLDER) == null;

        boolean saveCond = true;
        if (c1) {
            for (String blobID : Utils.plainFilenamesIn(BLOB_FOLDER)) {
                Blob currB = Utils.readObject(Utils.join(BLOB_FOLDER,
                        blobID), Blob.class);
                if (currB.getSha().equals(_bSha)
                        && currB.getBlobFile().equals(_blobFile)) {
                    saveCond = false;
                }
            }
        }
        if (saveCond) {
            saveBlob();
        }
    }

    /** Creates a Blob within BLOB_FOLDER. */
    public void saveBlob() {
        File blobFile = Utils.join(BLOB_FOLDER, _bSha);
        Utils.writeObject(blobFile, this);
    }

    /** @return byte[]
     *  Getter method for _info. */
    public byte[] getInfo() {
        return _info;
    }

    /** @return STRING
     * Getter method for _bSha. */
    public String getSha() {
        return _bSha;
    }

    /** @return FILE
     * Getter method for _blobFile. */
    public File getBlobFile() {
        return _blobFile;
    }

    /** Represents the SHA1 hash string associated with the Blob's file. */
    private final String _bSha;

    /** Represents the actual contents of the Blob's file as a byte[]. */
    private final byte[] _info;

    /** Represents the File this Blob is associated with. */
    private final File _blobFile;
}
