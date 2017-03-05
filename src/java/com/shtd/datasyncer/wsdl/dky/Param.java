/**
 * Param.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.shtd.datasyncer.wsdl.dky;

public class Param  implements java.io.Serializable {
    private java.lang.String paramCmn;

    private java.lang.String paramName;

    private java.lang.String paramType;

    private java.lang.String[] paramValue;

    public Param() {
    }

    public Param(
           java.lang.String paramCmn,
           java.lang.String paramName,
           java.lang.String paramType,
           java.lang.String[] paramValue) {
           this.paramCmn = paramCmn;
           this.paramName = paramName;
           this.paramType = paramType;
           this.paramValue = paramValue;
    }


    /**
     * Gets the paramCmn value for this Param.
     * 
     * @return paramCmn
     */
    public java.lang.String getParamCmn() {
        return paramCmn;
    }


    /**
     * Sets the paramCmn value for this Param.
     * 
     * @param paramCmn
     */
    public void setParamCmn(java.lang.String paramCmn) {
        this.paramCmn = paramCmn;
    }


    /**
     * Gets the paramName value for this Param.
     * 
     * @return paramName
     */
    public java.lang.String getParamName() {
        return paramName;
    }


    /**
     * Sets the paramName value for this Param.
     * 
     * @param paramName
     */
    public void setParamName(java.lang.String paramName) {
        this.paramName = paramName;
    }


    /**
     * Gets the paramType value for this Param.
     * 
     * @return paramType
     */
    public java.lang.String getParamType() {
        return paramType;
    }


    /**
     * Sets the paramType value for this Param.
     * 
     * @param paramType
     */
    public void setParamType(java.lang.String paramType) {
        this.paramType = paramType;
    }


    /**
     * Gets the paramValue value for this Param.
     * 
     * @return paramValue
     */
    public java.lang.String[] getParamValue() {
        return paramValue;
    }


    /**
     * Sets the paramValue value for this Param.
     * 
     * @param paramValue
     */
    public void setParamValue(java.lang.String[] paramValue) {
        this.paramValue = paramValue;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Param)) return false;
        Param other = (Param) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.paramCmn==null && other.getParamCmn()==null) || 
             (this.paramCmn!=null &&
              this.paramCmn.equals(other.getParamCmn()))) &&
            ((this.paramName==null && other.getParamName()==null) || 
             (this.paramName!=null &&
              this.paramName.equals(other.getParamName()))) &&
            ((this.paramType==null && other.getParamType()==null) || 
             (this.paramType!=null &&
              this.paramType.equals(other.getParamType()))) &&
            ((this.paramValue==null && other.getParamValue()==null) || 
             (this.paramValue!=null &&
              java.util.Arrays.equals(this.paramValue, other.getParamValue())));
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
        if (getParamCmn() != null) {
            _hashCode += getParamCmn().hashCode();
        }
        if (getParamName() != null) {
            _hashCode += getParamName().hashCode();
        }
        if (getParamType() != null) {
            _hashCode += getParamType().hashCode();
        }
        if (getParamValue() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParamValue());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParamValue(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Param.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:DataService", "Param"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("paramCmn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "paramCmn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("paramName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "paramName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("paramType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "paramType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("paramValue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "paramValue"));
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
