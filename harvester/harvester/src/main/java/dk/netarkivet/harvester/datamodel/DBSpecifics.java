/* File:        $Id$
 * Revision:    $Revision$
 * Author:      $Author$
 * Date:        $Date$
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2010 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package dk.netarkivet.harvester.datamodel;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.IllegalState;
import dk.netarkivet.common.exceptions.NotImplementedException;
import dk.netarkivet.common.utils.DBUtils;
import dk.netarkivet.common.utils.SettingsFactory;

/**
 * Defines database specific implementations used by the Harvester.
 *
 * The actual actual implementation which is loaded is defined by the
 * {@link CommonSettings#DB_SPECIFICS_CLASS} setting. See the sub class list for
 * available implementations
 */
public abstract class DBSpecifics extends SettingsFactory<DBSpecifics> {

    /** The instance of the DBSpecifics class. */
    private static DBSpecifics instance;

    Log log = LogFactory.getLog(DBSpecifics.class);

    /* Constants definining the tables of the Harvester database and the 
     * required versions of the individual tables.
     */
    public static final String DOMAINS_TABLE = "domains";
    public static final int DOMAINS_TABLE_REQUIRED_VERSION = 2;

    public static final String CONFIGURATIONS_TABLE = "configurations";
    public static final int CONFIGURATIONS_TABLE_REQUIRED_VERSION = 5;

    public static final String SEEDLISTS_TABLE = "seedlists";
    public static final int SEEDLISTS_TABLE_REQUIRED_VERSION = 1;

    public static final String PASSWORDS_TABLE = "passwords";
    public static final int PASSWORDS_TABLE_REQUIRED_VERSION = 1;

    public static final String OWNERINFO_TABLE = "ownerinfo";
    public static final int OWNERINFO_TABLE_REQUIRED_VERSION = 1;

    public static final String HISTORYINFO_TABLE = "historyinfo";
    public static final int HISTORYINFO_TABLE_REQUIRED_VERSION = 2;

    public static final String CONFIGPASSWORDS_TABLE = "config_passwords";
    public static final int CONFIGPASSWORDS_TABLE_REQUIRED_VERSION = 1;

    public static final String CONFIGSEEDLISTS_TABLE = "config_seedlists";
    public static final int CONFIGSEEDLISTS_TABLE_REQUIRED_VERSION = 1;

    public static final String HARVESTDEFINITIONS_TABLE = "harvestdefinitions";
    public static final int HARVESTDEFINITIONS_TABLE_REQUIRED_VERSION = 2;

    public static final String PARTIALHARVESTS_TABLE = "partialharvests";
    public static final int PARTIALHARVESTS_TABLE_REQUIRED_VERSION = 1;

    public static final String FULLHARVESTS_TABLE = "fullharvests";
    public static final int FULLHARVESTS_TABLE_REQUIRED_VERSION = 5;

    public static final String HARVESTCONFIGS_TABLE = "harvest_configs";
    public static final int HARVESTCONFIGS_TABLE_REQUIRED_VERSION = 1;

    public static final String SCHEDULES_TABLE = "schedules";
    public static final int SCHEDULES_TABLE_REQUIRED_VERSION = 1;

    public static final String ORDERTEMPLATES_TABLE = "ordertemplates"; 
    public static final int ORDERTEMPLATES_TABLE_REQUIRED_VERSION = 1;

    public static final String JOBS_TABLE = "jobs";
    public static final int JOBS_TABLE_REQUIRED_VERSION = 6;

    public static final String JOBCONFIGS_TABLE = "job_configs";
    public static final int JOBCONFIGS_TABLE_REQUIRED_VERSION = 1;

    public static final String GLOBALCRAWLERTRAPLISTS_TABLE = "global_crawler_trap_lists";
    public static final int GLOBALCRAWLERTRAPLISTS_TABLE_REQUIRED_VERSION = 1;

    public static final String GLOBALCRAWLERTRAPEXPRESSIONS_TABLE 
    = "global_crawler_trap_expressions";
    public static final int GLOBALCRAWLERTRAPEXPRESSIONS_TABLE_REQUIRED_VERSION = 1;

    public static final String RUNNINGJOBSHISTORY_TABLE = "runningjobshistory";
    public static final int RUNNINGJOBSHISTORY_TABLE_REQUIRED_VERSION = 2;

    public static final String RUNNINGJOBSMONITOR_TABLE = "runningjobsmonitor";
    public static final int RUNNINGJOBSMONITOR_TABLE_REQUIRED_VERSION = 2;

    public static final String FRONTIERREPORTMONITOR_TABLE = "frontierreportmonitor"; 
    public static final int FRONTIERREPORTMONITOR_TABLE_REQUIRED_VERSION = 1;

    public static final String EXTENDEDFIELD_TABLE = "extendedfield"; 
    public static final int EXTENDEDFIELD_TABLE_REQUIRED_VERSION = 1;

    public static final String EXTENDEDFIELDVALUE_TABLE = "extendedfieldvalue"; 
    public static final int EXTENDEDFIELDVALUE_TABLE_REQUIRED_VERSION = 1;

    public static final String EXTENDEDFIELDTYPE_TABLE = "extendedfieldtype"; 
    public static final int EXTENDEDFIELDTYPE_TABLE_REQUIRED_VERSION = 1;
    

    /**
     * Get the singleton instance of the DBSpecifics implementation class.
     *
     * @return An instance of DBSpecifics with implementations for a given DB.
     */
    public static synchronized DBSpecifics getInstance() {
        if (instance == null) {
            instance = getInstance(CommonSettings.DB_SPECIFICS_CLASS);
        }
        return instance;
    }

    /**
     * Get a temporary table for short-time use. The table should be disposed of
     * with dropTemporaryTable. The table has two columns domain_name
     * varchar(Constants.MAX_NAME_SIZE) + config_name
     * varchar(Constants.MAX_NAME_SIZE) All rows in the table must be deleted at
     * commit or rollback.
     *
     * @param c
     *            The DB connection to use.
     * @throws SQLException
     *             if there is a problem getting the table.
     * @return The name of the created table
     */
    public abstract String getJobConfigsTmpTable(Connection c)
    throws SQLException;

    /**
     * Dispose of a temporary table gotten with getTemporaryTable. This can be
     * expected to be called from within a finally clause, so it mustn't throw
     * exceptions.
     *
     * @param c
     *            The DB connection to use.
     * @param tableName
     *            The name of the temporarily created table.
     */
    public abstract void dropJobConfigsTmpTable(Connection c, String tableName);

    /**
     * Get the name of the JDBC driver class that handles interfacing to this
     * server.
     *
     * @return The name of a JDBC driver class
     */
    public abstract String getDriverClassName();

    /**
     * Update a table to a newer version, if necessary. This will check the
     * schemaversions table to see the current version and perform a
     * table-specific update if required.
     *
     * @param tableName
     *            The table to update
     * @param toVersion
     *            The version to update the table to.
     * @throws IllegalState
     *             If the table is an unsupported version, and the toVersion is
     *             less than the current version of the table
     * @throws NotImplementedException
     *             If no method exists for migration from current version of the
     *             table to the toVersion of the table.
     * @throws IOFailure
     *             in case of problems in interacting with the database
     */

    public synchronized void updateTable(String tableName, int toVersion) {
        ArgumentNotValid.checkNotNullOrEmpty(tableName, "String tableName");
        ArgumentNotValid.checkPositive(toVersion, "int toVersion");

        Connection c = HarvestDBConnection.get();
        int currentVersion = -1;
        try {
            currentVersion = DBUtils.getTableVersion(c, tableName);
        } finally {
            HarvestDBConnection.release(c);
        }
        if (currentVersion == toVersion) {
            // Nothing to do. Version of table is already correct.
            return;
        }
        log.info("Trying to migrate table '" + tableName + "' from version '"
                + currentVersion + "' to version '" + toVersion + "'.");

        if (currentVersion > toVersion) {
            throw new IllegalState("Database is in an illegalState: "
                    + "The current version of table '" + tableName
                    + "' is not acceptable "
                    + "(current version is greater than requested version).");
        }

        if (tableName.equals(JOBS_TABLE)) {
            upgradeJobsTable(currentVersion, toVersion);
        } else if (tableName.equals(FULLHARVESTS_TABLE)) {
            upgradeFullharvestsTable(currentVersion, toVersion);
        } else if (tableName.equals(CONFIGURATIONS_TABLE)) {
            upgradeConfigurationsTable(currentVersion, toVersion);
        } else if (tableName.equals(GLOBALCRAWLERTRAPLISTS_TABLE)) {
            upgradeGlobalcrawlertraplistsTable(currentVersion, toVersion);
        } else if (tableName.equals(GLOBALCRAWLERTRAPEXPRESSIONS_TABLE)) {
            upgradeGlobalcrawlertrapexpressionsTable(currentVersion, toVersion);
        } else if (tableName.equals(RUNNINGJOBSHISTORY_TABLE)) {
            upgradeRunningjobshistoryTable(currentVersion, toVersion);
        } else if (tableName.equals(RUNNINGJOBSMONITOR_TABLE)) {
            upgradeRunningjobsmonitor(currentVersion, toVersion);
        } else if (tableName.equals(FRONTIERREPORTMONITOR_TABLE)) {
            upgradeFrontierreportmonitorTable(currentVersion, toVersion);
        } else if (tableName.equals(EXTENDEDFIELD_TABLE)) {
            upgradeExtendedFieldTable(currentVersion, toVersion);
        } else if (tableName.equals(EXTENDEDFIELDVALUE_TABLE)) {
            upgradeExtendedFieldValueTable(currentVersion, toVersion);
        } else if (tableName.equals(EXTENDEDFIELDTYPE_TABLE)) {
            upgradeExtendedFieldTypeTable(currentVersion, toVersion);
            // Add new if else when other tables need to be upgraded
        } else {
            throw new NotImplementedException(
                    "No method exists for migrating table '" + tableName
                    + "' to version " + toVersion);
        }
    }

    private void upgradeExtendedFieldTypeTable(int currentVersion, int toVersion) {
        if (currentVersion == 0 && toVersion == 1) {
            createExtendedFieldTypeTable();
            currentVersion = 1;
        }
        if (currentVersion > EXTENDEDFIELDTYPE_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '"
                            + EXTENDEDFIELDTYPE_TABLE + "' from version "
                            + currentVersion + " to version " + toVersion);
        }
    }

    private void upgradeExtendedFieldValueTable(int currentVersion,
            int toVersion) {
        if (currentVersion == 0 && toVersion == 1) {
            createExtendedFieldValueTable();
            currentVersion = 1;
        }
        if (currentVersion > EXTENDEDFIELDVALUE_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '"
                            + EXTENDEDFIELDVALUE_TABLE + "' from version "
                            + currentVersion + " to version " + toVersion);
        }
    }

    private void upgradeExtendedFieldTable(int currentVersion, int toVersion) {
        if (currentVersion == 0 && toVersion == 1) {
            createExtendedFieldTable();
            currentVersion = 1;
        }
        if (currentVersion > EXTENDEDFIELD_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '"
                            + EXTENDEDFIELD_TABLE + "' from version "
                            + currentVersion + " to version " + toVersion);
        }
    }

    /** Migrate the frontierreportmonitor table.
     * 
     * @param currentVersion the current version of the frontierreportmonitor 
     * table
     * @param toVersion the required version of the frontierreportmonitor table
     */
    private void upgradeFrontierreportmonitorTable(int currentVersion,
            int toVersion) {
        if (currentVersion == 0 && toVersion == 1) {
            createFrontierReportMonitorTable();
            currentVersion = 1;
        }
        // insert new migrations here
        if (currentVersion > FRONTIERREPORTMONITOR_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '"
                    + FRONTIERREPORTMONITOR_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }    
    }
    
    /** Migrate the runningjobsmonitor table.
     * 
     * @param currentVersion the current version of the runningjobsmonitor table
     * @param toVersion the required version of the runningjobsmonitor table
     */
    private void upgradeRunningjobsmonitor(int currentVersion, int toVersion) {
        if (currentVersion == 0 && toVersion >= 1) {
            createRunningJobsMonitorTable();
            currentVersion = 1;
        }
        if (currentVersion == 1 && toVersion >= 2) {
            migrateRunningJobsMonitorTableV1ToV2();
            currentVersion = 2;
        }
        if (currentVersion > RUNNINGJOBSMONITOR_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '" 
                    + RUNNINGJOBSMONITOR_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }
    }
    
    /** Migrate the runningjobshistory table.
     * 
     * @param currentVersion the current version of the runningjobshistory table
     * @param toVersion The required version of the runningjobshistory table
     */
    private void upgradeRunningjobshistoryTable(int currentVersion,
            int toVersion) {
        if (currentVersion == 0 && toVersion >= 1) {
            createRunningJobsHistoryTable();
            currentVersion = 1;
        }
        if (currentVersion == 1 && toVersion >= 2) {
            migrateRunningJobsHistoryTableV1ToV2();
            currentVersion = 2;
        }
        // insert new migrations here
        if (currentVersion > RUNNINGJOBSHISTORY_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '" 
                    + RUNNINGJOBSHISTORY_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }

    }
    /** Migrate the globalecrawlertrapexpressions table.
     * 
     * @param currentVersion the current version of the jobs table
     * @param toVersion The required version of the jobs table
     */
    private void upgradeGlobalcrawlertrapexpressionsTable(int currentVersion,
            int toVersion) {
        if (currentVersion == 0 && toVersion >= 1) {
            createGlobalCrawlerTrapExpressions();
            currentVersion = 1;
        }
        // insert new migrations here
        if (currentVersion 
                > GLOBALCRAWLERTRAPEXPRESSIONS_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '" 
                    + GLOBALCRAWLERTRAPEXPRESSIONS_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }

    }
    /** Migrate the globalecrawlertraplists table.
     * 
     * @param currentVersion the current version of the 
     *  globalecrawlertraplists table
     * @param toVersion The required version of the 
     * globalecrawlertraplists table
     */
    private void upgradeGlobalcrawlertraplistsTable(int currentVersion,
            int toVersion) {
        if (currentVersion == 0 && toVersion >= 1) {
            createGlobalCrawlerTrapLists();
            currentVersion = 1;
        }
        // insert new migrations here
        if (currentVersion > GLOBALCRAWLERTRAPLISTS_TABLE_REQUIRED_VERSION) {
            throw new NotImplementedException(
                    "No method exists for migrating table '" 
                    + GLOBALCRAWLERTRAPLISTS_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }

    } 

    /** Migrate the jobs table.
     * 
     * @param currentVersion the current version of the jobs table
     * @param toVersion The required version of the jobs table
     */
    private void upgradeJobsTable(int currentVersion, int toVersion) {
        if (currentVersion < 3) {
            throw new IllegalState("Database is in an illegalState: "
                    + "The current version " + currentVersion
                    + " of table '" + JOBS_TABLE + "' is not acceptable. "
                    + "(current version is less than open source version).");
        }
        if (currentVersion == 3 && toVersion >= 4) {
            migrateJobsv3tov4();
            currentVersion = 4;
        }
        if (currentVersion == 4 && toVersion >= 5) {
            migrateJobsv4tov5();
            currentVersion = 5;
        }
        if (currentVersion == 5 && toVersion >= 6) {
            migrateJobsv5tov6();
            currentVersion = 6;
        }
        // future updates of the jobs table are inserted here
        if (currentVersion == JOBS_TABLE_REQUIRED_VERSION 
                && toVersion >= JOBS_TABLE_REQUIRED_VERSION + 1) {
            throw new NotImplementedException(
                    "No method exists for migrating table '" + JOBS_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }

        if (currentVersion > JOBS_TABLE_REQUIRED_VERSION) {
            throw new IllegalState("Database is in an illegalState: "
                    + "The current version (" + currentVersion
                    + ") of table '" + JOBS_TABLE
                    + "' is not an acceptable/known version. ");
        }

    }

    /** Migrate the configurations table.
     * 
     * @param currentVersion the current version of the configurations table
     * @param toVersion the required version of the configurations table
     */
    private void upgradeConfigurationsTable(int currentVersion, int toVersion) {
        if (currentVersion < 3) {
            throw new IllegalState("Database is in an illegalState: "
                    + "The current version " + currentVersion
                    + " of table '" + CONFIGURATIONS_TABLE 
                    + "' is not acceptable. "
                    + "(current version is less than open source version).");
        }
        if (currentVersion == 3 && toVersion >= 4) {
            migrateConfigurationsv3ov4();
            currentVersion = 4;
        }

        if (currentVersion == 4 && toVersion >= 5) {
            migrateConfigurationsv4tov5();
            currentVersion = 5;
        }
        // insert future migrations here
        if (currentVersion == CONFIGURATIONS_TABLE_REQUIRED_VERSION 
                && toVersion >= CONFIGURATIONS_TABLE_REQUIRED_VERSION + 1) {
            throw new NotImplementedException(
                    "No method exists for migrating table '" 
                    + CONFIGURATIONS_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }

        if (currentVersion > CONFIGURATIONS_TABLE_REQUIRED_VERSION) {
            throw new IllegalState("Database is in an illegalState: "
                    + "The current version (" + currentVersion
                    + ") of table '" + CONFIGURATIONS_TABLE
                    + "' is not an acceptable/known version. ");
        }
    }   

    /** Migrate the fullharvests table.
     * 
     * @param currentVersion the current version of the fullharvests table
     * @param toVersion the required version of the fullharvests table
     */
    private void upgradeFullharvestsTable(int currentVersion, int toVersion) {
        if (currentVersion < 2) {
            throw new IllegalState("Database is in an illegalState: "
                    + "The current version " + currentVersion
                    + " of table '" + FULLHARVESTS_TABLE 
                    + "' is not acceptable. "
                    + "(current version is less than open source version).");
        }
        if (currentVersion == 2 && toVersion >= 3) {
            migrateFullharvestsv2tov3();
            currentVersion = 3;
        }

        if (currentVersion == 3 && toVersion >= 4) {
            migrateFullharvestsv3tov4();
            currentVersion = 4;
        }
        
        if (currentVersion == 4 && toVersion >= 5) {
            migrateFullharvestsv4tov5();
            currentVersion = 5;
        }
        
        // insert future migrations here
        if (currentVersion == FULLHARVESTS_TABLE_REQUIRED_VERSION 
                && toVersion >= FULLHARVESTS_TABLE_REQUIRED_VERSION + 1) {
            throw new NotImplementedException(
                    "No method exists for migrating table '" 
                    + FULLHARVESTS_TABLE
                    + "' from version " + currentVersion
                    + " to version " + toVersion);
        }

    }

    /**
     * Migrates the 'jobs' table from version 3 to version 4 consisting of a
     * change of the field forcemaxbytes from int to bigint and setting its
     * default to -1. Furthermore the default value for field num_configs is set
     * to 0.
     *
     * @throws IOFailure
     *             in case of problems in interacting with the database
     */
    protected abstract void migrateJobsv3tov4();

    /**
     * Migrates the 'jobs' table from version 4 to version 5 consisting of
     * adding new fields 'resubmitted_as_job' and 'submittedDate'.
     *
     * @throws IOFailure
     *             in case of problems in interacting with the database
     */
    protected abstract void migrateJobsv4tov5();

    /**
     * Migrates the 'configurations' table from version 3 to version 4. This
     * consists of altering the default value of field 'maxbytes' to -1.
     */
    protected abstract void migrateConfigurationsv3ov4();

    /**
     * Migrates the 'fullharvests' table from version 2 to version 3. This
     * consists of altering the default value of field 'maxbytes' to -1.
     */
    protected abstract void migrateFullharvestsv2tov3();

    /**
     * Migrates the 'runningjobshistory' table from version 1 to version 2. This
     * consists of adding the new column 'retiredQueuesCount'.
     */
    protected abstract void migrateRunningJobsHistoryTableV1ToV2();

    /**
     * Migrates the 'runningjobsmonitor' table from version 1 to version 2. This
     * consists of adding the new column 'retiredQueuesCount'.
     */
    protected abstract void migrateRunningJobsMonitorTableV1ToV2();

    /**
     * Creates the initial (version 1) of table 'global_crawler_trap_lists'.
     */
    protected abstract void createGlobalCrawlerTrapLists();

    /**
     * Creates the initial (version 1) of table
     * 'global_crawler_trap_expressions'.
     */
    protected abstract void createGlobalCrawlerTrapExpressions();

    /**
     * Formats the LIMIT sub-clause of an SQL order clause. This sub-clause
     * allows to paginate query results and its syntax might be dependant on the
     * target RDBMS
     *
     * @param limit
     *            the maximum number of rows to fetch.
     * @param offset
     *            the starting offset in the full query results.
     * @return the proper sub-clause.
     */
    public abstract String getOrderByLimitAndOffsetSubClause(long limit,
            long offset);

    /**
     * Returns true if the target RDBMS supports CLOBs. If possible seedlists
     * will be stored as CLOBs.
     *
     * @return true if CLOBs are supported, false otherwise.
     */
    public abstract boolean supportsClob();

    /**
     * Create the frontierReportMonitor table in the database.
     */
    public abstract void createFrontierReportMonitorTable();

    /**
     * Create the frontierReportMonitor table in the database.
     */
    public abstract void createRunningJobsHistoryTable();
    /**
     * Create the frontierReportMonitor table in the database.
     */
    public abstract void createRunningJobsMonitorTable();

    /**
     * Migrates the 'jobs' table from version 5 to version 6.
     * Adds the field 'forcemaxrunningtime'.
     *
     * @throws IOFailure
     *             in case of problems in interacting with the database
     */
    protected abstract void migrateJobsv5tov6();

    /**
     * Migrates the 'configurations' table from version 4 to version 5. This
     * consists of altering the field 'maxobjects' from being an int to a 
     * bigint.
     */
    protected abstract void migrateConfigurationsv4tov5();

    /**
     * Migrates the 'fullharvests' table from version 3 to version 4. This
     * consists of adding the field 'maxjobrunningtime'.
     */
    protected abstract void migrateFullharvestsv3tov4();

    /**
     * Migrates the 'fullharvests' table from version 4 to version 5. This
     * consists of adding the field 'isindexready'.
     */
    protected abstract void migrateFullharvestsv4tov5();
    
    /**
     * Create the extendedfieldtype table in the database.
     */
    protected abstract void createExtendedFieldTypeTable();
    
    /**
     * Create the extendedfield table in the database.
     */
    protected abstract void createExtendedFieldTable();

    /**
     * Create the extendedfieldvalue table in the database.
     */
    protected abstract void createExtendedFieldValueTable();
}
