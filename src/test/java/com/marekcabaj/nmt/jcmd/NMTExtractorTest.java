package com.marekcabaj.nmt.jcmd;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.marekcabaj.nmt.bean.NativeMemoryTrackingKind;
import com.marekcabaj.nmt.bean.NativeMemoryTrackingType;
import com.marekcabaj.nmt.bean.NativeMemoryTrackingValues;

public class NMTExtractorTest {

    private NativeMemoryTrackingValues nmtProperties;

    @Before
    public void setUp() {
        String testJmcdOutput = "Total: reserved=1470626KB, committed=170826KB\n" +
                "-                 Java Heap (reserved=65536KB, committed=46592KB)\n" +
                "                            (mmap: reserved=65536KB, committed=46592KB) \n" +
                " \n" +
                "-                     Class (reserved=1081294KB, committed=36814KB)\n" +
                "                            (classes #5962)\n" +
                "                            (malloc=4046KB #6901) \n" +
                "                            (mmap: reserved=1077248KB, committed=32768KB) \n" +
                " \n" +
                "-                    Thread (reserved=22009KB, committed=22009KB)\n" +
                "                            (thread #22)\n" +
                "                            (stack: reserved=21504KB, committed=21504KB)\n" +
                "                            (malloc=65KB #112) \n" +
                "                            (arena=440KB #42)\n" +
                " \n" +
                "-                      Code (reserved=252309KB, committed=16101KB)\n" +
                "                            (malloc=2709KB #3757) \n" +
                "                            (mmap: reserved=249600KB, committed=13392KB) \n" +
                " \n" +
                "-                        GC (reserved=6028KB, committed=5860KB)\n" +
                "                            (malloc=3468KB #184) \n" +
                "                            (mmap: reserved=2560KB, committed=2392KB) \n" +
                " \n" +
                "-                  Compiler (reserved=8424KB, committed=8424KB)\n" +
                "                            (malloc=9KB #111) \n" +
                "                            (arena=8415KB #8)\n" +
                " \n" +
                "-                  Internal (reserved=4155KB, committed=4155KB)\n" +
                "                            (malloc=4091KB #7583) \n" +
                "                            (mmap: reserved=64KB, committed=64KB) \n" +
                " \n" +
                "-                    Symbol (reserved=9378KB, committed=9378KB)\n" +
                "                            (malloc=6557KB #58783) \n" +
                "                            (arena=2821KB #1)\n" +
                " \n" +
                "-    Native Memory Tracking (reserved=1232KB, committed=1232KB)\n" +
                "                            (malloc=5KB #66) \n" +
                "                            (tracking overhead=1227KB)\n" +
                " \n" +
                "-               Arena Chunk (reserved=20262KB, committed=20262KB)\n" +
                "                            (malloc=20262KB) ";
        nmtProperties = new NMTPropertiesExtractor().extractFromJcmdOutput(testJmcdOutput);
    }

    @Test
    public void testGetTotal() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.TOTAL;
        assertEquals(1470626, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(170826, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetHeap() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.JAVA_HEAP;
        assertEquals(65536, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(46592, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetClass() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.CLASS;
        assertEquals(1081294, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(36814, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetThread() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.THREAD;
        assertEquals(22009, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(22009, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetCode() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.CODE;
        assertEquals(252309, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(16101, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGC() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.GC;
        assertEquals(6028, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(5860, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetCompiler() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.COMPILER;
        assertEquals(8424, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(8424, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetInternal() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.INTERNAL;
        assertEquals(4155, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(4155, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetSymbol() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.SYMBOL;
        assertEquals(9378, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(9378, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetNMT() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.NMT;
        assertEquals(1232, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(1232, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }

    @Test
    public void testGetArenaChunk() throws Exception {
        NativeMemoryTrackingType key = NativeMemoryTrackingType.ARENA_CHUNK;
        assertEquals(20262, nmtProperties.get(NativeMemoryTrackingKind.RESERVED).get(key).longValue());
        assertEquals(20262, nmtProperties.get(NativeMemoryTrackingKind.COMMITTED).get(key).longValue());
    }
}