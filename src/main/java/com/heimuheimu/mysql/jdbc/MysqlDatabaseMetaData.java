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
    public boolean allProceduresAreCallable() {
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() {
        return false;
    }

    @Override
    public String getURL() {
        ConnectionConfiguration configuration = mysqlConnection.getMysqlChannel().getConnectionConfiguration();
        String databaseName = configuration.getDatabaseName() != null ? configuration.getDatabaseName() : "";
        return "jdbc:mysql://" + configuration.getHost() + "/" + databaseName;
    }

    @Override
    public String getUserName() {
        ConnectionConfiguration configuration = mysqlConnection.getMysqlChannel().getConnectionConfiguration();
        return configuration.getUsername();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() {
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() {
        return true;
    }

    @Override
    public boolean nullsAreSortedAtStart() {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() {
        return false;
    }

    @Override
    public String getDatabaseProductName() {
        return "MySQL";
    }

    @Override
    public String getDatabaseProductVersion() {
        return mysqlConnection.getMysqlChannel().getConnectionInfo().getServerVersion();
    }

    @Override
    public String getDriverName() {
        return "MySQL Connector/J";
    }

    @Override
    public String getDriverVersion() {
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
    public boolean usesLocalFiles() {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() {
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() {
        return true;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() {
        return true;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() {
        return true;
    }

    @Override
    public String getIdentifierQuoteString() {
        return " ";
    }

    @Override
    public String getSQLKeywords() {
        return ""; // boring method
    }

    @Override
    public String getNumericFunctions() {
        return ""; // boring method
    }

    @Override
    public String getStringFunctions() {
        return ""; // boring method
    }

    @Override
    public String getSystemFunctions() {
        return ""; // boring method
    }

    @Override
    public String getTimeDateFunctions() {
        return ""; // boring method
    }

    @Override
    public String getSearchStringEscape() {
        return "\\";
    }

    @Override
    public String getExtraNameCharacters() {
        return "#@";
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() {
        return true;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() {
        return true;
    }

    @Override
    public boolean supportsColumnAliasing() {
        return true;
    }

    @Override
    public boolean supportsConvert() {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() {
        return true;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        return true;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() {
        return true;
    }

    @Override
    public boolean supportsOrderByUnrelated() {
        return false;
    }

    @Override
    public boolean supportsGroupBy() {
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() {
        return true;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() {
        return true;
    }

    @Override
    public boolean supportsLikeEscapeClause() {
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() {
        return true;
    }

    @Override
    public boolean supportsNonNullableColumns() {
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() {
        return true;
    }

    @Override
    public boolean supportsCoreSQLGrammar() {
        return true;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        return true;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        return true;
    }

    @Override
    public boolean supportsOuterJoins() {
        return true;
    }

    @Override
    public boolean supportsFullOuterJoins() {
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() {
        return true;
    }

    @Override
    public String getSchemaTerm() {
        return "";
    }

    @Override
    public String getProcedureTerm() {
        return "PROCEDURE";
    }

    @Override
    public String getCatalogTerm() {
        return "database";
    }

    @Override
    public boolean isCatalogAtStart() {
        return true;
    }

    @Override
    public String getCatalogSeparator() {
        return ".";
    }

    @Override
    public boolean supportsSchemasInDataManipulation() {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInExists() {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInIns() {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        return true;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() {
        return true;
    }

    @Override
    public boolean supportsUnion() {
        return true;
    }

    @Override
    public boolean supportsUnionAll() {
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() {
        return 16777208;
    }

    @Override
    public int getMaxCharLiteralLength() {
        return 16777208;
    }

    @Override
    public int getMaxColumnNameLength() {
        return 64;
    }

    @Override
    public int getMaxColumnsInGroupBy() {
        return 64;
    }

    @Override
    public int getMaxColumnsInIndex() {
        return 16;
    }

    @Override
    public int getMaxColumnsInOrderBy() {
        return 64;
    }

    @Override
    public int getMaxColumnsInSelect() {
        return 256;
    }

    @Override
    public int getMaxColumnsInTable() {
        return 512;
    }

    @Override
    public int getMaxConnections() {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() {
        return 64;
    }

    @Override
    public int getMaxIndexLength() {
        return 256;
    }

    @Override
    public int getMaxSchemaNameLength() {
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() {
        return 32;
    }

    @Override
    public int getMaxRowSize() {
        return Integer.MAX_VALUE - 8;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        return true;
    }

    @Override
    public int getMaxStatementLength() {
        return 0;
    }

    @Override
    public int getMaxStatements() {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() {
        return 64;
    }

    @Override
    public int getMaxTablesInSelect() {
        return 256;
    }

    @Override
    public int getMaxUserNameLength() {
        return 16;
    }

    @Override
    public int getDefaultTransactionIsolation() {
        return Connection.TRANSACTION_READ_COMMITTED;
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) {
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
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        return true;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
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
    public boolean supportsResultSetType(int type) {
        return type == ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        return type == ResultSet.TYPE_SCROLL_INSENSITIVE && concurrency == ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() {
        return false;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)");
    }

    @Override
    public Connection getConnection() {
        return mysqlConnection;
    }

    @Override
    public boolean supportsSavepoints() {
        return true;
    }

    @Override
    public boolean supportsNamedParameters() {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
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
    public boolean supportsResultSetHoldability(int holdability) {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#getResultSetHoldability()");
    }

    @Override
    public int getDatabaseMajorVersion() {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() {
        return 0;
    }

    @Override
    public int getJDBCMajorVersion() {
        return 4;
    }

    @Override
    public int getJDBCMinorVersion() {
        return 2;
    }

    @Override
    public int getSQLStateType() {
        return DatabaseMetaData.sqlStateSQL99;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDatabaseMetaData#locatorsUpdateCopy()");
    }

    @Override
    public boolean supportsStatementPooling() {
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
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
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
    public boolean generatedKeyAlwaysReturned() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return MysqlDatabaseMetaData.class == iface;
    }
}
