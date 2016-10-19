package com.tzach.plugin.consolelogtextreplacer;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author tzach
 */
// first, extend the SimpleBuildWrapper
public class ConsoleLogFormatterSimpleBuildWrapper extends SimpleBuildWrapper {

    private final String configFile;

    // This is a must, if not jenkins will fail when running
    @Override
    public void setUp(Context context, Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener taskListener, EnvVars envVars) throws IOException, InterruptedException {

    }

    // @DataBoundConstructor is a must even if the constructor empty
    @DataBoundConstructor
    public ConsoleLogFormatterSimpleBuildWrapper(String configfile) {
        this.configFile = configfile;
    }


    // notice that the name of the getter must exactly like the parameter
    public String getConfigfile() {
        return configFile;
    }

    // @Extension is a must
    // BuildWrapperDescriptor wraps the whole build process
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        // Must be inside the DescriptorImpl
        public FormValidation doCheckConfigfile(@QueryParameter String value) throws IOException, ServletException{

            File file = new File(value);
            if ( value.isEmpty()){
                return FormValidation.ok();
            }
            if ( !file.exists()){
                return FormValidation.error("Could not find " + value);
            }
            if ( !file.isFile()){
                return FormValidation.error(value + " is not a file");
            }
            Gson gson = new GsonBuilder().create();

            try (FileReader fileReader = new FileReader(new File(value))) {
                ConfigurationFile configurationFile;
                    configurationFile = gson.fromJson(fileReader, ConfigurationFile.class);
                if ( configurationFile.entries.size() == 0){
                    return FormValidation.warning("Found 0 valid entries in file " + value);
                } else {
                    for ( ConfigurationFileEntry configurationFileEntry : configurationFile.entries){
                        if ( configurationFileEntry.text == null || configurationFileEntry.replaceto == null){
                            return FormValidation.error("File " + value + " contains invalid entries");
                        }
                    }
                }
                return FormValidation.ok("Valid configuration file. Found " + configurationFile.entries.size() + " entries");
            } catch (Exception e) {
                return FormValidation.error("Not a valid configuration file");

            }
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> abstractProject) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Console Log Text Replacer";
        }
    }

    // overriding in order to create our ConsoleLogFilterImpl
    @Override
    public ConsoleLogFilter createLoggerDecorator(@Nonnull Run<?, ?> build) {
        return new ConsoleLogFilterImpl(this.configFile);
    }

}
