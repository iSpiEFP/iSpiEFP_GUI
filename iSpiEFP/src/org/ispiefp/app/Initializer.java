package org.ispiefp.app;

import org.ispiefp.app.installer.BundleManager;

public class Initializer {
    ///Manage Working Directory
    BundleManager bundleManager = new BundleManager("LOCAL");
    bundleManager.manageLocal();

}
