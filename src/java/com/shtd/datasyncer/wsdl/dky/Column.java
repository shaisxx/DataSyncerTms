/**
 * Column.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.shtd.datasyncer.wsdl.dky;

public class Column  implements java.io.Serializable {
    private java.lang.String colComments;

    private int colLength;

    private java.lang.String colName;

    private java.lang.String colType;

    public Column() {
    }

    public Column(
           java.lang.String colComments,
           int colLength,
           java.lang.String colName,
           java.lang.String colType) {
           this.colComments = colComments;
           this.colLength = colLength;
           this.colName = colName;
           this.colType = colType;
    }


    /**
     * Gets the colComments value for this Column.
     * 
     * @return colComments
     */
    public java.lang.String getColComments() {
        return colComments;
    }


    /**
     * Sets the colComments value for this Column.
     * 
     * @param colComments
     */
    public void setColComments(java.lang.String colComments) {
        this.colComments = colComments;
    }


    /**
     * Gets the colLength value for this Column.
     * 
     * @return colLength
     */
    public int getColLength() {
        return colLength;
    }


    /**
     * Sets the colLength value for this Column.
     * 
     * @param colLength
     */
    public void setColLength(int colLength) {
        this.colLength = colLength;
    }


    /**
     * Gets the colName value for this Column.
     * 
     * @return colName
     */
    public java.lang.String getColName() {
        return colName;
    }


    /**
     * Sets the colName value for this Column.
     * 
     * @param colName
     */
    public void setColName(java.lang.String colName) {
        this.colName = colName;
    }


    /**
     * Gets the colType value for this Column.
     * 
     * @return colType
     */
    public java.lang.String getColType() {
        return colType;
    }


    /**
     * Sets the colType value for this Column.
     * 
     * @param colType
     */
    public void setColType(java.lang.String colType) {
        this.colType = colType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Column)) return false;
        Column other = (Column) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.colComments==null && other.getColComments()==null) || 
             (this.colComments!=null &&
              this.colComments.equals(other.getColComments()))) &&
            this.colLength == other.getColLength() &&
            ((this.colName==null && other.getColName()==null) || 
             (this.colName!=null &&
              this.colName.equals(other.getColName()))) &&
            ((this.colType==null && other.getColType()==null) || 
             (this.colType!=null &&
              this.colType.equals(other.getColType())));
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
        if (getColComments() != null) {
            _hashCode += getColComments().hashCode();
        }
        _hashCode += getColLength();
        if (getColName() != null) {
            _hashCode += getColName().hashCode();
        }
        if (getColType() != null) {
            _hashCode += getColType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Column.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:DataService", "Column"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("colComments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "colComments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("colLength");
        elemField.setXmlName(new javax.xml.namespace.QName("", "colLength"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("colName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "colName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("colType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "colType"));
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
