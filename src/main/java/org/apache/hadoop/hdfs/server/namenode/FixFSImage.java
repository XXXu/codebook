package org.apache.hadoop.hdfs.server.namenode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.SafeModeAction;
import org.apache.hadoop.hdfs.server.namenode.FSImage;
import org.apache.hadoop.hdfs.server.namenode.FSImageFormatProtobuf;
import org.apache.hadoop.hdfs.server.namenode.FSNamesystem;

public class FixFSImage {
    private static void printUsage() {
        String usage = "Required command line arguments:\n-i, --inputFile <arg>   FSImage file to process.\n-c, --confdir <arg>     config directory where core-site.xml and hdfs-site.xml can be found.\n-f, --fix               save a new fsimage";
        System.out.println("Required command line arguments:\n-i, --inputFile <arg>   FSImage file to process.\n-c, --confdir <arg>     config directory where core-site.xml and hdfs-site.xml can be found.\n-f, --fix               save a new fsimage");
    }

    private static Options buildOptions() {
        Options options = new Options();

        OptionBuilder.isRequired();
        OptionBuilder.hasArgs();
        OptionBuilder.withLongOpt("inputFile");
        options.addOption(OptionBuilder.create("i"));

        OptionBuilder.isRequired();
        OptionBuilder.hasArgs();
        OptionBuilder.withLongOpt("confdir");
        options.addOption(OptionBuilder.create("c"));

        options.addOption("f", "fix", false, "");

        return options;
    }

    private static int run(String[] args) throws IOException {
        System.out.println("================== Begin ==================");
        Options options = buildOptions();
        CommandLine cmd;
        CommandLineParser parser = new PosixParser();
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.out.println("Error parsing command-line options: " + e.getMessage());
            printUsage();
            return 1;
        }

        String fsimage = cmd.getOptionValue("i");
        String confdir = cmd.getOptionValue("c");
        System.out.println("fsimage: " + fsimage);
        System.out.println("confdir: " + confdir);

        Configuration conf = new Configuration();
        conf.addResource(new FileInputStream(new File(confdir + "/core-site.xml")));
        conf.addResource(new FileInputStream(new File(confdir + "/hdfs-site.xml")));

        FSNamesystem fns = new FSNamesystem(conf, new FSImage(conf));
        FSImageFormatProtobuf.Loader loader = new FSImageFormatProtobuf.Loader(conf, fns, false);

        loader.load(new File(fsimage));
        long txId = loader.getLoadedImageTxId();
        System.out.println("Loaded image for txId " + txId);

        if (cmd.hasOption("f")) {
            System.out.println("Try to save a new image");
            fns.setSafeMode(HdfsConstants.SafeModeAction.SAFEMODE_ENTER);
            fns.getFSImage().lastAppliedTxId = txId;
            fns.saveNamespace();
        }

        System.out.println("================== Done ==================");
        return 0;
    }

    public static void main(String[] args) throws IOException {
        int status = run(args);
        System.exit(status);
    }
}
