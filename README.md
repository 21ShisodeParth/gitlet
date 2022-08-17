# Gitlet
Gitlet is a project for UC Berkeley's CS61B (Data Structures) course, where I had to build my own version-control system, modeled after the widely used Git system. For a brief description of every command Gitlet is capable of, scroll to the last section, "Gitlet Commands".

[Here's the full specification.](https://inst.eecs.berkeley.edu/~cs61b/sp22/materials/proj/proj3/index.html)

## Context
This project, unlike most projects in computer science courses, requires its students to fully design the architecture of their software. The only thing we received for this project were requirements of what our final product should be able to accomplish. The blueprint of our software and the data structures used to accomplish this task was left completely up to me.

A design document for this project can be found in this repository at [gitlet/design.md](https://github.com/21ShisodeParth/gitlet/blob/main/design.md). 

Here are some preliminary sketches of my design:
<img width="359" alt="gitlet-design1" src="https://user-images.githubusercontent.com/52021668/185258149-976f646e-005a-403b-954f-b1f1569cedc2.png">
<img width="359" alt="gitlet-design2" src="https://user-images.githubusercontent.com/52021668/185258156-91ef8e19-dcad-43fe-b6f1-e7466521a4bc.png">


## Gitlet Commands
The purpose of Gitlet is to have a full scale version-control system which is meant to work on your local computer. 

Here are some of the commands (descriptions taken from spec. above):

![pg1](C:\Users\parth\Desktop\cs61a\projects\gitlet-design1.png)

### init
Creates a new Gitlet version-control system in the current directory.

### add
Adds a copy of the file as it currently exists to the staging area. For this reason, adding a file is also called staging the file for addition. Staging an already-staged file overwrites the previous entry in the staging area with the new contents.

### commit
Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files.

### rm
Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so.

### log
Starting at the current head commit, displays information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits.

### global-log
Like log, except displays information about all commits ever made. The order of the commits does not matter.

### find
Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines.

### status
Displays what branches currently exist, and marks the current branch with a \*. Also displays what files have been staged for addition or removal.

### checkout
This function has 3 different use-cases depending on the number of inputs.
1. Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
3. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist.

### branch
Creates a new branch with the given name, and points it at the current head node. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. 

### rm-branch
Deletes the branch with the given name. This only means to delete the pointer associated with the branch

### reset
Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit.

### merge
Merges files from the given branch into the current branch. This method requires a great, great deal of explanation, so please check out the [full description here](https://inst.eecs.berkeley.edu/~cs61b/sp22/materials/proj/proj3/merge.html).
