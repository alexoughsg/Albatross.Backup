// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.uuididentity.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.utils.Pair;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.TransactionLegacy;

@Component
@Local(value = {IdentityDao.class})
public class IdentityDaoImpl extends GenericDaoBase<IdentityVO, Long> implements IdentityDao {
    private static final Logger s_logger = Logger.getLogger(IdentityDaoImpl.class);

    public IdentityDaoImpl() {
    }

    @Override
    @DB
    public Long getIdentityId(String tableName, String identityString) {
        assert (tableName != null);
        assert (identityString != null);

        PreparedStatement pstmt = null;
        TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
        try {
            try {
                try {
                    pstmt = txn.prepareAutoCloseStatement(String.format("SELECT uuid FROM `%s`", tableName));
                    pstmt.executeQuery();
                } catch (SQLException e) {
                    throw new InvalidParameterValueException("uuid field doesn't exist in table " + tableName);
                }

                pstmt = txn.prepareAutoCloseStatement(String.format("SELECT id FROM `%s` WHERE id=? OR uuid=?", tableName)

                    // TODO : after graceful period, use following line turn on more secure check
                    // String.format("SELECT id FROM %s WHERE (id=? AND uuid IS NULL) OR uuid=?", mapper.entityTableName())
                    );

                long id = 0;
                try {
                    // TODO : use regular expression to determine
                    id = Long.parseLong(identityString);
                } catch (NumberFormatException e) {
                    // this could happen when it is a uuid string, so catch and ignore it
                }

                pstmt.setLong(1, id);
                pstmt.setString(2, identityString);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    if (id == -1L)
                        return id;

                    throw new InvalidParameterValueException("Object " + tableName + "(uuid: " + identityString + ") does not exist.");
                }
            } catch (SQLException e) {
                s_logger.error("Unexpected exception ", e);
            }
        } finally {
            txn.close();
        }
        return null;
    }

    @DB
    @Override
    public Pair<Long, Long> getAccountDomainInfo(String tableName, Long identityId, ResourceObjectType resourceType) {
        assert (tableName != null);

        PreparedStatement pstmt = null;
        TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
        try {
            Long domainId = null;
            Long accountId = null;
            //get domainId
            try {
                pstmt = txn.prepareAutoCloseStatement(String.format("SELECT domain_id FROM `%s` WHERE id=?", tableName));
                pstmt.setLong(1, identityId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (rs.getLong(1) != 0) {
                        domainId = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
            }

            //get accountId
            try {
                String account = "account_id";
                if (resourceType == ResourceObjectType.Project) {
                    account = "project_account_id";
                }
                pstmt = txn.prepareAutoCloseStatement(String.format("SELECT " + account + " FROM `%s` WHERE id=?", tableName));
                pstmt.setLong(1, identityId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (rs.getLong(1) != 0) {
                        accountId = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
            }
            return new Pair<Long, Long>(accountId, domainId);
        } finally {
            txn.close();
        }
    }

    @DB
    @Override
    public String getIdentityUuid(String tableName, String identityString) {
        assert (tableName != null);
        assert (identityString != null);

        PreparedStatement pstmt = null;
        TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
        try {
            try {
                pstmt = txn.prepareAutoCloseStatement(String.format("SELECT uuid FROM `%s` WHERE id=? OR uuid=?", tableName)
                    // String.format("SELECT uuid FROM %s WHERE (id=? AND uuid IS NULL) OR uuid=?", tableName)
                    );

                long id = 0;
                try {
                    // TODO : use regular expression to determine
                    id = Long.parseLong(identityString);
                } catch (NumberFormatException e) {
                    // this could happen when it is a uuid string, so catch and ignore it
                }

                pstmt.setLong(1, id);
                pstmt.setString(2, identityString);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String uuid = rs.getString(1);
                    if (uuid != null && !uuid.isEmpty())
                        return uuid;
                    return identityString;
                }
            } catch (SQLException e) {
                s_logger.error("Unexpected exception ", e);
            }
        } finally {
            txn.close();
        }

        return identityString;
    }

    @Override
    @DB
    public void initializeDefaultUuid(String tableName) {
        assert (tableName != null);
        List<Long> l = getNullUuidRecords(tableName);

        TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
        try {
            try {
                txn.start();
                for (Long id : l) {
                    setInitialUuid(tableName, id);
                }
                txn.commit();
            } catch (SQLException e) {
                txn.rollback();
                s_logger.error("Unexpected exception ", e);
            }
        } finally {
            txn.close();
        }
    }

    @DB
    List<Long> getNullUuidRecords(String tableName) {
        List<Long> l = new ArrayList<Long>();

        PreparedStatement pstmt = null;
        TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
        try {
            try {
                pstmt = txn.prepareAutoCloseStatement(String.format("SELECT id FROM `%s` WHERE uuid IS NULL", tableName));

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    l.add(rs.getLong(1));
                }
            } catch (SQLException e) {
                s_logger.error("Unexpected exception ", e);
            }
        } finally {
            txn.close();
        }
        return l;
    }

    @DB
    void setInitialUuid(String tableName, long id) throws SQLException {
        TransactionLegacy txn = TransactionLegacy.currentTxn();

        PreparedStatement pstmtUpdate = null;
        pstmtUpdate = txn.prepareAutoCloseStatement(String.format("UPDATE `%s` SET uuid=? WHERE id=?", tableName));

        pstmtUpdate.setString(1, String.valueOf(id));
        pstmtUpdate.setLong(2, id);
        pstmtUpdate.executeUpdate();
    }
}
