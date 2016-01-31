/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioboxes.bioboxmesossheduler.tools;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author jsteiner
 */
public class LibMesosFinder extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private int numMatches = 0;
    private String result;

    public LibMesosFinder(String pattern) {
        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
    }

    void find(Path file) {
        Path name = file.getFileName();
        if (name != null && matcher.matches(name)) {
            numMatches++;
            result = file.toString();
        }
    }

    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes attrs) {
        find(file);
        return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        find(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }

    public String getResult() {
        return result;
    }
    
    
}
