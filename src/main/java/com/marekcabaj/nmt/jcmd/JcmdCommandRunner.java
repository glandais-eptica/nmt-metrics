package com.marekcabaj.nmt.jcmd;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

class JcmdCommandRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(JcmdCommandRunner.class);

    private String jcmdCmd;

    private File jcmdDirectory;

    private String pid;

    public JcmdCommandRunner() {
        super();
        Environment environment = new StandardEnvironment();
        this.pid = environment.getProperty("PID");
        String javaHome = environment.getProperty("java.home");
        this.jcmdDirectory = new File(javaHome + File.separator + "bin");
        String os = environment.getProperty("os.name").toLowerCase();
        boolean isUnix = os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0;
        boolean isWindows = os.indexOf("win") >= 0;
        if (isUnix) {
            jcmdCmd = "./jcmd";
        } else if (isWindows) {
            jcmdCmd = "jcmd";
        } else {
            LOGGER.error("OS not supported ! JcmdCommandRunner only supports Windows and Unix systems");
            jcmdCmd = null;
        }
    }

    public String runJcmdCommand(String command) {
        if (jcmdCmd == null) {
            return "";
        }
        ProcessBuilder builder = new ProcessBuilder(jcmdCmd, pid, command);
        builder.directory(jcmdDirectory);
        String cmd = builder.command().toString();
        LOGGER.debug("Running command : {}", cmd);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output = readCommandOutput(process);
            LOGGER.debug("Output of command {} : {}", cmd, output);
            return output;
        } catch (IOException e) {
            LOGGER.error("Error while starting command : {}", cmd, e);
            return "";
        }
    }

    protected String readCommandOutput(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        // scanner will close input stream
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append(System.getProperty("line.separator"));
            }
        }
        return sb.toString();
    }
}
