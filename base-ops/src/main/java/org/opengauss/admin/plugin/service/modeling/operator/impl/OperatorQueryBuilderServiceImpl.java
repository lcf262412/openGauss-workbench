package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@Component("query")
public class OperatorQueryBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params , ModelingDataFlowSqlObject sqlObject) {

        String oriSql = sqlObject.getSelectRegion();

        JSONArray fields = params.getJSONArray("fields");
        List<String> sqlFields = new ArrayList<>();

        if (fields.isEmpty()) {
            oriSql = "select * ";
        } else {
            for (int i = 0; i < fields.size(); i++) {
                sqlFields.add(fields.getJSONObject(i).getString("value"));
            }
            oriSql += String.join(",",sqlFields) + " ";
        }

        sqlObject.setSelectRegion(oriSql);

        oriSql = sqlObject.getFromRegion();

        String table = params.getString("table");
        oriSql = "from "+table+" ";

        sqlObject.setFromRegion(oriSql);

        return sqlObject;
    }

}




