/**
 * WebServiceInterfaceImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.shtd.datasyncer.wsdl.dky;

public interface WebServiceInterfaceImpl extends java.rmi.Remote {
    public java.lang.String[][] getData(java.lang.String usename, java.lang.String password, java.lang.String dataServerId, int start, int count) throws java.rmi.RemoteException;
    public java.lang.String[][] getData(java.lang.String username, java.lang.String password, java.lang.String dataServerId, com.shtd.datasyncer.wsdl.dky.Param[] params, int start, int count) throws java.rmi.RemoteException;
    public int updateData(java.lang.String username, java.lang.String password, java.lang.String dataServerId, com.shtd.datasyncer.wsdl.dky.Param[] params) throws java.rmi.RemoteException;
    public com.shtd.datasyncer.wsdl.dky.MetadataColumn[] getDataServerColumns(java.lang.String username, java.lang.String password, java.lang.String dataServerId) throws java.rmi.RemoteException;
    public java.lang.String[] getDataArray(java.lang.String usename, java.lang.String password, java.lang.String dataServerId, int start, int count) throws java.rmi.RemoteException;
    public com.shtd.datasyncer.wsdl.dky.Column[] getTableColumnInfo(java.lang.String serviceid) throws java.rmi.RemoteException;
    public com.shtd.datasyncer.wsdl.dky.Column[] getTableColumnInfo(java.lang.String username, java.lang.String password, java.lang.String serviceid) throws java.rmi.RemoteException;
    public int getTableRecordCount(java.lang.String serviceid) throws java.rmi.RemoteException;
    public int getTableRecordCount(java.lang.String username, java.lang.String password, java.lang.String serviceid) throws java.rmi.RemoteException;
    public java.lang.String[][] getTableData(java.lang.String serviceid, int startLineNum, int pageSize) throws java.rmi.RemoteException;
    public java.lang.String[][] getTableData(java.lang.String username, java.lang.String password, java.lang.String serviceid, int startLineNum, int pageSize) throws java.rmi.RemoteException;
    public int getDataCount(java.lang.String username, java.lang.String password, java.lang.String dataServerId, com.shtd.datasyncer.wsdl.dky.Param[] params) throws java.rmi.RemoteException;
    public com.shtd.datasyncer.wsdl.dky.Param[] getDataServerParam(java.lang.String username, java.lang.String password, java.lang.String dataServerId) throws java.rmi.RemoteException;
}