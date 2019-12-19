package org.ispiefp.app;

import org.ispiefp.app.MetaData.*;
import org.ispiefp.app.installer.BundleManager;

public class Initializer {
    private BundleManager bundleManager;
    private LocalFragmentTree localFragmentTree;

    public Initializer(){
        bundleManager = new BundleManager("LOCAL");
    }

    public void init(){
        bundleManager.manageLocal();
        Main.fragmentTree= new LocalFragmentTree();
    }

}