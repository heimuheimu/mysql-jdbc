/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.mysql.jdbc;

import com.heimuheimu.mysql.jdbc.constant.DriverVersion;
import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;

import java.sql.*;

/**
 * Mysql 数据库信息。
 *
 * <p><strong>说明：</strong>{@code MysqlDatabaseMetaData} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlDatabaseMetaData implements DatabaseMetaData {

    private final MysqlConnection mysqlConnection;

    public MysqlDatabaseMetaData(MysqlConnection mysqlConnection) {
        this.mysqlConnection = mysqlConnection;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return false;
    }

    @Override
    public String getURL() throws SQLException {
        ConnectionConfiguration configuration = mysqlConnection.getMysqlChannel().getConnectionConfiguration();
        String databaseName = configuration.getDatabaseName() != null ? configuration.getDatabaseName() : "";
        String url = "jdbc:mysql://" + configuration.getHost() + "/" + databaseName;
        return url;
    }

    @Override
    public String getUserName() throws SQLException {
        ConnectionConfiguration configuration = mysqlConnection.getMysqlChannel().getConnectionConfiguration();
        return configuration.getUsername();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return true;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return "MySQL";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return mysqlConnection.getMysqlChannel().getConnectionInfo().getServerVersion();
    }

    @Override
    public String getDriverName() throws SQLException {
        return "MySQL Connector/J";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return DriverVersion.DRIVER_VERSION;
    }

    @Override
    public int getDriverMajorVersion() {
        return DriverVersion.DRIVER_MAJOR_VERSION;
    }

    @Override
    public int getDriverMinorVersion() {
        return DriverVersion.DRIVER_MINOR_VERSION;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return " ";
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return ""; // boring method
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return ""; // boring method
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return ""; // boring method
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return ""; // boring method
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return ""; // boring method
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return "#@";
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return "";
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return "PROCEDURE";
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return "database";
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return true;
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return ".";
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 16777208;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 16777208;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 16;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 256;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 512;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return 256;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 32;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return Integer.MAX_VALUE - 8;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 64;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 256;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 16;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_READ_COMMITTED;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        switch (level) {
            case Connection.TRANSACTION_READ_COMMITTED:
            case Connection.TRANSACTION_READ_UNCOMMITTED:
            case Connection.TRANSACTION_REPEATABLE_READ:
            case Connection.TRANSACTION_SERIALIZABLE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return true;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getProcedures(String catalog, String schemaPattern, String procedureNamePattern)");
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)");
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)");
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getSchemas()");
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getCatalogs()");
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getTableTypes()");
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)");
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)");
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)");
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)");
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getVersionColumns(String catalog, String schema, String table)");
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getPrimaryKeys(String catalog, String schema, String table)");
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getImportedKeys(String catalog, String schema, String table)");
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getExportedKeys(String catalog, String schema, String table)");
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable)");
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getTypeInfo()");
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)");
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        return type == ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return type == ResultSet.TYPE_SCROLL_INSENSITIVE && concurrency == ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return mysqlConnection;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return true;
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)");
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getSuperTables(String catalog, String schemaPattern, String tableNamePattern)");
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern)");
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getResultSetHoldability()");
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 2;
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return DatabaseMetaData.sqlStateSQL99;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#locatorsUpdateCopy()");
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getRowIdLifetime()");
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getSchemas(String catalog, String schemaPattern)");
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getClientInfoProperties()");
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getFunctions(String catalog, String schemaPattern, String functionNamePattern)");
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern)");
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)");
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return MysqlDatabaseMetaData.class == iface;
    }
}
