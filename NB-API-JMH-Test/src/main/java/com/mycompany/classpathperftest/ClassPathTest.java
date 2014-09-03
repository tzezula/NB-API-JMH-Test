/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.classpathperftest;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 *
 * @author Tomas Zezula
 */
@Fork(1)
@State(Scope.Thread)
public class ClassPathTest {
    
    private FileObject fo;

    @Setup
    public void setUp() throws IOException, PropertyVetoException {
        fo = FileUtil.toFileObject(new File("").getAbsoluteFile());
        System.out.println(fo.getPath());
    }
    
    @GenerateMicroBenchmark
    @Threads(1)
    @Warmup(iterations = 10, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 100, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    public void testGetClassPath() {
        final ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
    }
}
