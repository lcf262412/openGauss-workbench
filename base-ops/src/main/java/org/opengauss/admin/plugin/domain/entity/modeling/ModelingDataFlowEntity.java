package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @TableName modeling_data_flow
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_data_flow")
public class ModelingDataFlowEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * data flow name
     */
    private String name;

    /**
     * query type (insert,delete,update,query)
     */
    private String type;

    /**
     * unique id
     */
    private String uId;

    /**
     * tag
     */
    private String tags;

    /**
     * remark
     */
    private String remark;

    /**
     * full operator config info
     */
    private String operatorContent;

    /**
     * default cluster id
     */
    private String clusterId;

    /**
     * default clusterNodeId
     */
    private String clusterNodeId;

    /**
     * default schema
     */
    private String schema;

    /**
     * default dbName
     */
    private String dbName;

    private int queryCount;

    @TableField(exist = false)
    private String orderBy;

    @TableField(exist = false)
    private int count;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
