package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class DirectoryNode {
    private final String name;
    private List<DirectoryNode> children = null;

    public DirectoryNode(String name) {
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public List<DirectoryNode> getChildren() {
        if(children == null){
            children = new ArrayList<>();
            File currentFile = new File(name);
            File[] files = currentFile.listFiles();
            if(files != null){
                for(File file: files){
                    if(file.isDirectory()){
                        DirectoryNode child = new DirectoryNode(file.getAbsolutePath());
                        children.add(child);
                    }
                }
            }
        }
        return children;
    }


}
