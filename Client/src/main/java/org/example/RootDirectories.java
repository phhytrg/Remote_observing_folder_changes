package org.example;

import org.example.DirectoryNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RootDirectories {

    private List<DirectoryNode> roots = new ArrayList<>();

    public RootDirectories() {
        for(File rootDir: File.listRoots()){
            DirectoryNode root = new DirectoryNode(rootDir.getAbsolutePath());
            roots.add(root);
        }
    }
    public List<DirectoryNode> getRoots() {
        return roots;
    }
    public List<String> getRootsName(){
        List<String> rootsString = new ArrayList<>();
        for(DirectoryNode root: roots){
            rootsString.add(root.getName());
        }
        return rootsString;
    }
//    public DirectoryNode getDirectoryNode(String path){
//        char rootDir = path.charAt(0);
//        DirectoryNode rootNode = null;
//        for(DirectoryNode root: roots){
//            char c = root.getName().charAt(0);
//            if(rootDir == c){
//                rootNode = root;
//            }
//        }
//
//        if (rootNode == null) {
//            return null;
//        }
//        while()
//    }
}
