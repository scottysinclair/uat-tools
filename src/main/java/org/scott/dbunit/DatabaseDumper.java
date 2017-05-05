package org.scott.dbunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Used dbunit to dump database contents to XML files.
 *
 * The files which are dumped are also controlled from junit
 *
 * @author scott
 *
 */
public class DatabaseDumper {

  public static void main(String args[]) throws Exception {
     if (args.length < 1) {
       System.out.println("Please specify the test  folder");
       return;
     }
     File testFolder = new File(args[0]);
     if (!testFolder.exists()) {
       if (!testFolder.mkdirs()) {
         System.err.println("Could not create the test folder '" + testFolder.getAbsolutePath() + "'.");
         return;
       }
       System.out.println("Created test folder '" + testFolder.getAbsolutePath() + "'.");
     }

     Properties props ;
     try {
       props = loadProps("app.properties");
     }
     catch(FileNotFoundException x) {
       System.out.println("Could not find the app.properties file in the current working directory.");
       return;
     }
     try {
       dumpNew(testFolder, props);
     }
     catch(Exception x) {
       System.err.println("Error dumping new db, " + x.getMessage());
     }
     try {
       dumpInt(testFolder, props);
     }
     catch(Exception x) {
       System.err.println("Error dumping old db, " + x.getMessage());
     }
  }

  private static void dumpNew(File testFolder, Properties props) throws IOException {
    DataSource pgDataSource = createDataSource("pg",  props );
    DatabaseHelper helper = new DatabaseHelper(pgDataSource, null, null);
    File dumpFile = new File(testFolder, "input_ds.xml");
    helper.dumpDatabase( dumpFile, getTableFilter(props) );
  }

  private static void dumpInt(File testFolder, Properties props) throws IOException {
    DataSource pgDataSource = createDataSource("db2",  props );
    DatabaseHelper helper = new DatabaseHelper(pgDataSource, null, null);
    File dumpFile = new File(testFolder, "input_dsInt.xml");
    helper.dumpDatabase( dumpFile, getTableFilter(props) );
  }

  private static String[] getTableFilter(Properties props) {
    String value = props.getProperty("pg.table.filter");
    return value != null && value.length() > 0 ? value.split(",") : null;
  }

  private static Properties loadProps(String fileName) throws IOException {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream( fileName ); ){
      props.load(in);
      return props;
    }
  }

  private static DataSource createDataSource(String prefix, Properties props) throws IOException {
    DriverManagerDataSource dmDataSource = new DriverManagerDataSource();
    dmDataSource.setDriverClassName( props.getProperty(prefix + ".driver") );
    dmDataSource.setUrl( props.getProperty(prefix + ".jdbcurl") );
    dmDataSource.setUsername( props.getProperty(prefix + ".user") );
    dmDataSource.setPassword( props.getProperty(prefix + ".password") );
    return dmDataSource;
  }

}
