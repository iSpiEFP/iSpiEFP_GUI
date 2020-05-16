package org.ispiefp.app.MetaData;
import org.ispiefp.app.installer.LocalBundleManager;

import java.util.Collection;
import java.util.TreeMap;

public class LocalFragmentTree {
    private TreeMap<String, MetaData> frag_tree;
    private MetaData selectedFragment;
    MetaHandler metaHandler;

    public LocalFragmentTree() {
        frag_tree = new TreeMap<>();
        selectedFragment = null;
//        metaHandler = new MetaHandler(LocalBundleManager.MASTER_META_FILE);
        metaHandler = new MetaHandler();
        MetaData[] metaDataArray = metaHandler.getMetaFile().getMetaDataObjects();
        System.out.println(LocalBundleManager.MASTER_META_FILE);

            for (int i = 0; i < metaDataArray.length; i++) {
                frag_tree.putIfAbsent(metaDataArray[i].getFromFile(), metaDataArray[i]);
            }

    }

    /**
     * Adds the metaData from a fragment to the tree using a string to the json File
     * This is intended to be used internally after calling the python script on a user
     * added EFP file. The file whose path this is called on should be created immediately
     * before invocation, and removed immediately thereafter. The data extracted from the
     * file will be stored in this tree data structure, and removed shortly thereafter.
     * @param filePath the path of the file to extract data from
     */
    public void addFragment(String filePath){
        metaHandler.setCurrentMetaData(filePath);
        frag_tree.put(metaHandler.getCurrentMetaData().getFromFile(), metaHandler.getCurrentMetaData());
    }

    /**
     * Returns an instance of a metaData by searching the tree.
     * It remains to be seen whether or not this function will be used. I think
     * I just need an iterator.
     *
     * @param key The name of the file from which the metaData was extracted
     * @return The metaData instance associated with the key
     */
    public MetaData getMetaData(String key){
        return frag_tree.get(key);
    }

    /**
     * Returns a collection of all of the MetaDatas currently contained within
     * the tree
     *
     * @return every MetaData contained within the tree
     */
    public Collection<MetaData> getMetaDataIterator(){
        return frag_tree.values();
    }

    public void setSelectedFragment(MetaData selectedFragment) {
        this.selectedFragment = selectedFragment;
    }

    public MetaData getSelectedFragment() {
        return selectedFragment;
    }
}
