/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.perftest.barrier;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import sun.misc.Unsafe;

/**
 *
 * @author Tomas Zezula
 */
@Fork(1)
@State(Scope.Benchmark)
public class BarrierTest {

    private TestSync testSync;
    private TestAtomicReference testAR;
    private TestAtomicReferenceFieldUpdater testARFU;
    private TestUnsafe testUnsafe;

    @Setup
    public void setUp() throws IOException, PropertyVetoException {
        testSync = new TestSync();
        testAR = new TestAtomicReference();
        testARFU = new TestAtomicReferenceFieldUpdater();
        testUnsafe = new TestUnsafe();
    }

    @GenerateMicroBenchmark
    @Threads(4)
    @Warmup(iterations = 10, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 100, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    public void testSyncBarrier() {
        testSync.getPropertyChangeListener();
    }

    @GenerateMicroBenchmark
    @Threads(4)
    @Warmup(iterations = 10, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 100, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    public void testAtomicReference() {
        testAR.getPropertyChangeListener();
    }

    @GenerateMicroBenchmark
    @Threads(4)
    @Warmup(iterations = 10, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 100, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    public void testAtomicReferenceFieldUpdater() {
        testARFU.getPropertyChangeListener();
    }

    @GenerateMicroBenchmark
    @Threads(4)
    @Warmup(iterations = 10, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 100, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
    public void testUnsafe() {
        testUnsafe.getPropertyChangeListener();
    }

    private static final class TestSync {
        private PropertyChangeSupport support;

        public PropertyChangeSupport getPropertyChangeListener() {
            synchronized (this) {
                if (support == null) {
                    support = new PropertyChangeSupport(this);
                }
                return support;
            }
        }
    }

    private static final class TestAtomicReference {
        private final AtomicReference<PropertyChangeSupport> support = new AtomicReference<>();

        public PropertyChangeSupport getPropertyChangeListener() {
            PropertyChangeSupport sup = support.get();
            if (sup == null) {
                sup = new PropertyChangeSupport(this);
                if (!support.compareAndSet(null, sup)) {
                    sup = support.get();
                }
            }
            return sup;
        }
    }

    private static final class TestAtomicReferenceFieldUpdater {
        private static final AtomicReferenceFieldUpdater<TestAtomicReferenceFieldUpdater,PropertyChangeSupport> supportUpdater =
            AtomicReferenceFieldUpdater.newUpdater(
                TestAtomicReferenceFieldUpdater.class,
                PropertyChangeSupport.class,
                "support"); //NOI18N
        private volatile PropertyChangeSupport support;

        public PropertyChangeSupport getPropertyChangeListener() {
            PropertyChangeSupport sup = support;
            if (sup == null) {
                sup = new PropertyChangeSupport(this);
                if (!supportUpdater.compareAndSet(this, null, sup)) {
                    sup = support;
                }
            }
            return sup;
        }
    }

    private static final class TestUnsafe {
        private static Unsafe U;
        private static long offset;
        static {
            try {
                Field uProvider = Unsafe.class.getDeclaredField("theUnsafe");   //NOI18N
                uProvider.setAccessible(true);
                U = (Unsafe) uProvider.get(null);
                Field sf = TestUnsafe.class.getDeclaredField("support");        //NOI18N
                offset = U.objectFieldOffset(sf);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        private volatile PropertyChangeSupport support;

        public PropertyChangeSupport getPropertyChangeListener() {
            PropertyChangeSupport sup = support;
            if (sup == null) {
                sup = new PropertyChangeSupport(this);
                if (!U.compareAndSwapObject(this, offset, null, sup)) {
                    sup = support;
                }
            }
            return sup;
        }
    }
}
