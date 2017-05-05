package org.scott.dbunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Used dbunit to dump database contents to XML files.
 *
 *
 * @author scott
 *
 */
public class DatabasePreparer {

  public static void main(String args[]) throws Exception {
     if (args.length < 1) {
       System.out.println("Please specify the test  folder");
       return;
     }
     File testFolder = new File(args[0]);
     if (!testFolder.exists()) {
       System.out.println("The test folder '" + testFolder.getAbsolutePath() + "' does not exist");
       return;
     }

     try {
       dumpNew(testFolder);
     }
     catch(Exception x) {
       System.err.println("Error dumping new db, " + x.getMessage());
     }
     try {
       dumpInt(testFolder);
     }
     catch(Exception x) {
       System.err.println("Error dumping old db, " + x.getMessage());
     }
  }

  private static void dumpNew(File testFolder) throws IOException {
    DataSource pgDataSource = createPostgresDataSource();
    DatabaseHelper helper = new DatabaseHelper(pgDataSource, null, null);
    File dumpFile = new File(testFolder, "input_ds.xml");
    helper.dumpDatabase( dumpFile );
  }

  private static void dumpInt(File testFolder) throws IOException {
    DataSource pgDataSource = createDb2DataSource();
    DatabaseHelper helper = new DatabaseHelper(pgDataSource, null, null);
    File dumpFile = new File(testFolder, "input_dsInt.xml");
    helper.dumpDatabase( dumpFile );
  }

  private static DataSource createPostgresDataSource() throws IOException {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream("db.properties"); ){
      props.load(in);
    }
    DriverManagerDataSource dmDataSource = new DriverManagerDataSource();
    dmDataSource.setDriverClassName( props.getProperty("pg.driver") );
    dmDataSource.setUrl( props.getProperty("pg.jdbcurl") );
    dmDataSource.setUsername( props.getProperty("pg.user") );
    dmDataSource.setPassword( props.getProperty("pg.password") );
    return dmDataSource;
  }

  private static DataSource createDb2DataSource() throws IOException {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream("db.properties"); ){
      props.load(in);
    }
    DriverManagerDataSource dmDataSource = new DriverManagerDataSource();
    dmDataSource.setDriverClassName( props.getProperty("db2.driver") );
    dmDataSource.setUrl( props.getProperty("db2.jdbcurl") );
    dmDataSource.setUsername( props.getProperty("db2.user") );
    dmDataSource.setPassword( props.getProperty("db2.password") );
    return dmDataSource;
  }

}
