/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

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
