/**
 * WebServiceInterfaceImplServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.shtd.datasyncer.wsdl.dky;

public class WebServiceInterfaceImplServiceLocator extends org.apache.axis.client.Service implements com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImplService {
	public WebServiceInterfaceImplServiceLocator() {
    }


    public WebServiceInterfaceImplServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WebServiceInterfaceImplServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for dataService
    private java.lang.String dataService_address = "http://info.dky.bjedu.cn/dkydp/services/dataService";

    public java.lang.String getdataServiceAddress() {
        return dataService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String dataServiceWSDDServiceName = "dataService";

    public java.lang.String getdataServiceWSDDServiceName() {
        return dataServiceWSDDServiceName;
    }

    public void setdataServiceWSDDServiceName(java.lang.String name) {
        dataServiceWSDDServiceName = name;
    }

    public com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImpl getdataService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(dataService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getdataService(endpoint);
    }

    public com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImpl getdataService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
        	com.shtd.datasyncer.wsdl.dky.DataServiceSoapBindingStub _stub = new com.shtd.datasyncer.wsdl.dky.DataServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getdataServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setdataServiceEndpointAddress(java.lang.String address) {
        dataService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImpl.class.isAssignableFrom(serviceEndpointInterface)) {
            	com.shtd.datasyncer.wsdl.dky.DataServiceSoapBindingStub _stub = new com.shtd.datasyncer.wsdl.dky.DataServiceSoapBindingStub(new java.net.URL(dataService_address), this);
                _stub.setPortName(getdataServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("dataService".equals(inputPortName)) {
            return getdataService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:DataService", "WebServiceInterfaceImplService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:DataService", "dataService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("dataService".equals(portName)) {
            setdataServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
