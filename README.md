# iSpiEFP (I Spy  Effective Fragment Potential)
___
iSpiEFP is a tool for visualizing and describing molecular systems with the EFP method. iSpiEFP comes complete with a public database full of EFP Parameter files, and missing parameters can be calculated using Gamess. 
This application serves as a job-workflow manager which binds different technologies into one single application that allows chemists to point and click while utilizing high performance computing. 

### Latest Release
For patch notes on the latest release: iSpiEFP Version 0.5.6
 - https://github.com/iSpiEFP/iSpiEFP_GUI/releases/tag/0.5.6
 
### Help
For help on using iSpiEFP visit the website that Yen has made.<sorry I am missing that right now> (Release Data: TBA)

---
 
### Chemistry Technologies
iSpiEFP uses many different chemistry packages for visualizing and computing:
#### Jmol
Jmol: an open-source browser-based HTML5 viewer and stand-alone Java viewer for chemical structures in 3D with features for molecules, crystals, materials, and biomolecules.
For more information visit: http://jmol.sourceforge.net/
#### GAMESS
The General Atomic and Molecular Electronic Structure System (GAMESS) is a general ab initio quantum chemistry package. GAMESS is maintained by the members of the Gordon research group at Iowa State University. For more information visit: https://www.msg.chem.iastate.edu/gamess/
#### LibEFP
LibEFP is a full implementation of the Effective Fragment Potential (EFP method). LibEFP facilitates extension of unique electronic structure methodologies designed for accurate simulations in the gas phase to condensed phases via QM/EFP. LibEFP is designed to give developers of quantum chemistry software an easy way to add EFP support to their favorite package. For more information visit: http://carlosborca.com/libefp.html

---


### The Slipchenko Group
iSpiEFP is brought to you by the Slipchenko Theory Group at Purdue University. For more information on what we are up to visit: https://www.chem.purdue.edu/Slipchenko/
___

## Developer Notes
### Technologies/Software Used
1. Java JDK 8 (Mine is Java version - 1.8.0_101)
2. JavaFX -  a software platform for creating and delivering desktop applications, as well as rich Internet applications (RIAs) that can run across a wide variety of devices. JavaFX is intended to replace Swing as the standard GUI library for Java SE, but both will be included for the foreseeable future. JavaFX is supported up through SDK version 10. If the compiler has trouble recognizing JavaFX, go to File > Project Structure > Project and make sure that the SDK version and Project Language Level match.  If you are using JDK 11 or greater, you may have to install JavaFX manually. You can download it here: https://gluonhq.com/products/javafx/ 
3. SceneBuilder – for visualizing fxml files (UI files of the application). Download it from here – (http://gluonhq.com/products/scene-builder/). For a SceneBuilder tutorial visit: http://code.makery.ch/library/javafx-8-tutorial
4. Jmol Jar File (used for visualizing the molecules within the application), see *Chemistry Technologies*.
5. Gson - Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java Object.
6. JUnit4 - JUnit is a widely used library for creating unit tests for Java programs. Unit tests are intended to test the functionality of individual classes rather than test their interaction. It is expected that if you create a new class, you also implement unit tests for that class. To learn how to write unit tests visit this page: https://www.vogella.com/tutorials/JUnit/article.html
### Setup
Two great java IDE's I like to use are IntelliJ IDEA or Eclipse. Both are capable of integrating with javaFX, sceneBuilder, and github. So choose whichever you would like to use.

#### IntelliJ Setup (IntelliJ IDE 2018.2.2, JDK 1.8 - this doc last updated: 10/1/19) 
1. **Git Clone** - Clone the repository on your local machine to a directory of your choice
```
$ git clone https://github.com/iSpiEFP/iSpiEFP_GUI.git 
```
2. **Import into IntelliJ IDEA** - Select "File > New > Project from Existing Sources..." and navigate to the location of the git clone, and then select the directory "iSpiEFP_GUI". This will barebones import the project into IntelliJ (More configuration is needed). Alternatively, if this is your first time using IntelliJ - click "Open" and select the directory "iSpiEFP_GUI".
3. **Configure JDK** - Select "File > Project Structure > Project". Under Project SDK select "1.8" and under Project Language Level Select "8 - Lambdas, Type annotations etc." Note: Having a more recent SDK version will work, as long as you select the correct Project Language Level. The Project Language Level should match the SDK version (Ex. If the SDK version is 10, the Project Language Level should be 10 - Local variable type inference.
4. **Add External Jar Files** - All external jar files are located in "iSpiEFP_GUI/iSpiEFP/lib/". To add them to the project select "File > Project Structure > Modules > Dependencies". In the right hand side of the panel next to "scope" there will appear a "+" symbol. Select "+ > add library > java library", and select the directory "lib". After the panel updates with lib, select the checkbox next to lib, and click "apply". This will add all 3rd party jar files such as Jmol.
5. **JUnit** - JUnit4 is the library that we use for unit tests. Fortunately, installations of IntelliJ come with JUnit4. Navigate to the iSpiEFP/Tests/src folder and select any of the testing class files. Hover over the errors that are underlined in red, and IntelliJ will ask if you'd like to add JUnit4 to the classpath. Select yes. Note: If this does not work, you may need to go to "File > Project Structure > Libraries", click the "+" near the top left, and select the "lib" folder; then, navigate to "Modules" in the sidebar on the left, hit the checkbox next to "lib" and "JUnit4," and hit "Ok."
6. **Add Resources** - In IntelliJ, navigate to the file: "iSpiEFP_GUI/iSpiEFP/resources", right click the resources folder, and select "Mark Directory as > Resources Root". This will add all the image/icon files needed for iSpiEFP. Note: If "Mark Directory as > Unmark as Resources Root" already exists as an option, skip this step.
7. **Add Source Root** - Now, in Intellij, navigate to the file: "iSpiEFP_GUI/iSpiEFP/src", right click the src folder, and select "Mark Directory as > Sources Root". This will let IntelliJ know where your source code is. Note: If "Mark Directory as > Unmark as Sources Root" already exists as an option, skip this step.
8. **Clean Project** - To assure a successful setup, select "File > Invalidate Caches / Restart > Invalidate Caches and Restart". This will restart IntelliJ with a clean cache to help assure the setup process went smoothly. 
9. **Compiler Output** - To specify where IntelliJ should direct compiler output to, in the Project explorer sidebar, right click on the "iSpiEFP/out" folder and select "Copy path." Then, go to "File > Project Structure > Project," and under the "Project compiler output" section, paste the filepath that was copied (ctrl+v).
10. **Run iSpiEFP** - Select "Run > Edit Configurations", and for the Main Class field select the "..." button. "org.ispiefp.app.Main" should pop up in the menu. If it does, select it. Now you are ready to go! Hit the green arrow and have some fun!

#### Using IntelliJ (IntelliJ IDE 2018.2.2, this doc last updated: 7/18/19)
1. **SceneBuilder** - SceneBuilder  can be downloaded [here](https://www.oracle.com/technetwork/java/javase/downloads/javafxscenebuilder-info-2157684.html "here") and can be used immediately. To integrate it with IntelliJ use: https://www.jetbrains.com/help/idea/preparing-for-javafx-application-development.html.
After SceneBuilder has been integrated, you can use it by simply navigating to a ".fxml" file, right click, and at the very bottom select "Open in SceneBuilder". This will allow you to seamlessly edit the ".fxml" files without touching any ".fxml" source code (Very Helpful for UI Components).
2. **Git**. IDEA has a built-in terminal that simplifies Git usage. Once you install Git, you can simply use the terminal for Git commands. 
5. **Exporting as JAR** - To export iSpiEFP as a JAR file, go to File -> Project Structure -> Project Settings -> Artifacts -> Click green plus sign -> Jar -> From modules with dependencies... From there, go to extract to the target JAR and press OK. Then to go Build on the main menu and select Build Artifact.

#### Eclipse Setup (Eclipse IDE, Mars .2 Release (4.5.2) (Updated 2/21/20)
1. **Java FX** – Install this as a new software within eclipse – named “e(fx)clipse – IDE”. Link: https://www.eclipse.org/efxclipse/install.html#for-the-lazy. The library should be imported for all every new project created. 
2. **SceneBuilder**  - We can configure to open and edit the fxml files directly from Eclipse. After downloading the SceneBuilder set the path to its exe file at Window -> Preferences -> JavaFX -> Scenebuilder Executable in Eclipse.
3. **GSON** - You may need to import GSON into Eclipse as it is not a default Java Library. For help importing Gson into eclipse go to: https://medium.com/programmers-blockchain/importing-gson-into-eclipse-ec8cf678ad52. This library should also be available for every new project you make. 
4. For **cloning** the repository into Eclipse visit: https://github.com/collab-uniba/socialcde4eclipse/wiki/How-to-import-a-GitHub-project-into-Eclipse for a tutorial.
5. For **Commiting** and **Pushing**(Git Plug-in must be installed, likely to be installed by default):
Right-Click Project Folder -> Select Team -> commit OR push
6. For __Exporting__ this project as a Jar file. From eclipse, click File -> Export -> Runnable Jar file. For the launch configuration, you need to run the Main.java for the first time and mention it in the configuration.

### Git Workflow
We are using the [GitHub flow branching model](https://guides.github.com/introduction/flow/). The cardinal rule of it is _"the `master` branch is always deployable"_.  In other words, for any new feature or fix, we would create a new branch, work in it until satisfied, then merge everything into master (and delete the no-longer-needed feature branch).

Let's say you want to add a new feature. This section will outline the Git structure with which we create and add new features. 
1. **Creating and switching to a branch** - To make a new branch, we use the **checkout** command. **Checkout** will switch your current branch to the one specified after the command. To create a new branch, type **git checkout -b new_branch_name** (**-b** specifies a new branch, only use this flag when checking out a not yet created branch). After running this command, you can use **git branch** to check to see if you had switched to **new_branch_name**. You could also use **git branch new_branch_name** followed by **git checkout new_branch_name** to do the same thing, it's just an extra step. NOTE: This will only create the branch on your local machine, and it will not exist in the gitHub remote repository just yet! "Pushing to the remote" is covered a few steps down, so that your changes on that branch may be seen and edited by others on their own machines.
2. **Adding and committing to your branch** - Once you are finished developing and testing your feature, you first want to add those changes to your local. Do this using the **git add** command. If you only changed one or two files, you can use **git add filename_1 filename_2**; to "add" all your files, even those you didn't work on, simply type **git add .** from the root folder, iSpiEFP_GUI (**git add *** also works). Then, you want to commit those changes, which will record your changes on your local repository. Ideally, you should have a short commit message as well, detailing what you've changed. Do this using **git commit -m "Your message"**. It's good practice to commit small but substantial changes and to do it often. Sensible commit messages are also a virtue.
3. **Pushing to the remote** - In order to add your changes to the iSpiEFP Github, we will use the **git push** command. But we will have to specify an "origin" argument, i.e. where the code will be pushed to. **Never commit to the master branch directly.** Instead, set the origin to be your branch name. The full command should be **git push origin/your_branch_name** (or, alternatively, **git push origin your_branch_name**).
4. **Merging your code with the master** - Once your branch's code is on gitHub, you'll want to merge your branch with the master in order to get your changes on the actual project. We will be using gitHub's built-in "Pull Request" functionality for this, but first, you will need to manage any conflicts between your branch changes and the current state of the master branch. To merge changes *into* a desired branch, you first have to make sure you're on that desired branch, in this case the one you created. First, run **git branch** to make sure you are on the correct branch. If not, run **git checkout <branch_name>** (notice there's no **-b** flag since your personal branch is not new). Once you're in the branch you wish to merge with master, run **git pull origin master** to try and merge the two. Chances are, things didn't go smoothly and you have what are called merge conflicts. While these can sometimes get tricky, the main thing you have to do is remove the code that you want to get rid of and keep your changes (Git will automatically add text markers to each problematic file, placing conflicting sections between very visible Git markers <<<<<<< and >>>>>>>>). You then remove any Git markers (like **HEAD**, **<<<<<<**, etc.) after resolving a section (take the upper half between **<<<<<< HEAD** and **======**, or the lower half between **======** and **>>>>>> <branch name**, or a mix of the two). The other option is **git rebase**, which is usually the preferred option for developers. This is like **git merge** except it allows for a more linear commit history. Then, to add your own changes to the remote repo on your remote branch, use the **git push** command to do so. Rebasing is the preferred option for developers and it is probably what you will find most useful. As an example, suppose you'd like to put your feature/changes into the master branch. Once you are on that branch, you can do **git rebase master**, which will cause the feature branch to begin on the tip of master. So all your changes are implemented onto master. But the key difference is that Git will rewrite the project history and create brand new commits for each of the previous commits. Which results in a much cleaner project history. 
5. **Pull Requests** - Make sure your branch is merged with the most current version of master before doing this step ("git checkout master" "git pull origin master" to make sure that your local machine has the most recent version - wouldn't want to accidentally merge your code with an older version of the branch!). Once your branch has been merged with master on your machine, and you would like to put these changes in the remote master branch on gitHub, navigate to the iSpiEFP_GUI directory in gitHub (if you are reading this README from gitHub, you are already there!). Along the top bar which states "commits ... branches ... releases ... contributors", click the "branches" tab. Locate your branch in the list, and click the "New pull request" button off to the right hand side. This will bring you to the Pull Request creation page, where you may leave a comment as to what your branch accomplished (if your code changes are attempting to resolve an Issue located in the "Issues" tab, you may refer to the issue by number by typing # - options to specify which Issue you are referring to will appear in a dropdown table). After clicking the green "Create pull request" button, you will be directed to a page where you may make comments on the Pull Request, add Reviewers (off to the right side of the page: **please add at least one code reviewer other than yourself**), or close the Pull Request (though it is not always a good idea to close Pull Requests that you personally made). If an issue comes up with your Pull Request and you need to make a change to the code, you may make changes without needing to delete the Pull Request and make another: simply make the changes on your local machine, commit the changes via "git add <files>" "git commit -m ...", and run "git push origin branch_name". When you return to the gitHub page for your Pull Request, you will see that your commit has been added to the remote. After a Pull Request has been accepted into master, it is a good idea to delete the branch on your local machine (see **Deleting a branch** below).
6. **Updating local master branch** - To update your local repo to be in tune with the master branch, the main option is doing **git pull**.  This will essentially **fetch** the changes from Git and **merge** them with your local,  and the developer can settle merge conflicts from there. You will typically want to be on the master branch itself before running **git pull origin master** (**git checkout master**), unless you are attempting to merge your personal branch with `master` in order to submit a Pull Request on gitHub.
7. **Deleting a branch** - Once you have safely and surely merged your code from your branch, the next thing to do is delete the branch. The process for this is simple and only requires one command. But first, make sure you're not on the branch you're attempting to delete (remember, you can switch using **git checkout other_branch_name**). Once you have navigated to a different branch, run **git branch -d branch_to_delete** in your terminal to delete the local version of your branch (though Git may ask you to run **git branch -D branch_to_delete** to confirm). To delete your branch on Github,  navigate to the overhead bar containing repository activity information (commits, branches, etc.) and click on branches. This will take you to all the branches that have been pushed to the Github repo, and next to each one is a trash icon, which you simply click to delete the branch. 

### Code Base
Most of the Source Code that needs to be edited belongs in:
```
iSpiEFP/src/org/vmol/app
```
The path of the Main Class is where the app is launched:
```
iSpiEFP/src/org/vmol/app/Main.java
```
The path of the App Resources for images such as button icons:
```
iSpiEFP/resources/images
```
### Quick Class & Package descriptions
1. **Main** - Starts the Application.
2. **visualizer** - handles most button actions and visualizing of molecules with Jmol. This package is used when a user opens a pdb or xyz file and launches about every next step in the simulation.
3. **util** - contains some helper functions for actions.
4. **submission** - contains the controller for javaFX gui when a user asks for Libefp Job submission history.
5. **server** - handles some server interactions and remote interactions.
6. **libEFP** - contains controller for editing and submitting LibEFP jobs.
7. **loginPack** - handles user login for SSH connections.
8. **installer** - handles all bundleManagement for remote and local packages (GAMESS and LIBEFP). Makes sure correct directories are installed locally and remote. Makes sure correct packages are given on remote ends.
9. **gamessSubmission** - contains controller when user wants to find Gamess jobs submission history.
10. **gamess** - controller for editing and submitting Gamess jobs.
11. **database** - controller for querying the database for parameters, parsing and writing data, and controlling the auxiliary jmol viewer and list. 

### Unit Tests
In order to run unit tests, there is some initial set-up which needs to be done only once. The following instructions are for IntelliJ setup.
####Set-Up
1. Right-Click on the ```iSpiEFP/Tests/src``` directory and select the option **Mark Directory as -> Test Sources Root**
2. Right-Click on the ```iSpiEFP/Tests/TestResources``` directory and select the option **Mark Directory as -> Test Resources Root**
3. Ensure JUnit4 has been added to your classpath by following the instructions in the IntelliJ set-up section of this README.
#### Running Tests
To run unit tests, navigate to the testing class contained in ```iSpiEFP/Tests/src``` which corresponds to the class you are interested in testing. To run all of the tests contained within that testing class, navigate to the class declaration of that file and click on the green play button left of that line of code. To run individual tests, click on the green play button next to the method declaration for that test. 
### Server Config
iSpiEFP connects to the iSpiEFP Server. The iSpiEFP Server is powered by AWS and is configured to run from
__port 8080__. There are currently two servers for iSpiEFP to use and they should be changed depending on the situation. (Test for a Release, Developer for developing work). The Configurations for the server can be found at the top of __iSpiEFP/src/org/vmol/app/Main.java__. For information of the iSpiEFP Server visit: https://github.com/iSpiEFP/iSpiEFPServer/blob/master/README.md
#### Test Server
1. The Test Server's primary purpose is to keep an instance of iSpiEFP running 24/7 so that users can connect and use the currently released iSpiEFP Client Applications.
2. The test server address is to be used for a new release. 
3. Address:
    ```
    ec2-3-16-11-177.us-east-2.compute.amazonaws.com
    ```
#### Development Server
1. The Development Server's primary purpose is for developers to modify a new version of iSpiEFP that has yet to be released. This is so that users do not get interrupted while using a stable version of iSpiEFP.
2. The Developer Server is used for developing.
3. Address:
    ```
     ec2-18-220-105-41.us-east-2.compute.amazonaws.com
    ```


