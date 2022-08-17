package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.TreeSet;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Parth Shisode */
public class Main {
    /** The working directory. */
    static final File WORK_DIR = new File(".");

    /** Represents /.gitlet. */
    static final File DOT_GITLET = Utils.join(WORK_DIR, ".gitlet");

    /** The staging area for addition. */
    static final File S_ADD_FOLDER = Utils.join(DOT_GITLET, "stage_add");

    /** The staging area for removal. */
    static final File S_REM_FOLDER = Utils.join(DOT_GITLET, "stage_rem");

    /** A copy of the Branch of the current commit. */
    private static Branch _currBranch = null;

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  @param args Array.*/
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command."); System.exit(0);
        }
        if (!args[0].equals("init") && !DOT_GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        persist();
        switch (args[0]) {
        case "init":
            init(); break;
        case "add":
            add(args[1]); break;
        case "commit":
            if (args.length < 2) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            commit(args[1]); break;
        case "rm":
            rm(args[1]); break;
        case "log":
            log(); break;
        case "global-log":
            globalLog(); break;
        case "find":
            find(args[1]); break;
        case "status":
            status(); break;
        case "checkout":
            if (args.length == 2) {
                checkout(args[1]);
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands."); System.exit(0);
                }
                checkout(args[1], args[2]);
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands."); System.exit(0);
                }
                checkout(args[1], args[2], args[3]);
            }
            break;
        case "branch":
            branch(args[1]); break;
        case "rm-branch":
            rmBranch(args[1]); break;
        case "reset":
            reset(args[1]); break;
        case "merge":
            merge(args[1]); break;
        default:
            System.out.println("No command with that name exists."); break;
        }
        Utils.writeObject(Utils.join(Branch.BRANCH_FOLDER, "CURR_BRANCH"),
                _currBranch);
        correctCurr();
    }

    /** Creates a new Gitlet version-control system in the
     * current directory. This system will automatically start with one
     * commit: a commit that contains no files and has the commit
     * message "initial commit". */
    public static void init() {
        if (!isEmpty(CommitObj.COMMIT_FOLDER)) {
            System.out.println("A Gitlet version-control"
                    + " system already exists in the current directory.");
            System.exit(0);
        }

        CommitObj initial = new CommitObj();
        _currBranch = new Branch("master", initial.getSha());
    }

    /** @param fileName STRING
     * Adds a copy of the file as it currently exists to the staging area. */
    public static void add(String fileName) {
        File t1 = Utils.join(WORK_DIR, fileName);
        if (!t1.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        File cF = Utils.join(CommitObj.COMMIT_FOLDER,
                _currBranch.getCommitSha());
        CommitObj c = Utils.readObject(cF, CommitObj.class);
        byte[] fileCont = Utils.readContents(t1);

        if (Objects.equals(c.getBlobMap().get(t1), (Utils.sha1(fileCont)))) {
            File t; Blob b;
            for (String dirName : Utils.plainFilenamesIn(S_ADD_FOLDER)) {
                t = Utils.join(S_ADD_FOLDER, dirName);
                if (t.exists()) {
                    b = Utils.readObject(t, Blob.class);
                    if (b.getBlobFile().getName().equals(fileName)) {
                        t.delete();
                    }
                }
            }
        } else {
            Blob addBlob = new Blob(t1);
            File t2 = Utils.join(S_ADD_FOLDER, addBlob.getSha());
            Utils.writeObject(t2, addBlob);
        }

        File remF = Utils.join(S_REM_FOLDER, fileName);
        remF.delete();
    }

    /** @param message STRING
     * @param parent2Sha STRING[]
     * Saves a snapshot of tracked files in the current commit and staging
     * area so they can be restored at a later time, creating a new commit. */
    public static void commit(String message, String... parent2Sha) {
        if (isEmpty(S_ADD_FOLDER) && isEmpty(S_REM_FOLDER)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);

        }
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        CommitObj newCom;
        if (parent2Sha.length == 1) {
            newCom = new CommitObj(message,
                    _currBranch.getCommitSha(), parent2Sha);
        } else {
            newCom = new CommitObj(message, _currBranch.getCommitSha());
        }
        _currBranch.reSha(newCom);

        if (!isEmpty(S_ADD_FOLDER)) {
            for (File f : S_ADD_FOLDER.listFiles()) {
                f.delete();
            }
        }
        if (!isEmpty(S_REM_FOLDER)) {
            for (File f : S_REM_FOLDER.listFiles()) {
                f.delete();
            }
        }
    }

    /** @param fileName String
     * Unstage the file if it is currently staged for addition. If the
     *  file is tracked in the current commit, stage it for removal and
     *  remove the file from the working directory if the user has not
     *  already done so. */
    public static void rm(String fileName) throws IOException {
        File fAdd = new File("dummy");

        File fWor = Utils.join(WORK_DIR, fileName);

        for (String fName : Utils.plainFilenamesIn(S_ADD_FOLDER)) {
            File bF = Utils.join(Blob.BLOB_FOLDER, fName);
            Blob b = Utils.readObject(bF, Blob.class);
            if (b.getBlobFile().equals(fWor)) {
                fAdd = Utils.join(S_ADD_FOLDER, b.getSha());
                break;
            }
        }

        File fCom = Utils.join(CommitObj.COMMIT_FOLDER,
                _currBranch.getCommitSha());
        File fREM = Utils.join(S_REM_FOLDER, fileName);
        CommitObj c = Utils.readObject(fCom, CommitObj.class);

        if (fAdd.equals(new File("dummy"))
                && c.getBlobMap().get(fWor) == null) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (fAdd.exists()) {
            fAdd.delete();
        }

        if (c.getBlobMap().get(fWor) != null) {
            fREM.createNewFile();
            if (fWor.exists()) {
                fWor.delete();
            }
        }
    }

    /** Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents
     * found in merge commits. */
    public static void log() {
        File f = Utils.join(CommitObj.COMMIT_FOLDER,
                _currBranch.getCommitSha());
        CommitObj currCom = Utils.readObject(f, CommitObj.class);
        logHelper(currCom);
    }

    public static void logHelper(CommitObj c) {
        System.out.print("===" + "\n"
                + "commit " + c.getSha() + "\n"
                + "Date: " + c.getDate() + "\n"
                + c.getMessage() + "\n" + "\n");

        if (c.getParent1Sha() != null) {
            File f = Utils.join(CommitObj.COMMIT_FOLDER, c.getParent1Sha());
            CommitObj p = Utils.readObject(f, CommitObj.class);
            logHelper(p);
        }
    }

    /** Like log, except displays information about all commits ever made. */
    public static void globalLog() {
        File comF;
        for (String name : Utils.plainFilenamesIn(CommitObj.COMMIT_FOLDER)) {
            comF = Utils.join(CommitObj.COMMIT_FOLDER, name);
            CommitObj c = Utils.readObject(comF, CommitObj.class);
            System.out.print("===" + "\n"
                    + "commit " + c.getSha() + "\n"
                    + "Date: " + c.getDate() + "\n"
                    + c.getMessage() + "\n" + "\n");
        }
    }

    /** @param m STRING
     * Prints out the ids of all commits that have the given commit
     * message, one per line. */
    public static void find(String m) {
        boolean found = false;
        if (CommitObj.COMMIT_FOLDER.list().length > 0) {
            for (String n : Utils.plainFilenamesIn(CommitObj.COMMIT_FOLDER)) {
                File f = Utils.join(CommitObj.COMMIT_FOLDER, n);
                CommitObj c = Utils.readObject(f, CommitObj.class);
                if (c.getMessage().equals(m)) {
                    System.out.println(c.getSha());
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Displays what branches currently exist, and marks the current
     * branch with a *. */
    public static void status() {
        List<String> s;

        System.out.println("=== Branches ===");
        s = Utils.plainFilenamesIn(Branch.BRANCH_FOLDER);
        s.sort(Comparator.naturalOrder());
        for (String bName : s) {
            Branch b = Utils.readObject(
                    Utils.join(Branch.BRANCH_FOLDER, bName), Branch.class);
            if (!bName.equals("CURR_BRANCH")) {
                if (_currBranch.getName().equals(b.getName())) {
                    System.out.print("*");
                }
                System.out.println(bName);
            }
        }

        System.out.println("\n" + "=== Staged Files ===");
        ArrayList<String> sF = new ArrayList<>();
        s = Utils.plainFilenamesIn(S_ADD_FOLDER);
        for (String sha : s) {
            File bF = Utils.join(Blob.BLOB_FOLDER, sha);
            Blob b = Utils.readObject(bF, Blob.class);
            sF.add(b.getBlobFile().getName());
        }
        sF.sort(Comparator.naturalOrder());
        for (String fName : sF) {
            System.out.println(fName);
        }

        System.out.println("\n" + "=== Removed Files ===");
        s = Utils.plainFilenamesIn(S_REM_FOLDER);
        s.sort(Comparator.naturalOrder());
        for (String fName : s) {
            System.out.println(fName);
        }

        System.out.println("\n"
                + "=== Modifications Not Staged For Commit ===");

        System.out.println("\n" + "=== Untracked Files ===");

    }

    /** @param dashes STRING
     * @param fileName STRING
     * 1. Takes the version of the file as it exists in the head commit,
     * the front of the current branch, and puts it in the working directory,
     * overwriting the version of the file that's already there if there
     * is one. */
    public static void checkout(String dashes, String fileName) {
        checkout(_currBranch.getCommitSha(), "--", fileName);
    }

    /** @param comID STRING
     * @param dashes STRING
     * @param fileName STRING
     * 2. Takes the version of the file as it exists in the commit with the
     * given id, and puts it in the working directory, overwriting the version
     * of the file that's already there if there is one. */
    public static void checkout(String comID, String dashes, String fileName) {
        String usefulID;
        if (comID.length() <= 8) {
            usefulID = "not going to exist";
            for (String cN : Utils.plainFilenamesIn(CommitObj.COMMIT_FOLDER)) {
                if (comID.equals(cN.substring(0, 8))) {
                    usefulID = cN;
                }
            }
        } else {
            usefulID = comID;
        }

        File comFile = Utils.join(CommitObj.COMMIT_FOLDER, usefulID);

        if (!comFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        CommitObj c = Utils.readObject(comFile, CommitObj.class);

        File extFile = Utils.join(WORK_DIR, fileName);
        if (c.getBlobMap().get(extFile) == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobSha = c.getBlobMap().get(extFile);

        File blobFile = Utils.join(Blob.BLOB_FOLDER, blobSha);
        Blob b = Utils.readObject(blobFile, Blob.class);

        Utils.writeContents(extFile, b.getInfo());
    }

    /** @param brName STRING
     * 3.Takes all files in the commit at the head of the given branch, and
     * puts them in the working directory, overwriting the versions of the
     * files that are already there if they exist. */
    public static void checkout(String brName) {
        File brF = Utils.join(Branch.BRANCH_FOLDER, brName);
        if (!brF.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Branch br = Utils.readObject(brF, Branch.class);
        if (_currBranch.getCommitSha().equals(br.getCommitSha())
                && _currBranch.getName().equals(br.getName())) {

            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        String comID = br.getCommitSha();
        File f = Utils.join(CommitObj.COMMIT_FOLDER, comID);
        CommitObj c = Utils.readObject(f, CommitObj.class);
        String currComID = _currBranch.getCommitSha();
        File fCurr = Utils.join(CommitObj.COMMIT_FOLDER, currComID);
        CommitObj currC = Utils.readObject(fCurr, CommitObj.class);
        TreeMap<File, String> currMap = currC.getBlobMap();
        TreeMap<File, String> map = c.getBlobMap();
        String resBlob;

        for (Map.Entry<File, String> e : map.entrySet()) {
            resBlob = e.getValue();
            if (currMap.get(e.getKey()) == null) {
                if (!e.getKey().exists()) {
                    File bF = Utils.join(Blob.BLOB_FOLDER, e.getValue());
                    Blob b = Utils.readObject(bF, Blob.class);
                    Utils.writeObject(e.getKey(), b.getInfo());
                } else {
                    byte[] worCont = Utils.readContents(e.getKey());
                    byte[] blobCont = Utils.readObject(
                            Utils.join(Blob.BLOB_FOLDER, resBlob),
                            Blob.class).getInfo();
                    if (!Arrays.equals(worCont, blobCont)) {
                        System.out.println("There is an untracked file in"
                                + " the way; delete it, or add and"
                                + " commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
        for (Map.Entry<File, String> e : map.entrySet()) {
            checkout(comID, "--", e.getKey().getName());
        }
        for (Map.Entry<File, String> e : currMap.entrySet()) {
            if (map.get(e.getKey()) == null) {
                e.getKey().delete();
            }
        }
        for (String fName : Utils.plainFilenamesIn(S_ADD_FOLDER)) {
            File fAdd = Utils.join(S_ADD_FOLDER, fName);
            fAdd.delete();
        }

        _currBranch = br;
    }

    /** @param brName String
     * Creates a new branch with the given name, and points it at the
    * current head node. */
    public static void branch(String brName) {
        File f = Utils.join(Branch.BRANCH_FOLDER, brName);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        new Branch(brName, _currBranch.getCommitSha());
    }

    /** @param brName STRING
     * Deletes the branch with the given name. */
    public static void rmBranch(String brName) {
        if (_currBranch.getName().equals(brName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File bF = Utils.join(Branch.BRANCH_FOLDER, brName);
        if (!bF.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else {
            bF.delete();
        }
    }

    /** @param comID STRING
     * Checks out all the files tracked by the given commit.
     *  Removes tracked files that are not present in that commit.
     *  Also moves the current branch's head to that commit node. */
    public static void reset(String comID) {
        File f = Utils.join(CommitObj.COMMIT_FOLDER, comID);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        CommitObj currCom = Utils.readObject(Utils.join
                (CommitObj.COMMIT_FOLDER, _currBranch.getCommitSha()),
                CommitObj.class);
        CommitObj resCom = Utils.readObject(Utils.join(
                CommitObj.COMMIT_FOLDER, comID), CommitObj.class);

        Map<File, String> map = resCom.getBlobMap();


        String resBlob;
        for (Map.Entry<File, String> e : map.entrySet()) {
            resBlob = e.getValue();
            if (currCom.getBlobMap().get(e.getKey()) == null
                    && e.getKey().exists()) {
                byte[] worCont = Utils.readContents(e.getKey());
                byte[] blobCont = Utils.readObject(Utils.join
                        (Blob.BLOB_FOLDER, resBlob), Blob.class).getInfo();
                if (!Arrays.equals(worCont, blobCont)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            checkout(comID, "--", e.getKey().getName());
        }

        File f1;
        for (String fN : Utils.plainFilenamesIn(S_ADD_FOLDER)) {
            f1 = Utils.join(S_ADD_FOLDER, fN);
            f1.delete();
        }

        for (String fN : Utils.plainFilenamesIn(S_REM_FOLDER)) {
            f1 = Utils.join(S_REM_FOLDER, fN);
            f1.delete();
        }

        File assocBrF = Utils.join(Branch.BRANCH_FOLDER,
                _currBranch.getName());
        Branch assocBr = Utils.readObject(assocBrF, Branch.class);
        assocBr.reSha(Utils.readObject(f, CommitObj.class));

        _currBranch = assocBr;
    }

    /** @param brName STRING
     * Merges files from the given branch into the current branch. */
    public static void merge(String brName) throws IOException {
        if (!Utils.join(Branch.BRANCH_FOLDER, brName).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        CommitObj spCom = Utils.readObject(Utils.join(CommitObj.COMMIT_FOLDER,
                splitPoint(brName)), CommitObj.class);
        Branch givBr = Utils.readObject(Utils.join
                (Branch.BRANCH_FOLDER, brName), Branch.class);
        CommitObj givCom = Utils.readObject(Utils.join(CommitObj.COMMIT_FOLDER,
                givBr.getCommitSha()), CommitObj.class);
        if (brName.equals(_currBranch.getName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (!isEmpty(S_ADD_FOLDER) || !isEmpty(S_REM_FOLDER)) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (givCom.getSha().equals(spCom.getSha())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            System.exit(0);
        }
        Branch currBr = Utils.readObject(Utils.join
                (Branch.BRANCH_FOLDER, _currBranch.getName()), Branch.class);
        CommitObj currCom = Utils.readObject(
                Utils.join(CommitObj.COMMIT_FOLDER,
                currBr.getCommitSha()), CommitObj.class);
        if (currCom.getSha().equals(spCom.getSha())) {
            checkout(brName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        TreeMap<File, String> givMap = givCom.getBlobMap();
        TreeMap<File, String> currMap = currCom.getBlobMap();
        TreeMap<File, String> spMap = spCom.getBlobMap();
        Set<File> files = new TreeSet<>(); files.addAll(givMap.keySet());
        files.addAll(currMap.keySet()); files.addAll(spMap.keySet());
        for (File f : files) {
            String givBlob = givMap.get(f); String currBlob = currMap.get(f);
            String spBlob = spMap.get(f); if (spBlob == null && givBlob != null
                    && currBlob == null) {
                Blob gB = Utils.readObject(Utils.join
                        (Blob.BLOB_FOLDER, givBlob), Blob.class);
                if (!currMap.containsKey(f)) {
                    if (f.exists() && !gB.getInfo().equals
                            (Utils.readContents(f))) {
                        System.out.println("There is an untracked file"
                                + " in the way; delete it, or add and commit"
                                + " it first.");
                        System.exit(0);
                    }
                }
            }
        }
        checkConds(givCom, files, spMap, currMap, givMap);
        commit("Merged " + brName + " into " + _currBranch.getName() + ".",
                givBr.getCommitSha());
    }

    /** @param givCom
     * @param files
     * @param spMap
     * @param currMap
     * @param givMap
     * @throws IOException
     * Helper method to check merge scenarios.
     */
    public static void checkConds(CommitObj givCom, Set<File> files,
                                  TreeMap<File, String> spMap,
                                  TreeMap<File, String> currMap,
                                  TreeMap<File, String> givMap)
            throws IOException {
        for (File f : files) {
            String givBlob = givMap.get(f); String currBlob = currMap.get(f);
            String spBlob = spMap.get(f);
            if (spBlob == null && givBlob == null
                    && currBlob != null) {
                continue;
            }
            if (spBlob == null && givBlob != null
                    && currBlob == null) {
                checkout(givCom.getSha(), "--", f.getName()); add(f.getName());
                continue;
            }
            if (spBlob != null && givBlob == null
                    && currBlob != null && spBlob.equals(currBlob)) {
                rm(f.getName());
                continue;
            }
            if (spBlob != null && givBlob == null
                    && currBlob == null) {
                continue;
            }
            if (mergeConflict(f, spBlob, currBlob, givBlob)) {
                add(f.getName());
                continue;
            }
            if (spBlob != null && givBlob != null
                    && currBlob == null) {
                continue;
            }
            if (!givBlob.equals(spBlob) && currBlob.equals(spBlob)) {
                checkout(givCom.getSha(), "--", f.getName()); add(f.getName());
                continue;
            }
            if (givBlob.equals(spBlob) && !currBlob.equals(spBlob)) {
                continue;
            }
            if (!givBlob.equals(spBlob) && !currBlob.equals(spBlob)
                    && givBlob.equals(currBlob)) {
                continue;
            }
        }
    }

    /** @return boolean
     * @param f FILE
     * @param spBlob STRING
     * @param currBlob STRING
     * @param givBlob STRING
     * Helper function to handle conficts for merge. */
    public static boolean mergeConflict(File f, String spBlob,
                                     String currBlob, String givBlob) {
        Blob cB; Blob gB;

        if (spBlob == null && !givBlob.equals(currBlob)) {

            cB = Utils.readObject(Utils.join
                    (Blob.BLOB_FOLDER, currBlob), Blob.class);
            gB = Utils.readObject(Utils.join
                    (Blob.BLOB_FOLDER, givBlob), Blob.class);

            Utils.writeContents(f, "<<<<<<< HEAD\n"
                    + new String(cB.getInfo())
                    + "=======\n"
                    + new String(gB.getInfo())
                    + ">>>>>>>\n");

            System.out.println("Encountered a merge conflict.");
            return true;
        }

        if (currBlob == null && !spBlob.equals(givBlob)) {
            gB = Utils.readObject(Utils.join
                    (Blob.BLOB_FOLDER, givBlob), Blob.class);
            Utils.writeContents(f, "<<<<<<< HEAD\n"
                    + "=======\n"
                    + new String(gB.getInfo())
                    + ">>>>>>>\n");
            System.out.println("Encountered a merge conflict.");
            return true;
        }

        if (givBlob == null && !spBlob.equals(currBlob)) {
            cB = Utils.readObject(Utils.join
                    (Blob.BLOB_FOLDER, currBlob), Blob.class);
            Utils.writeContents(f, "<<<<<<< HEAD\n"
                    + new String(cB.getInfo())
                    + "=======\n"
                    + ">>>>>>>\n");
            System.out.println("Encountered a merge conflict.");
            return true;
        }

        if (!givBlob.equals(currBlob) && !spBlob.equals(givBlob)
                && !spBlob.equals(currBlob)) {
            cB = Utils.readObject(Utils.join
                    (Blob.BLOB_FOLDER, currBlob), Blob.class);
            gB = Utils.readObject(Utils.join
                    (Blob.BLOB_FOLDER, givBlob), Blob.class);

            Utils.writeContents(f, "<<<<<<< HEAD\n"
                    + new String(cB.getInfo())
                    + "=======\n"
                    + new String(gB.getInfo())
                    + ">>>>>>>\n");

            System.out.println("Encountered a merge conflict.");
            return true;
        }
        return false;
    }

    /** @param br STRING
     * Two BFS searches to return the splitPoint. */
    public static String splitPoint(String br) {
        ArrayDeque<String> q = new ArrayDeque<>();
        ArrayList<String> givHist = new ArrayList<>();
        ArrayList<String> currHist = new ArrayList<>();
        Branch b = Utils.readObject(Utils.join(Branch.BRANCH_FOLDER, br),
                Branch.class);
        CommitObj c = Utils.readObject(Utils.join(CommitObj.COMMIT_FOLDER,
                b.getCommitSha()), CommitObj.class);
        givHist.add(c.getSha());
        if (c.getParent1Sha() != null) {
            q.add(c.getParent1Sha());
        }
        if (c.getParent2Sha() != null) {
            q.add(c.getParent2Sha());
        }
        while (!q.isEmpty()) {
            String curr = q.pop();
            if (givHist.contains(curr)) {
                continue;
            }
            givHist.add(curr);
            c = Utils.readObject(Utils.join
                    (CommitObj.COMMIT_FOLDER, curr), CommitObj.class);
            if (c.getParent1Sha() != null) {
                q.add(c.getParent1Sha());
            }
            if (c.getParent2Sha() != null) {
                q.add(c.getParent2Sha());
            }
        }
        c = Utils.readObject(Utils.join(CommitObj.COMMIT_FOLDER,
                _currBranch.getCommitSha()), CommitObj.class);
        currHist.add(c.getSha());
        if (c.getParent1Sha() != null) {
            q.add(c.getParent1Sha());
        }
        if (c.getParent2Sha() != null) {
            q.add(c.getParent2Sha());
        }
        while (!q.isEmpty()) {
            String curr = q.pop();
            if (currHist.contains(curr)) {
                continue;
            }
            currHist.add(curr);
            c = Utils.readObject(Utils.join
                    (CommitObj.COMMIT_FOLDER, curr), CommitObj.class);
            if (c.getParent1Sha() != null) {
                q.add(c.getParent1Sha());
            }
            if (c.getParent2Sha() != null) {
                q.add(c.getParent2Sha());
            }
        }

        currHist.retainAll(givHist);

        return currHist.get(0);
    }


    /** Takes care of all serialization processes which occur. */
    public static void persist() throws IOException {
        if (Utils.join(Branch.BRANCH_FOLDER, "CURR_BRANCH").exists()) {
            _currBranch = Utils.readObject(Utils.join
                    (Branch.BRANCH_FOLDER, "CURR_BRANCH"), Branch.class);
        }

        if (!S_ADD_FOLDER.exists()) {
            S_ADD_FOLDER.mkdir();
        }
        if (!DOT_GITLET.exists()) {
            DOT_GITLET.mkdir();
        }
        if (!S_REM_FOLDER.exists()) {
            S_REM_FOLDER.mkdir();
        }
        if (!S_ADD_FOLDER.exists()) {
            S_ADD_FOLDER.mkdir();
        }
        if (!WORK_DIR.exists()) {
            WORK_DIR.mkdir();
        }
        if (!CommitObj.COMMIT_FOLDER.exists()) {
            CommitObj.COMMIT_FOLDER.mkdir();
        }
        if (!Blob.BLOB_FOLDER.exists()) {
            Blob.BLOB_FOLDER.mkdir();
        }
        if (!Branch.BRANCH_FOLDER.exists()) {
            Branch.BRANCH_FOLDER.mkdir();
        }
        if (!Branch.BRANCH_FOLDER.exists()) {
            Branch.BRANCH_FOLDER.mkdir();
        }
    }

    /** @param dir File
     * Returns whether file is empty or not. */
    public static boolean isEmpty(File dir) {
        return dir.list().length == 0;
    }

    /** Attempts to correct differences between
     * CURR_BRANCH and associated branch. */
    public static void correctCurr() {
        if (_currBranch != null) {
            File bF = Utils.join(Branch.BRANCH_FOLDER, _currBranch.getName());
            Branch assocB = Utils.readObject(bF, Branch.class);
            if (!_currBranch.getCommitSha().equals(assocB.getCommitSha())) {
                CommitObj c = Utils.readObject(Utils.join(
                        CommitObj.COMMIT_FOLDER, _currBranch.getCommitSha()),
                        CommitObj.class);
                assocB.reSha(c);
                Utils.writeObject(Utils.join(Branch.BRANCH_FOLDER,
                        assocB.getName()), assocB);
            }
        }
    }
}
