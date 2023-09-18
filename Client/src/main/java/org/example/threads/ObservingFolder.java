package org.example.threads;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ObservingFolder implements Runnable {
    private WatchService watcher;
    private Path rootDir;
    private HashMap<WatchKey, Path> watchKeyHashMap;
    private Listener listener;
    private boolean isCancel = false;
    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public ObservingFolder(String dirPath, Listener listener) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        watchKeyHashMap = new HashMap<>();
//        File rootDir = new File(dirPath);
        this.rootDir = Path.of(dirPath);
        this.listener = listener;
        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println(dir);
                WatchKey watchKey = dir.register(
                        watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY
                );
                watchKeyHashMap.put(watchKey, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

//    public ObservingFolder(String dirPath, DataOutputStream outToServer) throws IOException {
//        this.outToServer = outToServer;
//        watcher = FileSystems.getDefault().newWatchService();
//        watchKeyHashMap = new HashMap<>();
////        File rootDir = new File(dirPath);
//        this.rootDir = Path.of(dirPath);
//        this.listener = listener;
//        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>(){
//            @Override
//            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                System.out.println(dir);
//                WatchKey watchKey = dir.register(
//                        watcher,
//                        StandardWatchEventKinds.ENTRY_CREATE,
//                        StandardWatchEventKinds.ENTRY_DELETE,
//                        StandardWatchEventKinds.ENTRY_MODIFY
//                );
//                watchKeyHashMap.put(watchKey, dir);
//                return FileVisitResult.CONTINUE;
//            }
//        });
//    }

//    private List<File> iterateDir(File dir){
//        List<File> files = new ArrayList<>();
//        files.add(dir);
//        File[] dirListing = dir.listFiles();
//        if(dirListing != null){
//            for(File subDir: dirListing){
//                if(subDir.isDirectory()){
//                    files.addAll(iterateDir(subDir));
//                }
//            }
//        }
//        return files;
//    }

//    public String iterateOverDir(String dirPath){
//        StringBuilder str = new StringBuilder();
//        File dir = new File(dirPath);
//        File[] directoryListing = dir.listFiles();
//        str.append(dir.getName());
//        str.append("\n");
//        if(directoryListing != null){
//            for(File file: directoryListing){
//                str.append(iterateOverDir(file.getAbsolutePath()));
//            }
//        }
////        else{
////            str.append(dir.getName());
////            str.append("\n");
////        }
//        return str.toString();
//    }

//    private void unregisterInvalidWatchKey(){
//        for(WatchKey key: watchKeyHashMap.values().toArray(new WatchKey[0])){
//            if(!key.isValid()){
//                key.cancel();
//            }
//        }
//    }

        @Override
        public void run () {
            while (!isCancel) {
                WatchKey watchKey = null;
                try {
                    watchKey = watcher.take();
                } catch (InterruptedException e) {
                    // Break the thread as purpose when server stopped.
                    return;
//                    throw new RuntimeException(e);
                }

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    System.out.println(event);
                    // Check type of event.
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    // Get file name from even context
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;

                    Path fileName = pathEvent.context();

                    // Start observing new directory created;
//                Path context = (Path) watchKey.watchable();
//                Path dir = context.resolve(fileName);
                    Path context = watchKeyHashMap.get(watchKey);
                    Path dir = context.resolve(fileName);
                    if (dir.toFile().isDirectory() && event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        try {
                            WatchKey dirWatchKey = dir.register(watcher,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_MODIFY,
                                    StandardWatchEventKinds.ENTRY_MODIFY
                            );
                            watchKeyHashMap.put(dirWatchKey, dir);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Perform necessary action with the event
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        listener.notifyServer(dir + " is created");
                        System.out.println("A file has been created: " + dir);
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        listener.notifyServer(dir + " has been deleted");
                        System.out.println("A file has been deleted: " + dir);
                    }
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY && !dir.toFile().isDirectory()) {

                        listener.notifyServer(dir + " has been modified");
                        System.out.println("A file has been modified: " + dir);
                    }

                }

                // Reset the watch key everytime for continuing to use it for further event polling
                boolean valid = watchKey.reset();
                if (!valid) {
                    Path dir = watchKeyHashMap.get(watchKey);
                    watchKeyHashMap.remove(watchKey);
                    watchKey.cancel();
                    if (dir == rootDir) {
                        break;
                    }
                }
            }
        }
        //    public void watchService() throws IOException {
//        WatchService watcher = FileSystems.getDefault().newWatchService();
//        Path directory = Path.of("D:\\HK2-2223\\KTNN");
//        WatchKey watchKey = directory.register(
//                watcher,
//                StandardWatchEventKinds.ENTRY_CREATE,
//                StandardWatchEventKinds.ENTRY_MODIFY,
//                StandardWatchEventKinds.ENTRY_DELETE);
//
//        // STEP4: Poll for events
//        while (true) {
//            for (WatchEvent<?> event : watchKey.pollEvents()) {
//
//                // STEP5: Get file name from even context
//                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
//
//                Path fileName = pathEvent.context();
//
//                // STEP6: Check type of event.
//                WatchEvent.Kind<?> kind = event.kind();
//
//                // STEP7: Perform necessary action with the event
//                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
//
//                    System.out.println("A new file is created : " + fileName);
//                }
//
//                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
//
//                    System.out.println("A file has been deleted: " + fileName);
//                }
//                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
//
//                    System.out.println("A file has been modified: " + fileName);
//                }
//
//            }
//
//            // STEP8: Reset the watch key everytime for continuing to use it for further event polling
//            boolean valid = watchKey.reset();
//            if (!valid) {
//                break;
//            }
//        }
//    }
    public interface Listener {
        void notifyServer(String str);
    }
}