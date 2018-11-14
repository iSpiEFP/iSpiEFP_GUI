# iSpiEFP_Dev_Scripts
iSpiEFP Development Scripts

Technical Documentation – VMol

Technologies/Software used for building the application:
1)	Eclipse IDE, Mars .2 Release (4.5.2)
2)	JDK 8 (Mine is Java version - 1.8.0_101)
3)	Java FX – Install this as a new software within eclipse – named “e(fx)clipse – IDE”. Link: https://www.eclipse.org/efxclipse/install.html#for-the-lazy
4)	SceneBuilder – for visualizing fxml files (UI files of the application). Download it from here – (http://gluonhq.com/products/scene-builder/). We can configure to open and edit the fxml files directly from Eclipse. After downloading the SceneBuilder set the path to its exe file at Window -> Preferences -> JavaFX -> Scenebuilder Executable in Eclipse.
5)	JMol Jar file (used for visualizing the molecules within the application).
Refer to this javafx and SceneBuilder tutorial - http://code.makery.ch/library/javafx-8-tutorial

Instructions:
Running the project from Eclipse – Import the ‘Vmol’ project using Git to eclipse’s workspace and run the Main.java file located at iSpiEFP_Dev_Scripts/iSpiEFP/src/org/vmol/app/Main.java (click on Main.java, hit the green arrow in eclipse). This will launch the application.
Exporting the Project as jar file – From eclipse, click File -> Export -> Runnable Jar file. For the launch configuration, you need to run the Main.java for the first time and mention it in the configuration. 
All editable code for the source file is located in iSpiEFP_Dev_Scripts/iSpiEFP/src/org/vmol/app/

Git Instructions with Eclipse IDE, Mars .2 Release (4.5.2)
For cloning this repository: 
https://github.com/collab-uniba/socialcde4eclipse/wiki/How-to-import-a-GitHub-project-into-Eclipse

Commiting and Pushing(Git Plug-in must be installed, likely to be installed by default):
Right-Click Project Folder -> Select Team -> commit OR push

And Finally:

-for directions on how to actually use this thing see "iSpiEFP usage"

-for a list of known bugs see "Known Issues" 

