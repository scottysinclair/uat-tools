package org.scott.uattools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class ServiceCallDumper {

  public static void main(String args[]) throws Exception {
     if (args.length < 2) {
       System.err.println("Please specify the test case name, then the location of the file");
       return;
     }

     File testDir = new File(args[0]);
     if (!testDir.exists()) {
       if (!testDir.mkdirs()) {
         System.err.println("Test directory " + testDir.getPath() + " could not be created.");
         return;
       }
     }

     String source = args[1];
     String content = "";
     if (source.startsWith("docker:")) {
       content = getFromDockerContainer( source );
     }
     else {
       content = getFromLocalFileSystem( source );
     }

     FileWriter output = new FileWriter( new File(testDir, "player.xml") );
     output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
     output.write("<com.acme.spring.hibernate.MethodCalls>\n");
     output.write(content);
     output.write('\n');
     output.write("</com.acme.spring.hibernate.MethodCalls>\n");
     output.flush();
     output.close();
  }

  private static String getFromDockerContainer(String customDockerUrl) {
    return null;
  }

  private static String getFromLocalFileSystem(String path) {
    File file  = new File(path);
    try {
      StringBuilder sb = new StringBuilder();
      try (LineNumberReader lin = new LineNumberReader( new InputStreamReader( new FileInputStream( file ) ) );) {
        String line;
        while((line = lin.readLine()) != null) {
          sb.append(line);
          sb.append('\n');
        }
        return sb.toString();
      }
    }
    catch(IOException x) {
      throw new IllegalStateException("Could not load playerXml file at path  '" + path + "'", x);
    }
  }
}
