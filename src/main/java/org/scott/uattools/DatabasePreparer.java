package org.scott.uattools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Used dbunit to prepare database contents based on XML files.
 *
 * @author scott
 *
 */
public class DatabasePreparer {

  public static void main(String args[]) throws Exception {
     if (args.length < 1) {
       System.out.println("Please specify the test folder");
       return;
     }
     File testFolder = new File(args[0]);
     if (!testFolder.exists()) {
       System.out.println("The test folder '" + testFolder.getAbsolutePath() + "' does not exist");
       return;
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
       prepareNew(testFolder, props);
     }
     catch(Exception x) {
       System.err.println("Error dumping new db, " + x.getMessage());
     }
     try {
       prepareOld(testFolder, props);
     }
     catch(Exception x) {
       System.err.println("Error dumping old db, " + x.getMessage());
     }
  }

  private static void prepareNew(File testFolder, Properties props) throws Exception {
    DataSource pgDataSource = createDataSource("pg",  props );
    DatabaseHelper helper = new DatabaseHelper(pgDataSource, null, null);
    File prepFile = new File(testFolder, "input_ds.xml");
    helper.prepareDatabaseFromPath( prepFile.getAbsolutePath() );
  }

  private static void prepareOld(File testFolder, Properties props) throws Exception {
    DataSource pgDataSource = createDataSource("db2",  props );
    DatabaseHelper helper = new DatabaseHelper(pgDataSource, null, null);
    File prepFile = new File(testFolder, "input_dsInt.xml");
    helper.prepareDatabaseFromPath( prepFile.getPath() );
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
