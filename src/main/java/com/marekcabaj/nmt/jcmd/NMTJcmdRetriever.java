package com.marekcabaj.nmt.jcmd;

import com.marekcabaj.nmt.bean.NativeMemoryTrackingValues;

public class NMTJcmdRetriever {

    public static final String VM_NATIVE_MEMORY_SUMMARY_COMMAND = "VM.native_memory summary";

    private JcmdCommandRunner jcmdCommandRunner;

    private NMTPropertiesExtractor nmtPropertiesExtractor;

    public NMTJcmdRetriever() {
        super();
        this.jcmdCommandRunner = new JcmdCommandRunner();
        this.nmtPropertiesExtractor = new NMTPropertiesExtractor();
    }

    public NativeMemoryTrackingValues retrieveNativeMemoryTrackingValues(String command) {
        String output = this.jcmdCommandRunner.runJcmdCommand(command);
        return this.nmtPropertiesExtractor.extractFromJcmdOutput(output);
    }
}
