package org.scott.dbunit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import javax.sql.DataSource;

import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseDataSet;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.persistence.core.test.AssertionErrorCollector;
import org.jboss.arquillian.persistence.dbunit.DataSetComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a datasource and provides methods for preparing the database and
 * asserting expected results.
 *
 * @author scott
 *
 */
public class DatabaseHelper {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

  protected final DataSource dataSource;
  private final String prepareFileName;
  private final String dumpFileName;

  public DatabaseHelper(DataSource dataSource, String prepareFileName, String dumpFileName) {
    this.dataSource = dataSource;
    this.prepareFileName = prepareFileName;
    this.dumpFileName = dumpFileName;
  }

  /**
   * Prepares the new database.</br>
   * Looks for file 'input_ds.xml' in the folder for the test case.<br/>
   * <br/>
   * This method must be called directly in the test case method so that the
   * test case name can be found from the StackTraceElement
   *
   * @throws Exception
   */
  public void prepareDatabase(String testName) throws Exception {
    prepareDatabaseFromPath('/' + testName + "/" + prepareFileName);
  }

  public void prepareDatabaseFromPath(String datasetPath) throws Exception {
    IDatabaseTester databaseTester = new DataSourceDatabaseTester(dataSource);
    databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
    databaseTester.setDataSet(readDataSetFromClasspath(datasetPath));
    databaseTester.onSetup();
  }

  /**
   * Asserts that the databse state matches the expected data-set found at the
   * datasetPath
   *
   * @param context
   *          some information about our assertion
   * @param datasetPath
   *          the path to the DBUnit dataset
   * @param excludedColumns
   *          columns to exclude (can be null).
   * @throws Exception
   */
  public void assertTestData(String context, String datasetPath, String excludedColumns[]) throws Exception {
    assertTestData(context, datasetPath, null, excludedColumns);
  }

  /**
   * Asserts that the databse state matches the expected data-set found at the
   * datasetPath
   *
   * @param context
   *          some information about our assertion
   * @param datasetPath
   *          the path to the DBUnit dataset
   * @param excludedColumns
   *          columns to exclude (can be null).
   * @param orderBy
   *          how to order the dataset before comparison (can be null)
   * @throws Exception
   */
  public void assertTestData(final String context, String datasetPath, String orderBy[], String excludedColumns[])
      throws Exception {

    if (orderBy == null) {
      orderBy = new String[0];
    }
    if (excludedColumns == null) {
      excludedColumns = new String[0];
    }
    DataSetComparator comparator = new DataSetComparator(orderBy, excludedColumns, Collections.emptySet());

    IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource);
    IDataSet currentDataSet = new DatabaseDataSet(connection, false);
    IDataSet expectedDataSet = readDataSetFromClasspath(datasetPath);

    AssertionErrorCollector errorCollector = new AssertionErrorCollector();

    comparator.compare(currentDataSet, expectedDataSet, errorCollector);

    /*
     * fail with errors if we need to (we catch the exception and adjust it to
     * add the context information
     */
    try {
      errorCollector.report();
    } catch (AssertionError x) {
      if (context != null) {
        AssertionError xAdjusted = new AssertionError("Test Failed " + context + "\n" + x.getMessage());
        xAdjusted.setStackTrace(x.getStackTrace());
        throw xAdjusted;
      } else {
        throw x;
      }
    }
  }

  public void dumpDatabase(String testName) {
    File targetFile = new File(getTargetFolderPath() + "/" + testName + "/" + dumpFileName);
      dumpDatabase(targetFile, null);
  }

  public void dumpDatabase(File file, String includeTables[]) {
      try {
        IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource);
        IDataSet currentDataSet = new DatabaseDataSet(connection, false);

        if (includeTables != null) {
          currentDataSet = new FilteredDataSet(includeTables, currentDataSet);
        }

        createDataDump(file, currentDataSet);
      }
      catch(Exception x) {
        //we log here, we don't want to fail the test because we couldn't dump data
        LOG.error("Could not dump the database", x);
      }
    }

  protected String getTargetFolderPath() {
    String targetFolderPath = System.getenv("uat_target");
    if (targetFolderPath == null) {
      targetFolderPath = "/target";
    }
    return targetFolderPath;
  }

  protected void createDataDump(File file, IDataSet dbContent) throws IOException, DataSetException {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));) {
      FlatXmlDataSet.write(dbContent, out);
    }
  }

  private IDataSet readDataSetFromClasspath(String datasetPath) throws Exception {
    try (InputStream in = new FileInputStream( datasetPath);) {
      if (in == null) {
        throw new IllegalStateException("Could not find dataset at path '" + datasetPath + "'");
      }
      return new FlatXmlDataSetBuilder().build(in);
    }
  }
}
