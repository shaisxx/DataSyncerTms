/**
 * DataServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.shtd.datasyncer.wsdl.dky;

public class DataServiceSoapBindingSkeleton implements com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImpl, org.apache.axis.wsdl.Skeleton {
    private com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImpl impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "usename"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataServerId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "start"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "count"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getData", _params, new javax.xml.namespace.QName("", "getDataReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "DoubleArray"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getData"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getData") == null) {
            _myOperations.put("getData", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getData")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataServerId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "params"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:DataService", "ParamArray"), com.shtd.datasyncer.wsdl.dky.Param[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "start"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "count"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getData", _params, new javax.xml.namespace.QName("", "getDataReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "DoubleArray"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getData"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getData") == null) {
            _myOperations.put("getData", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getData")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataServerId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "params"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:DataService", "ParamArray"), com.shtd.datasyncer.wsdl.dky.Param[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("updateData", _params, new javax.xml.namespace.QName("", "updateDataReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "updateData"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("updateData") == null) {
            _myOperations.put("updateData", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("updateData")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataServerId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataServerColumns", _params, new javax.xml.namespace.QName("", "getDataServerColumnsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "ArrayOfMetadataColumn"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getDataServerColumns"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataServerColumns") == null) {
            _myOperations.put("getDataServerColumns", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataServerColumns")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "usename"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataServerId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "start"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "count"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataArray", _params, new javax.xml.namespace.QName("", "getDataArrayReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "ArraySQL"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getDataArray"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataArray") == null) {
            _myOperations.put("getDataArray", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataArray")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "serviceid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTableColumnInfo", _params, new javax.xml.namespace.QName("", "getTableColumnInfoReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "ArrayColumn"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getTableColumnInfo"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTableColumnInfo") == null) {
            _myOperations.put("getTableColumnInfo", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTableColumnInfo")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "serviceid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTableColumnInfo", _params, new javax.xml.namespace.QName("", "getTableColumnInfoReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "ArrayColumn"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getTableColumnInfo"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTableColumnInfo") == null) {
            _myOperations.put("getTableColumnInfo", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTableColumnInfo")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "serviceid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTableRecordCount", _params, new javax.xml.namespace.QName("", "getTableRecordCountReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getTableRecordCount"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTableRecordCount") == null) {
            _myOperations.put("getTableRecordCount", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTableRecordCount")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "serviceid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTableRecordCount", _params, new javax.xml.namespace.QName("", "getTableRecordCountReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getTableRecordCount"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTableRecordCount") == null) {
            _myOperations.put("getTableRecordCount", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTableRecordCount")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "serviceid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "startLineNum"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pageSize"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTableData", _params, new javax.xml.namespace.QName("", "getTableDataReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "DoubleArray"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getTableData"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTableData") == null) {
            _myOperations.put("getTableData", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTableData")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "serviceid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "startLineNum"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pageSize"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTableData", _params, new javax.xml.namespace.QName("", "getTableDataReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "DoubleArray"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getTableData"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTableData") == null) {
            _myOperations.put("getTableData", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTableData")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataServerId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "params"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:DataService", "ParamArray"), com.shtd.datasyncer.wsdl.dky.Param[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataCount", _params, new javax.xml.namespace.QName("", "getDataCountReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getDataCount"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataCount") == null) {
            _myOperations.put("getDataCount", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataCount")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataServerId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataServerParam", _params, new javax.xml.namespace.QName("", "getDataServerParamReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:DataService", "ParamArray"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:DataService", "getDataServerParam"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataServerParam") == null) {
            _myOperations.put("getDataServerParam", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataServerParam")).add(_oper);
    }

    public DataServiceSoapBindingSkeleton() {
        this.impl = new com.shtd.datasyncer.wsdl.dky.DataServiceSoapBindingImpl();
    }

    public DataServiceSoapBindingSkeleton(com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImpl impl) {
        this.impl = impl;
    }
    public java.lang.String[][] getData(java.lang.String usename, java.lang.String password, java.lang.String dataServerId, int start, int count) throws java.rmi.RemoteException
    {
        java.lang.String[][] ret = impl.getData(usename, password, dataServerId, start, count);
        return ret;
    }

    public java.lang.String[][] getData(java.lang.String username, java.lang.String password, java.lang.String dataServerId, com.shtd.datasyncer.wsdl.dky.Param[] params, int start, int count) throws java.rmi.RemoteException
    {
        java.lang.String[][] ret = impl.getData(username, password, dataServerId, params, start, count);
        return ret;
    }

    public int updateData(java.lang.String username, java.lang.String password, java.lang.String dataServerId, com.shtd.datasyncer.wsdl.dky.Param[] params) throws java.rmi.RemoteException
    {
        int ret = impl.updateData(username, password, dataServerId, params);
        return ret;
    }

    public com.shtd.datasyncer.wsdl.dky.MetadataColumn[] getDataServerColumns(java.lang.String username, java.lang.String password, java.lang.String dataServerId) throws java.rmi.RemoteException
    {
        com.shtd.datasyncer.wsdl.dky.MetadataColumn[] ret = impl.getDataServerColumns(username, password, dataServerId);
        return ret;
    }

    public java.lang.String[] getDataArray(java.lang.String usename, java.lang.String password, java.lang.String dataServerId, int start, int count) throws java.rmi.RemoteException
    {
        java.lang.String[] ret = impl.getDataArray(usename, password, dataServerId, start, count);
        return ret;
    }

    public com.shtd.datasyncer.wsdl.dky.Column[] getTableColumnInfo(java.lang.String serviceid) throws java.rmi.RemoteException
    {
        com.shtd.datasyncer.wsdl.dky.Column[] ret = impl.getTableColumnInfo(serviceid);
        return ret;
    }

    public com.shtd.datasyncer.wsdl.dky.Column[] getTableColumnInfo(java.lang.String username, java.lang.String password, java.lang.String serviceid) throws java.rmi.RemoteException
    {
        com.shtd.datasyncer.wsdl.dky.Column[] ret = impl.getTableColumnInfo(username, password, serviceid);
        return ret;
    }

    public int getTableRecordCount(java.lang.String serviceid) throws java.rmi.RemoteException
    {
        int ret = impl.getTableRecordCount(serviceid);
        return ret;
    }

    public int getTableRecordCount(java.lang.String username, java.lang.String password, java.lang.String serviceid) throws java.rmi.RemoteException
    {
        int ret = impl.getTableRecordCount(username, password, serviceid);
        return ret;
    }

    public java.lang.String[][] getTableData(java.lang.String serviceid, int startLineNum, int pageSize) throws java.rmi.RemoteException
    {
        java.lang.String[][] ret = impl.getTableData(serviceid, startLineNum, pageSize);
        return ret;
    }

    public java.lang.String[][] getTableData(java.lang.String username, java.lang.String password, java.lang.String serviceid, int startLineNum, int pageSize) throws java.rmi.RemoteException
    {
        java.lang.String[][] ret = impl.getTableData(username, password, serviceid, startLineNum, pageSize);
        return ret;
    }

    public int getDataCount(java.lang.String username, java.lang.String password, java.lang.String dataServerId, com.shtd.datasyncer.wsdl.dky.Param[] params) throws java.rmi.RemoteException
    {
        int ret = impl.getDataCount(username, password, dataServerId, params);
        return ret;
    }

    public com.shtd.datasyncer.wsdl.dky.Param[] getDataServerParam(java.lang.String username, java.lang.String password, java.lang.String dataServerId) throws java.rmi.RemoteException
    {
        com.shtd.datasyncer.wsdl.dky.Param[] ret = impl.getDataServerParam(username, password, dataServerId);
        return ret;
    }

}
