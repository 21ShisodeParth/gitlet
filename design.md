# Gitlet Design Document
author: Parth Shisode

## 1. Classes and Data Structures

### Main.java
This class contains all the logic for the commands that a user could input through a terminal.

#### Fields
1. Branch _currBr represents the pointer of the most recently added CommitObj.
2. File G_FOLDER represents the most general, outermost working directory.
3. File S_ADD_FOLDER represents the files in the staging area to be added with commit().
4. File S_REM_FOLDER represents the files in the staging area to be removed with commit().

### Blob.java
This class simply is the representation for the content within a file.
* Blob.java MUST implement Serializable.

#### Fields
1. String _bSha represents the "name" of this blob, represented by a SHA1 value.
2. byte[] _info is the File information of this Blob, which can be serialized and deserialized.

### Branch.java
This class represents a pointer to a CommitObj, but will use a _commitID instead.

#### Fields
1. String _name is the name which the user has given a branch, aside from the master branch which is created with init().
2. String _commitSha is the SHA value of the current CommitObj which this branch is associated with.

### CommitObj.java 
This class is the representation for a commit, which would be created by the 
commit() function described elsewhere in this design doc. 
* Blob.java MUST implement Serializable.

#### Fields
1. File COMMIT_FOLDER
2. String _parent1Sha represents the parent of this commit.
3. String _parent2Sha represents the parent of this commit.
4. String _sha would be the "name" of this commit, represented by its SHA1 value.
5. TreeMap<File, String> _blobMap represents the set of respective files and blob SHA1 values contained within 
this CommitObj.
6. String _logMes is the log message inputted when
7. String _date represents the date and time which the commit took place.

## 2. Algorithms

### Main.java
1. main(String args[]): Responsible for handling user input, and what function(options listed below)
is called as a result. Calls the persist() function to read in serialized data.
2. init(): Creates a brand new version control system with an initial commit. Since this is called
through the main() function, there is no need to actually create the directories, since this is done
persist.
3. add(String fileName): Responsible for placing a file in the staging area to be added, interacting with
the S_ADD_FOLDER inside the Main class.
4. commit(String message): Creates a new CommitObj with its respective data and message, then saves this
object within CommitObj.COMMIT_FOLDER using the saveCommit() function.
5. rm(String fileName): Responsible for placing a file in the staging area to be removed, interacting with
the S_REMOVE_FOLDER inside the Main class.
6. log(): Prints out the information of the head commit as well as that of all the parent commits. For this,
the getter methods created within the CommitObj class are going to be very useful. 
7. globalLog(): Prints out the information of EVERY commit ever created, using the getter methods within
the CommitObj class. In order to access every commit created, the plainFilenamesIn() function will be used
on CommitObj.COMMIT_FOLDER.
8. find(String message): Will print every SHA1 ID of the commits with the given message. The getter method
for the _sha and _message variables of the CommitObj class will be used in order to achieve this.
9. status(): Will display all current branches, 
10. checkout():
- checkout(String dashes, String fileName): I would simply access the head branch by traversing the BRANCH_FOLDER,
then looking for a name with a star in front of it, then use Utils.writeObject() to save this to my working
directory.
- checkout(String commitID, String dashes, String fileName): I would use a very similar process to the previous
version of checkout, but this time, traverse the CommitObj.COMMIT_FOLDER class instead; after, I'd be using the
Utils.writeObject() method to write this to my directory.
- checkout(String brName): This would take every file within the commit of the head branch, which is a TreeMap
object, then traverse Blob.BLOB_FOLDER to access its information, and then use Utils.writeObject in order to write 
the information from every file info the working directory.
11. branch(String brName): This creates a new Branch, which is pointed at the CommitObj that the current head Branch
is currently pointing at through its stored _commitSha.
12. rmBranch (String brName): Set Branch object equal to null, then go into working directory and delete File associated
with Branch.
13. reset (String commitID): call checkout using the second version, calling it on every single file which the CommitObj contains
in its TreeMap. Then, change the _commitSha of the branch of the head node using Branch.reSha(). Lastly,
 empty out all files from the directory S_ADD_FOLDER, which represents the staging area for additions.

### CommitObj.java
1. saveCommit(): Serialize this commit within the COMMIT_FOLDER within this class using writeObject().
2. TreeMap<File, String> createMap(): Create a TreeMap object which contains the information of the Files being stored
within this commit, as well as the SHA1 value of the Blob associated with this File and its contents. Traverse through the Main.S_ADD_FOLDER,
Main.G_FOLDER, and then Main.S_REM_FOLDER to decide what to actually save.
3. String getDate(): Return the _date that this commit was created, which was formed using Java.util.Date and Java.text.SimpleDateFormat.
4. String getMessage(): Return the _message that the user associated with this commit.
5. String getParent1Sha(): Return the _parent1Sha, which serves as a sort of pointer without actually having to store a pointer to a CommitObj.

### Blob.java
1. byte[] getInfo(): Returns the _info contained within this Blob, represented as a byte arrray.
2. String getSha(): Returns the _bSha of this Blob, which is its identifier.

### Branch.java
1. reSha(): Renames the SHA value of this Branch, which would be needed when pointing to a new CommitObj.
2. String getName(): Returns the _name that this branch was assigned.
3. String getCommitSha(): Returns the _commitSha of this branch, which can be used to find a commitNode.

## 3. Persistence
Rather than include persistence differently for every single command within this project, what I plan to do is create
a single persist() method within the Main class which will ensure that the following folders are already created:
- Main.S_ADD_FOLDER
- Main.S_REM_FOLDER
- Main.WORK_DIR
- CommitObj.COMMIT_FOLDER
- Branch.BRANCH_FOLDER
- Blob.BLOB_FOLDER

The persist() function may take in multiple Strings as its argument, which all represent the multiple directories or Files
which need to be serialized or saved. This will be used whenever entire directories would need to be serialized. 

Additionally, within the CommitObj and Blob class exist saveCommit() and
saveBlob() respectively, which are to be used whenever a new commit is created, such as with Main.commit()
or within the CommitObj class when a new Blob SHA1 value is saved to a CommitObj's TreeMap.
