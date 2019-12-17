package org.ispiefp.app;

import org.ispiefp.app.installer.BundleManager;

public class Initializer {
    BundleManager bundleManager;
    LocalFragmentTree localFragmentTree;

    public Initializer(){
        bundleManager = new BundleManager("LOCAL");
    }

    public void init(){
        bundleManager.manageLocal();
        localFragmentTree = new LocalFragmentTree();
    }

}