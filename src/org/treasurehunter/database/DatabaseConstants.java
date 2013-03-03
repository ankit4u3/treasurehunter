/*
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package org.treasurehunter.database;

/**
 * Database version history: 12: Add tables LABELS and CACHELABELS 13: Replace
 * LABELS and CACHELABELS with TAGS and CACHETAGS 14: Add primary key to
 * CACHETAGS to avoid duplicate rows 15: Add tables LOGS and TRAVELBUGS. Add
 * CACHES columns ShortDesc, LongDesc, Hints, LastModifiedDate. Add index on
 * CACHETAGS.CacheId 16: Add CACHES columns CreationDate, Owner 17: Add CACHES
 * column PlacedBy 18: Create table USERNOTES 19: (Not used) 20: Create table
 * WAYPOINTS
 */
public class DatabaseConstants {
    public static final int DATABASE_VERSION = 20;

    public static final String S0_COLUMN_CACHE_TYPE = "CacheType INTEGER NOT NULL Default 0";

    public static final String S0_COLUMN_CONTAINER = "Container INTEGER NOT NULL Default 0";

    public static final String S0_COLUMN_DELETE_ME = "DeleteMe BOOLEAN NOT NULL Default 1";

    public static final String S0_COLUMN_DIFFICULTY = "Difficulty INTEGER NOT NULL Default 0";

    public static final String S0_COLUMN_TERRAIN = "Terrain INTEGER NOT NULL Default 0";

    // ///// CREATE TABLE ////////
    public static final String SQL_CREATE_CACHE_TABLE_V08 = "CREATE TABLE CACHES ("
            + "Id VARCHAR PRIMARY KEY, Description VARCHAR, "
            + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR);";

    public static final String SQL_CREATE_CACHE_TABLE_V11 = "CREATE TABLE CACHES ("
            + "Id VARCHAR PRIMARY KEY, Description VARCHAR, "
            + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR, " + S0_COLUMN_DELETE_ME + ", "
            + S0_COLUMN_CACHE_TYPE + ", " + S0_COLUMN_CONTAINER + ", " + S0_COLUMN_DIFFICULTY
            + ", " + S0_COLUMN_TERRAIN + ");";

    public static final String SQL_CREATE_CACHE_TABLE_V15 = "CREATE TABLE CACHES ("
            + "Id VARCHAR PRIMARY KEY, Description VARCHAR, "
            + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR, " + S0_COLUMN_DELETE_ME + ", "
            + S0_COLUMN_CACHE_TYPE + ", " + S0_COLUMN_CONTAINER + ", " + S0_COLUMN_DIFFICULTY
            + ", " + S0_COLUMN_TERRAIN
            + ", ShortDesc VARCHAR, LongDesc VARCHAR, Hints VARCHAR, LastModifiedDate DATETIME);";

    public static final String SQL_CREATE_CACHE_TABLE_V16 = "CREATE TABLE CACHES ("
            + "Id VARCHAR PRIMARY KEY, Description VARCHAR, "
            + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR, " + S0_COLUMN_DELETE_ME + ", "
            + S0_COLUMN_CACHE_TYPE + ", " + S0_COLUMN_CONTAINER + ", " + S0_COLUMN_DIFFICULTY
            + ", " + S0_COLUMN_TERRAIN + ", ShortDesc VARCHAR, LongDesc VARCHAR, Hints VARCHAR, "
            + "LastModifiedDate DATETIME, CreationDate DATETIME, Owner VARCHAR);";

    public static final String SQL_CREATE_CACHE_TABLE_V17 = "CREATE TABLE CACHES ("
            + "Id VARCHAR PRIMARY KEY, Description VARCHAR, "
            + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR, " + S0_COLUMN_DELETE_ME + ", "
            + S0_COLUMN_CACHE_TYPE + ", " + S0_COLUMN_CONTAINER + ", " + S0_COLUMN_DIFFICULTY
            + ", " + S0_COLUMN_TERRAIN + ", ShortDesc VARCHAR, LongDesc VARCHAR, Hints VARCHAR, "
            + "LastModifiedDate DATETIME, CreationDate DATETIME, Owner VARCHAR, PlacedBy VARCHAR);";

    public static final String SQL_CREATE_GPX_TABLE_V10 = "CREATE TABLE GPX ("
            + "Name VARCHAR PRIMARY KEY NOT NULL, ExportTime DATETIME NOT NULL, DeleteMe BOOLEAN NOT NULL);";

    public static final String SQL_CREATE_TAGS_TABLE_V12 = "CREATE TABLE TAGS ("
            + "Id VARCHAR PRIMARY KEY NOT NULL, Name VARCHAR NOT NULL, Locked BOOLEAN NOT NULL);";

    public static final String SQL_CREATE_CACHETAGS_TABLE_V12 = "CREATE TABLE CACHETAGS ("
            + "CacheId VARCHAR NOT NULL, TagId INTEGER NOT NULL);";

    public static final String SQL_CREATE_CACHETAGS_TABLE_V14 = "CREATE TABLE CACHETAGS ("
            + "CacheId VARCHAR NOT NULL, TagId INTEGER NOT NULL, PRIMARY KEY (CacheId, TagId))";

    public static final String SQL_CREATE_LOGS_TABLE = "CREATE TABLE LOGS "
            + "(LogId INTEGER PRIMARY KEY, CacheId VARCHAR NOT NULL, LogType INTEGER, "
            + "LogDate DATETIME NOT NULL, FinderName VARCHAR, Text VARCHAR, "
            + "IsTextEncoded BOOLEAN NOT NULL)";

    public static final String SQL_CREATE_TB_TABLE = "CREATE TABLE TRAVELBUGS "
            + "(TbId VARCHAR PRIMARY KEY, CacheId VARCHAR, Ref VARCHAR, Name VARCHAR)";

    public static final String SQL_CREATE_USERNOTES_TABLE = "CREATE TABLE USERNOTES "
            + "(NoteId INTEGER PRIMARY KEY, CacheId VARCHAR NOT NULL, NoteType INTEGER, "
            + "NoteDate DATETIME NOT NULL, UserName VARCHAR, Text VARCHAR, "
            + "IsTextEncoded BOOLEAN NOT NULL)";

    public static final String SQL_CREATE_WAYPOINTS_TABLE = "CREATE TABLE WAYPOINTS "
            + "(Id VARCHAR PRIMARY KEY, Name VARCHAR, "
            + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR, " + S0_COLUMN_DELETE_ME + ", "
            + S0_COLUMN_CACHE_TYPE + ", "
            + "LastModifiedDate DATETIME, CreationDate DATETIME, ParentId VARCHAR);";

    // ///// CREATE INDEX ////////
    public static final String SQL_CREATE_IDX_CACHETAGS = "CREATE INDEX IDX_CACHETAGS on CACHETAGS (CacheId, TagId);";

    public static final String SQL_CREATE_IDX_LATITUDE = "CREATE INDEX IDX_LATITUDE on CACHES (Latitude);";

    public static final String SQL_CREATE_IDX_LONGITUDE = "CREATE INDEX IDX_LONGITUDE on CACHES (Longitude);";

    public static final String SQL_CREATE_IDX_SOURCE = "CREATE INDEX IDX_SOURCE on CACHES (Source);";

    public static final String SQL_CREATE_IDX_LOGS_CACHEID = "CREATE INDEX IDX_LOGS_CACHEID on LOGS (CacheId)";

    public static final String SQL_CREATE_IDX_TB_CACHEID = "CREATE INDEX IDX_TRAVELBUGS_CACHEID on TRAVELBUGS (CacheId)";

    public static final String SQL_CREATE_IDX_CACHETAGS_CACHEID = "CREATE INDEX IDX_CACHETAGS_CACHEID on CACHETAGS (CacheId);";

    // ///// DELETE ///////
    public static final String SQL_DELETE_CACHE = "DELETE FROM CACHES WHERE Id=?";

    public static final String SQL_DELETE_WAYPOINT = "DELETE FROM WAYPOINTS WHERE Id=?";

    public static final String SQL_DELETE_OLD_CACHES = "DELETE FROM CACHES WHERE DeleteMe = 1";

    public static final String SQL_DELETE_OLD_GPX = "DELETE FROM GPX WHERE DeleteMe = 1";

    public static final String SQL_DELETE_GPX = "DELETE FROM GPX WHERE Name = ?";

    public static final String SQL_DELETE_CACHE_TAGS = "DELETE FROM CACHETAGS WHERE CacheId = ?";

    public static final String SQL_DELETE_CACHETAG = "DELETE FROM CACHETAGS WHERE CacheId = ? AND TagId = ?";

    public static final String SQL_DELETE_ALL_TAGS = "DELETE FROM CACHETAGS WHERE TagId = ?";

    public static final String SQL_DELETE_ALL_CACHES = "DELETE FROM CACHES";

    public static final String SQL_DELETE_ALL_GPX = "DELETE FROM GPX";

    public static final String SQL_DELETE_ALL_LOGS = "DELETE FROM LOGS";

    public static final String SQL_DELETE_ALL_TRAVELBUGS = "DELETE FROM TRAVELBUGS";

    public static final String SQL_DELETE_ALL_WAYPOINTS = "DELETE FROM WAYPOINTS";

    public static final String SQL_DELETE_CACHE_LOGS = "DELETE FROM LOGS WHERE CacheId = ?";

    public static final String SQL_DELETE_CACHE_TRAVELBUGS = "DELETE FROM TRAVELBUGS WHERE CacheId = ?";

    public static final String SQL_DELETE_CACHE_WAYPOINTS = "DELETE FROM WAYPOINTS WHERE ParentId = ?";

    // // DELETE from source ////
    public static final String SQL_DELETE_LOGS_FROM_SOURCE = "DELETE FROM logs WHERE EXISTS (select * from caches where caches.id = logs.CacheId AND caches.source = ?)";

    public static final String SQL_DELETE_TRAVELBUGS_FROM_SOURCE = "DELETE FROM travelbugs WHERE EXISTS (select * from caches where caches.id = travelbugs.CacheId AND caches.source = ?)";

    public static final String SQL_DELETE_TAGS_FROM_SOURCE = "DELETE FROM cachetags WHERE EXISTS (select * from caches where caches.id = cachetags.CacheId AND caches.source = ?)";

    public static final String SQL_DELETE_CACHES_FROM_SOURCE = "DELETE FROM CACHES WHERE Source = ?";

    public static final String SQL_DELETE_WAYPOINTS_FROM_SOURCE = "DELETE FROM WAYPOINTS WHERE Source = ?";

    public static final String SQL_DROP_CACHE_TABLE = "DROP TABLE IF EXISTS CACHES";

    public static final String SQL_GPX_DONT_DELETE_ME = "UPDATE GPX SET DeleteMe = 0 WHERE Name = ?";

    public static final String SQL_CACHES_DONT_DELETE_ID = "UPDATE CACHES SET DeleteMe = 0 WHERE Id = ?";

    public static final String SQL_MATCH_NAME_AND_EXPORTED_LATER = "Name = ? AND ExportTime >= ?";

    public static final String SQL_CACHES_DONT_DELETE_SOURCE = "UPDATE CACHES SET DeleteMe = 0 WHERE Source = ?";

    // ///// REPLACE / UPDATE ///////
    public static final String SQL_REPLACE_CACHE = "REPLACE INTO CACHES "
            + "(Id, Description, Latitude, Longitude, Source, DeleteMe, CacheType, "
            + "Difficulty, Terrain, Container, LastModifiedDate, CreationDate) "
            + "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_UPDATE_CACHE_ATTRS = "UPDATE CACHES SET "
            + "Description=?, Latitude=?, Longitude=?, LastModifiedDate=? " + "WHERE Id=?";

    public static final String SQL_REPLACE_CACHE_ALL = "REPLACE INTO CACHES "
            + "(Id, Description, Latitude, Longitude, Source, DeleteMe, CacheType, "
            + "Difficulty, Terrain, Container, ShortDesc, LongDesc, Hints, "
            + "LastModifiedDate, CreationDate, Owner, PlacedBy) "
            + "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_REPLACE_GPX = "REPLACE INTO GPX (Name, ExportTime, DeleteMe) VALUES (?, ?, 0)";

    public static final String SQL_REPLACE_TAG = "REPLACE INTO TAGS "
            + "(Id, Name, Locked) VALUES (?, ?, ?)";

    public static final String SQL_REPLACE_CACHETAG = "REPLACE INTO CACHETAGS "
            + "(CacheId, TagId) VALUES (?, ?)";

    public static final String SQL_RESET_DELETE_ME_CACHES = "UPDATE CACHES SET DeleteMe = 1 WHERE Source LIKE 'gpx/%' OR Source LIKE 'loc/%'";

    public static final String SQL_SET_DELETE_ME_GPX = "UPDATE GPX SET DeleteMe = 1 WHERE Name LIKE 'gpx/%' OR Name LIKE 'loc/%'";

    public static final String SQL_REPLACE_LOG = "REPLACE INTO LOGS "
            + "(LogId, CacheId, LogType, LogDate, FinderName, Text, IsTextEncoded) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_INSERT_USERNOTE = "INSERT INTO USERNOTES "
            + "(NoteId, CacheId, NoteType, NoteDate, UserName, Text, IsTextEncoded) "
            + "VALUES (NULL, ?, ?, ?, ?, ?, 0)";

    public static final String SQL_DELETE_USERNOTE = "DELETE FROM USERNOTES "
            + "WHERE CacheId=? AND NoteType=?";

    public static final String SQL_GET_LOGS_AND_USERNOTES = "SELECT "
            + "LogId, LogType, LogDate, FinderName, Text, IsTextEncoded FROM LOGS "
            + "WHERE CacheId=? "
            + "UNION ALL SELECT NoteId AS LogId, NoteType AS LogType, NoteDate AS LogDate, "
            + "UserName AS FinderName, Text, IsTextEncoded FROM USERNOTES " + "WHERE CacheId=? "
            + "ORDER BY LogDate DESC, LogId DESC";

    public static final String SQL_REPLACE_TRAVELBUG = "REPLACE INTO TRAVELBUGS "
            + "(TbId, CacheId, Ref, Name) VALUES (?, ?, ?, ?)";

    public static final String SQL_REPLACE_WAYPOINT = "REPLACE INTO WAYPOINTS "
            + "(Id, Name, Latitude, Longitude, Source, DeleteMe, CacheType, LastModifiedDate, CreationDate, ParentId) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?)";

    public static final String SQL_UPDATE_WAYPOINT_ATTRS = "UPDATE WAYPOINTS SET "
            + "Name=?, Latitude=?, Longitude=?, LastModifiedDate=? " + "WHERE Id=?";

    public static final String TBL_CACHES = "CACHES";

    public static final String TBL_GPX = "GPX";

    public static final String TBL_TAGS = "TAGS";

    public static final String TBL_CACHETAGS = "CACHETAGS";

    public static final String TBL_LOGS = "LOGS";

    public static final String TBL_TRAVELBUGS = "TRAVELBUGS";

    public static final String TBL_WAYPOINTS = "WAYPOINTS";

}
