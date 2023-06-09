package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.util.LocaleString;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.nctigba.datastudio.constants.SqlConstants.*;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Service
public class DbConnectionServiceImpl implements DbConnectionService {

    @Resource
    private DatabaseConnectionDAO databaseConnectionDAO;
    @Resource
    private ConnectionMapDAO connectionMapDAO;
    @Resource
    private MetaDataByJdbcService metaDataByJdbcService;

    @Resource
    private DataListByJdbcService dataListByJdbcService;

    @Override
    public DatabaseConnectionDO addDatabaseConnection(DbConnectionCreateDTO request) {
        if (databaseConnectionDAO.getJudgeName(request.getName(), request.getWebUser()) == 0) {
            DatabaseConnectionDO conn = request.toDatabaseConnection();
            try {
                conn.setEdition(test(request));
                databaseConnectionDAO.insertTable(conn);
                DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(), request.getWebUser());
                String uuid = UUID.randomUUID().toString();
                dataList.setConnectionid(uuid);
                ConnectionDTO connectionDTO = new ConnectionDTO();
                connectionDTO.setIpConnectionDTO(dataList);
                ConnectionMapDAO.setConMap(uuid, connectionDTO);
                dataList.setPassword("");
                return dataList;
            } catch (Exception e) {
                throw new CustomException(e.getMessage(), e);
            }
        } else {
            return loginDatabaseConnection(request);
        }
    }

    @Override
    public void deleteDatabaseConnectionList(String id) {
        try {
            databaseConnectionDAO.deleteTable(Integer.parseInt(id));
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), e);
        }
    }

    @Override
    public DatabaseConnectionDO databaseAttributeConnection(String id) {
        try {
            DatabaseConnectionDO atabaseConnectionEntity = databaseConnectionDAO.getAttributeById(id, "A");
            atabaseConnectionEntity.setPassword("");
            return atabaseConnectionEntity;
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), e);
        }
    }

    @Override
    public List<DatabaseConnectionDO> databaseConnectionList(String webUser) {
        try {
            List<DatabaseConnectionDO> databaseConnectionEntity = new ArrayList<>();
            databaseConnectionEntity = databaseConnectionDAO.selectTable(webUser);
            return databaseConnectionEntity;
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), e);
        }
    }

    @Override
    public DatabaseConnectionDO updateDatabaseConnection(DbConnectionCreateDTO request) {
        try {
            DatabaseConnectionDO conn = request.toDatabaseConnection();
            conn.setEdition(test(request));
            connectionMapDAO.deleteConnection(request.getConnectionid());
            databaseConnectionDAO.updateTable(conn);
            DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(), request.getWebUser());
            String uuid = UUID.randomUUID().toString();
            dataList.setConnectionid(uuid);
            ConnectionDTO connectionDTO = new ConnectionDTO();
            DatabaseConnectionUrlDO databaseConnectionUrlDO = databaseConnectionDAO.getByName(request.getName(), request.getWebUser());
            connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
            connectionMapDAO.setConMap(uuid, connectionDTO);
            return dataList;
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), e);
        }
    }

    @Override
    public DatabaseConnectionDO loginDatabaseConnection(DbConnectionCreateDTO request) {
        try {
            DatabaseConnectionDO conn = request.toDatabaseConnection();
            conn.setEdition(test(request));
            databaseConnectionDAO.updateTable(conn);
            DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(), request.getWebUser());
            String uuid = UUID.randomUUID().toString();
            dataList.setConnectionid(uuid);
            ConnectionDTO connectionDTO = new ConnectionDTO();
            DatabaseConnectionUrlDO databaseConnectionUrlDO = databaseConnectionDAO.getByName(request.getName(), request.getWebUser());
            connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
            ConnectionMapDAO.setConMap(uuid, connectionDTO);
            return dataList;
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), e);
        }
    }

    @Override
    public List<DataListDTO> dataList(String uuid) {

        try {
            ConnectionDTO connectionDTO = conMap.get(uuid);
            List<DataListDTO> listDataList = new ArrayList<>();
            List<String> list = dataListByJdbcService.schemaListQuerySQL(
                    connectionDTO.getUrl(),
                    connectionDTO.getDbUser(),
                    connectionDTO.getDbPassword(),
                    GET_SCHEMA_NAME_SQL
            );
            for (int i = 0; i < list.size(); i++) {
                DataListDTO dataList = new DataListDTO();
                dataList = dataListByJdbcService.dataListQuerySQL(
                        connectionDTO.getUrl(),
                        connectionDTO.getDbUser(),
                        connectionDTO.getDbPassword(),
                        "SELECT tablename FROM pg_tables where schemaname ='" + list.get(i) + "';",
                        "select c.relname as viewname from pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '" + list.get(i) + SELECT_OBJECT_WHERE_IN_SQL + "v','m" + QUOTES_PARENTHESES_SEMICOLON,
                        "SELECT proname,proargtypes FROM pg_proc\n" +
                                "WHERE pronamespace = (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '" + list.get(i) + "')\n",

                        "select c.relname as relname from\n" +
                                "pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace \n" +
                                "and n.nspname = '" + list.get(i) + "' where c.relkind = 'S'",
                        "select synname from PG_SYNONYM where synobjschema ='" + list.get(i) + "'",
                        list.get(i)
                );
                listDataList.add(dataList);
            }
            connectionDTO.updataConnectionDTO(connectionDTO);
            ConnectionMapDAO.setConMap(uuid, connectionDTO);
            return listDataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(), e);
        }
    }


    @Override
    public List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema) {
        try {
            if (!conMap.containsKey(schema.getUuid())) {
                throw new CustomException(LocaleString.transLanguage("1004"));
            }
            ConnectionDTO connectionDTO = conMap.get(schema.getUuid());
            List<DataListDTO> listDataList = new ArrayList<>();
            DataListDTO dataList = dataListByJdbcService.dataListQuerySQL(
                    connectionDTO.getUrl(),
                    connectionDTO.getDbUser(),
                    connectionDTO.getDbPassword(),
                    "select relname as tablename\n" +
                            "  from (select tbl.oid as oid, tbl.relname relname\n" +
                            "          from pg_class tbl\n" +
                            "         inner join pg_namespace ns on tbl.relnamespace = ns.oid\n" +
                            "         where tbl.relkind = 'r'\n" +
                            "           and ns.nspname = '" + schema.getSchema() + "' ) x\n" +
                            " where has_table_privilege(x.oid, 'SELECT')" +
                            " order by 1",
                    "select c.relname as viewname from pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '" + schema.getSchema() + SELECT_OBJECT_WHERE_IN_SQL + "v','m" + "') order by 1" + SEMICOLON,
                    "SELECT proname,proargtypes FROM pg_proc\n" +
                            "WHERE pronamespace = (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '" + schema.getSchema() + "')\n" +
                            " order by 1\n",

                    "select c.relname as relname from\n" +
                            "pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace \n" +
                            "and n.nspname = '" + schema.getSchema() + "' where c.relkind = 'S'\n" +
                            " order by 1",
                    "select synname from PG_SYNONYM pgs, pg_namespace pgn\n" +
                            "   where pgn.oid = pgs.synnamespace\n" +
                            "     and pgn.nspname  ='" + schema.getSchema() + "'\n" +
                            " order by 1",
                    schema.getSchema()
            );
            connectionDTO.updataConnectionDTO(connectionDTO);
            ConnectionMapDAO.setConMap(schema.getUuid(), connectionDTO);
            listDataList.add(dataList);
            return listDataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(), e);
        }
    }

    @Override
    public String test(DbConnectionCreateDTO request) throws Exception {
        return metaDataByJdbcService.versionSQL(
                GET_URL_JDBC + request.getIp() + ":" + request.getPort() + "/" + request.getDataName() + CONFIGURE_TIME,
                request.getUserName(),
                request.getPassword(),
                " select version();"
        );
    }

}
