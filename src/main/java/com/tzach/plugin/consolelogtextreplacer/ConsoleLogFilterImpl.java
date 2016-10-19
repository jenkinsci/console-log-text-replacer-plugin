package com.tzach.plugin.consolelogtextreplacer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author tzach
 */
public class ConsoleLogFilterImpl extends ConsoleLogFilter {
    private ConfigurationFile configurationFile;

    public ConsoleLogFilterImpl(String configFile) {
        if ( configFile == null){

        } else {
            Gson gson = new GsonBuilder().create();
            try (FileReader fileReader = new FileReader(new File(configFile))) {
                configurationFile = gson.fromJson(fileReader, ConfigurationFile.class);
            } catch (IOException e) {
                configurationFile = null;
            }
        }

    }

    @Override
    public OutputStream decorateLogger(AbstractBuild abstractBuild, OutputStream outputStream) throws IOException, InterruptedException {
        return new MyLine(outputStream, configurationFile);
    }
}

