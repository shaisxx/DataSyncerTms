/**
 * MetadataColumn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.shtd.datasyncer.wsdl.dky;

public class MetadataColumn  implements java.io.Serializable {
    private java.lang.String col_comments;

    private java.lang.String column_name;

    private java.lang.String comments;

    private java.lang.String data_default;

    private java.lang.String data_length;

    private java.math.BigDecimal data_precision;

    private java.math.BigDecimal data_scale;

    private java.lang.String data_type;

    private java.lang.String nullable;

    private boolean required;

    private java.lang.Integer sn;

    private java.lang.String tab_comments;

    private java.lang.String tableColumnName;

    private java.lang.String table_name;

    public MetadataColumn() {
    }

    public MetadataColumn(
           java.lang.String col_comments,
           java.lang.String column_name,
           java.lang.String comments,
           java.lang.String data_default,
           java.lang.String data_length,
           java.math.BigDecimal data_precision,
           java.math.BigDecimal data_scale,
           java.lang.String data_type,
           java.lang.String nullable,
           boolean required,
           java.lang.Integer sn,
           java.lang.String tab_comments,
           java.lang.String tableColumnName,
           java.lang.String table_name) {
           this.col_comments = col_comments;
           this.column_name = column_name;
           this.comments = comments;
           this.data_default = data_default;
           this.data_length = data_length;
           this.data_precision = data_precision;
           this.data_scale = data_scale;
           this.data_type = data_type;
           this.nullable = nullable;
           this.required = required;
           this.sn = sn;
           this.tab_comments = tab_comments;
           this.tableColumnName = tableColumnName;
           this.table_name = table_name;
    }


    /**
     * Gets the col_comments value for this MetadataColumn.
     * 
     * @return col_comments
     */
    public java.lang.String getCol_comments() {
        return col_comments;
    }


    /**
     * Sets the col_comments value for this MetadataColumn.
     * 
     * @param col_comments
     */
    public void setCol_comments(java.lang.String col_comments) {
        this.col_comments = col_comments;
    }


    /**
     * Gets the column_name value for this MetadataColumn.
     * 
     * @return column_name
     */
    public java.lang.String getColumn_name() {
        return column_name;
    }


    /**
     * Sets the column_name value for this MetadataColumn.
     * 
     * @param column_name
     */
    public void setColumn_name(java.lang.String column_name) {
        this.column_name = column_name;
    }


    /**
     * Gets the comments value for this MetadataColumn.
     * 
     * @return comments
     */
    public java.lang.String getComments() {
        return comments;
    }


    /**
     * Sets the comments value for this MetadataColumn.
     * 
     * @param comments
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }


    /**
     * Gets the data_default value for this MetadataColumn.
     * 
     * @return data_default
     */
    public java.lang.String getData_default() {
        return data_default;
    }


    /**
     * Sets the data_default value for this MetadataColumn.
     * 
     * @param data_default
     */
    public void setData_default(java.lang.String data_default) {
        this.data_default = data_default;
    }


    /**
     * Gets the data_length value for this MetadataColumn.
     * 
     * @return data_length
     */
    public java.lang.String getData_length() {
        return data_length;
    }


    /**
     * Sets the data_length value for this MetadataColumn.
     * 
     * @param data_length
     */
    public void setData_length(java.lang.String data_length) {
        this.data_length = data_length;
    }


    /**
     * Gets the data_precision value for this MetadataColumn.
     * 
     * @return data_precision
     */
    public java.math.BigDecimal getData_precision() {
        return data_precision;
    }


    /**
     * Sets the data_precision value for this MetadataColumn.
     * 
     * @param data_precision
     */
    public void setData_precision(java.math.BigDecimal data_precision) {
        this.data_precision = data_precision;
    }


    /**
     * Gets the data_scale value for this MetadataColumn.
     * 
     * @return data_scale
     */
    public java.math.BigDecimal getData_scale() {
        return data_scale;
    }


    /**
     * Sets the data_scale value for this MetadataColumn.
     * 
     * @param data_scale
     */
    public void setData_scale(java.math.BigDecimal data_scale) {
        this.data_scale = data_scale;
    }


    /**
     * Gets the data_type value for this MetadataColumn.
     * 
     * @return data_type
     */
    public java.lang.String getData_type() {
        return data_type;
    }


    /**
     * Sets the data_type value for this MetadataColumn.
     * 
     * @param data_type
     */
    public void setData_type(java.lang.String data_type) {
        this.data_type = data_type;
    }


    /**
     * Gets the nullable value for this MetadataColumn.
     * 
     * @return nullable
     */
    public java.lang.String getNullable() {
        return nullable;
    }


    /**
     * Sets the nullable value for this MetadataColumn.
     * 
     * @param nullable
     */
    public void setNullable(java.lang.String nullable) {
        this.nullable = nullable;
    }


    /**
     * Gets the required value for this MetadataColumn.
     * 
     * @return required
     */
    public boolean isRequired() {
        return required;
    }


    /**
     * Sets the required value for this MetadataColumn.
     * 
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }


    /**
     * Gets the sn value for this MetadataColumn.
     * 
     * @return sn
     */
    public java.lang.Integer getSn() {
        return sn;
    }


    /**
     * Sets the sn value for this MetadataColumn.
     * 
     * @param sn
     */
    public void setSn(java.lang.Integer sn) {
        this.sn = sn;
    }


    /**
     * Gets the tab_comments value for this MetadataColumn.
     * 
     * @return tab_comments
     */
    public java.lang.String getTab_comments() {
        return tab_comments;
    }


    /**
     * Sets the tab_comments value for this MetadataColumn.
     * 
     * @param tab_comments
     */
    public void setTab_comments(java.lang.String tab_comments) {
        this.tab_comments = tab_comments;
    }


    /**
     * Gets the tableColumnName value for this MetadataColumn.
     * 
     * @return tableColumnName
     */
    public java.lang.String getTableColumnName() {
        return tableColumnName;
    }


    /**
     * Sets the tableColumnName value for this MetadataColumn.
     * 
     * @param tableColumnName
     */
    public void setTableColumnName(java.lang.String tableColumnName) {
        this.tableColumnName = tableColumnName;
    }


    /**
     * Gets the table_name value for this MetadataColumn.
     * 
     * @return table_name
     */
    public java.lang.String getTable_name() {
        return table_name;
    }


    /**
     * Sets the table_name value for this MetadataColumn.
     * 
     * @param table_name
     */
    public void setTable_name(java.lang.String table_name) {
        this.table_name = table_name;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MetadataColumn)) return false;
        MetadataColumn other = (MetadataColumn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.col_comments==null && other.getCol_comments()==null) || 
             (this.col_comments!=null &&
              this.col_comments.equals(other.getCol_comments()))) &&
            ((this.column_name==null && other.getColumn_name()==null) || 
             (this.column_name!=null &&
              this.column_name.equals(other.getColumn_name()))) &&
            ((this.comments==null && other.getComments()==null) || 
             (this.comments!=null &&
              this.comments.equals(other.getComments()))) &&
            ((this.data_default==null && other.getData_default()==null) || 
             (this.data_default!=null &&
              this.data_default.equals(other.getData_default()))) &&
            ((this.data_length==null && other.getData_length()==null) || 
             (this.data_length!=null &&
              this.data_length.equals(other.getData_length()))) &&
            ((this.data_precision==null && other.getData_precision()==null) || 
             (this.data_precision!=null &&
              this.data_precision.equals(other.getData_precision()))) &&
            ((this.data_scale==null && other.getData_scale()==null) || 
             (this.data_scale!=null &&
              this.data_scale.equals(other.getData_scale()))) &&
            ((this.data_type==null && other.getData_type()==null) || 
             (this.data_type!=null &&
              this.data_type.equals(other.getData_type()))) &&
            ((this.nullable==null && other.getNullable()==null) || 
             (this.nullable!=null &&
              this.nullable.equals(other.getNullable()))) &&
            this.required == other.isRequired() &&
            ((this.sn==null && other.getSn()==null) || 
             (this.sn!=null &&
              this.sn.equals(other.getSn()))) &&
            ((this.tab_comments==null && other.getTab_comments()==null) || 
             (this.tab_comments!=null &&
              this.tab_comments.equals(other.getTab_comments()))) &&
            ((this.tableColumnName==null && other.getTableColumnName()==null) || 
             (this.tableColumnName!=null &&
              this.tableColumnName.equals(other.getTableColumnName()))) &&
            ((this.table_name==null && other.getTable_name()==null) || 
             (this.table_name!=null &&
              this.table_name.equals(other.getTable_name())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCol_comments() != null) {
            _hashCode += getCol_comments().hashCode();
        }
        if (getColumn_name() != null) {
            _hashCode += getColumn_name().hashCode();
        }
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        if (getData_default() != null) {
            _hashCode += getData_default().hashCode();
        }
        if (getData_length() != null) {
            _hashCode += getData_length().hashCode();
        }
        if (getData_precision() != null) {
            _hashCode += getData_precision().hashCode();
        }
        if (getData_scale() != null) {
            _hashCode += getData_scale().hashCode();
        }
        if (getData_type() != null) {
            _hashCode += getData_type().hashCode();
        }
        if (getNullable() != null) {
            _hashCode += getNullable().hashCode();
        }
        _hashCode += (isRequired() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getSn() != null) {
            _hashCode += getSn().hashCode();
        }
        if (getTab_comments() != null) {
            _hashCode += getTab_comments().hashCode();
        }
        if (getTableColumnName() != null) {
            _hashCode += getTableColumnName().hashCode();
        }
        if (getTable_name() != null) {
            _hashCode += getTable_name().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MetadataColumn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:DataService", "MetadataColumn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("col_comments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "col_comments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("column_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "column_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("comments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "comments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("data_default");
        elemField.setXmlName(new javax.xml.namespace.QName("", "data_default"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("data_length");
        elemField.setXmlName(new javax.xml.namespace.QName("", "data_length"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("data_precision");
        elemField.setXmlName(new javax.xml.namespace.QName("", "data_precision"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("data_scale");
        elemField.setXmlName(new javax.xml.namespace.QName("", "data_scale"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("data_type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "data_type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nullable");
        elemField.setXmlName(new javax.xml.namespace.QName("", "nullable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("required");
        elemField.setXmlName(new javax.xml.namespace.QName("", "required"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tab_comments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "tab_comments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tableColumnName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "tableColumnName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("table_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "table_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
