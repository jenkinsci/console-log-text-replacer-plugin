package com.tzach.plugin.consolelogtextreplacer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author tzach
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public class MyLine extends LineTransformationOutputStream {

    private final OutputStream logger;
    private final ConfigurationFile configurationFile;

    public MyLine(OutputStream logger, ConfigurationFile configurationFile) {
        this.logger = logger;
        this.configurationFile = configurationFile;
    }

    @Override
    protected void eol(byte[] bytes, int len) throws IOException {
        String line = new String(bytes, 0, len);

        if (configurationFile == null) {
            return;
        } else {
            for (ConfigurationFileEntry configurationFileEntry : configurationFile.entries) {
                if (configurationFileEntry.text == null || configurationFileEntry.replaceto == null) {

                } else {
                    line = line.replace(configurationFileEntry.text, configurationFileEntry.replaceto);
                }
            }
        }

        logger.write(line.getBytes());

    }
}
