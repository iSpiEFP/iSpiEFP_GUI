# iSpiEFP (I Spy  Effective Fragment Potential)
___
iSpiEFP is a tool for visualizing and describing molecular systems with the EFP method. iSpiEFP comes complete with a public database full of EFP Parameter files, and missing parameters can be calculated using Gamess. 
This application serves as a job-workflow manager which binds different technologies into one single application that allows chemists to point and click while utilizing high performance computing. 

### Latest Release
For patch notes on the latest release: iSpiEFP Version 1.06-Alpha
 - https://github.com/iSpiEFP/iSpiEFP_GUI/releases/tag/v1.06-alpha
 
### Help
For help on using iSpiEFP visit the website that Yen has made. <sorry I am missing that right now>. (Release Data: TBA)

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
iSpiEFP is brought to you by the Slipchenko Theory Group at Purdue University. For more information on what we are up to visit: https://www.chem.purdue.edu/slipchenko/
___

## Developer Notes
### Technologies/Software Used
1. Java JDK 8 (Mine is Java version - 1.8.0_101)
2. JavaFX -  a software platform for creating and delivering desktop applications, as well as rich Internet applications (RIAs) that can run across a wide variety of devices. JavaFX is intended to replace Swing as the standard GUI library for Java SE, but both will be included for the foreseeable future.
3. SceneBuilder – for visualizing fxml files (UI files of the application). Download it from here – (http://gluonhq.com/products/scene-builder/). For a SceneBuilder tutorial visit: http://code.makery.ch/library/javafx-8-tutorial
4. Jmol Jar File (used for visualizing the molecules within the application), see *Chemistry Technologies*.
5. Gson - Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java Object.
### Setup
Two great java IDE's I like to use are IntelliJ IDEA or Eclipse. Both are capable of integrating with javaFX, sceneBuilder, and github. So choose whichever you would like to use.
#### Eclipse Setup (Eclipse IDE, Mars .2 Release (4.5.2))
1. **Java FX** – Install this as a new software within eclipse – named “e(fx)clipse – IDE”. Link: https://www.eclipse.org/efxclipse/install.html#for-the-lazy
2. **SceneBuilder**  - We can configure to open and edit the fxml files directly from Eclipse. After downloading the SceneBuilder set the path to its exe file at Window -> Preferences -> JavaFX -> Scenebuilder Executable in Eclipse.
3. **Gson** - You may need to import Gson into Eclipse as it is not a default Java Library. For help importing Gson into eclipse go to: https://medium.com/programmers-blockchain/importing-gson-into-eclipse-ec8cf678ad52
4. For **cloning** the repository into Eclipse visit: https://github.com/collab-uniba/socialcde4eclipse/wiki/How-to-import-a-GitHub-project-into-Eclipse for a tutorial.
5. For **Commiting** and **Pushing**(Git Plug-in must be installed, likely to be installed by default):
Right-Click Project Folder -> Select Team -> commit OR push
6. For __Exporting__ this project as a Jar file. From eclipse, click File -> Export -> Runnable Jar file. For the launch configuration, you need to run the Main.java for the first time and mention it in the configuration.
#### IntelliJ Setup
IntelliJ Setup is similar. (currently I am using IntelliJ, the process for getting the project on here is similar). You can google it here: https://www.google.com./
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
6. **qchem** - contains controller for editing and submitting LibEFP jobs.
7. **loginPack** - handles user login for SSH connections.
8. **installer** - handles all bundleManagement for remote and local packages (GAMESS and LIBEFP). Makes sure correct directories are installed locally and remote. Makes sure correct packages are given on remote ends.
9. **gamessSubmission** - contains controller when user wants to find Gamess jobs submission history.
10. **gamess** - controller for editing and submitting Gamess jobs.
11. **database** - controller for querying the database for parameters, parsing and writing data, and controlling the auxiliary jmol viewer and list. 

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



