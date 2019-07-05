package com.marekcabaj.nmt;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

public class JcmdCommandRunner {

    private final Logger logger = LoggerFactory.getLogger(JcmdCommandRunner.class);
    private String jcmdCmd;

    private String pid;

    private String javaHome;

    public JcmdCommandRunner() {
        super();
        Environment environment = new StandardEnvironment();
        this.pid = environment.getProperty("PID");
        this.javaHome = environment.getProperty("java.home");
        String os = environment.getProperty("os.name").toLowerCase();
        boolean isUnix = os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0;
        boolean isWindows = os.indexOf("win") >= 0;
        if (isUnix) {
            jcmdCmd = "./jcmd";
        } else if (isWindows) {
            jcmdCmd = "jcmd";
        } else {
            logger.error("OS not supported ! JcmdCommandRunner only supports Windows and Unix systems");
            jcmdCmd = null;
        }
    }

    protected String runNMTSummary() {
        return runJcmdCommand("VM.native_memory summary");
    }

    protected String runNMTBaseline() {
        return runJcmdCommand("VM.native_memory baseline");
    }

    protected String runJcmdCommand(String command) {
        if (jcmdCmd == null) {
            return "";
        }
        ProcessBuilder builder = new ProcessBuilder(jcmdCmd, pid, command);
        builder.directory(new File(javaHome + File.separator + "bin"));
        String cmd = builder.command().toString();
        logger.info("Running command : {}", cmd);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output = readCommandOutput(process);
            logger.debug("Output of command {} : {}", cmd, output);
            return output;
        } catch (IOException e) {
            logger.error("Error while starting command : {}", cmd, e);
            return "";
        }
    }

    protected String readCommandOutput(Process process) {
        StringBuilder sb = new StringBuilder();
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append(System.getProperty("line.separator"));
            }
        }
        return sb.toString();
    }
}
