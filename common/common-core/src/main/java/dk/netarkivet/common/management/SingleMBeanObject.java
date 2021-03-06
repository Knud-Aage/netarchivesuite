/*
 * #%L
 * Netarchivesuite - common
 * %%
 * Copyright (C) 2005 - 2017 The Royal Danish Library, 
 *             the National Library of France and the Austrian National Library.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

package dk.netarkivet.common.management;

import java.util.Hashtable;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.distribute.arcrepository.Replica;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.IllegalState;
import dk.netarkivet.common.exceptions.UnknownID;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.common.utils.SystemUtils;

/**
 * Wrapper class for registering objects of type I as MBeans.
 * <p>
 * The register method will register a given object under an object name, generated with the domain given in
 * constructor, and fields from the Hashtable nameProperties. It is prefilled with values common for all MBeans, but it
 * is expected to be extended after the object is created with additional info.
 *
 * @param <I> The Type of Object to expose as an MBean. Currently only used with {@link SingleMBeanObject}
 */
public class SingleMBeanObject<I> {
    /** Properties for the ObjectName name. */
    private Hashtable<String, String> nameProperties = new Hashtable<String, String>();
    /** The domain for this SingleMBeanObject. */
    private String domain;
    /** The object to expose as an MBean. */
    private I exposedObject;
    /** The interface, this SingleMBeanObject should expose on the given MBeanServer. */
    private Class<I> asInterface;
    /** The ObjectName this SingleMBeanObject registers on the given MBeanServer. */
    private ObjectName name;
    /** MBeanServer to register mbeans in. */
    private final MBeanServer mBeanServer;

    // FIXME Following environment constant are defined here in order to avoid
    // refering to independent modules - the environment values are only
    // used if defined
    // Note that HARVESTER_HARVEST_CONTROLLER_CHANNEL should be identical to
    // HarvesterSettings.HARVESTER_HARVEST_CONTROLLER_PRIORITY
    private static String HARVESTER_HARVEST_CONTROLLER_CHANNEL = "settings.harvester.harvesting.channel";

    /**
     * Create a single mbean object. This will fill out nameProperties with default values, and remember the domain and
     * interface given. It will not, however, register the MBean.
     *
     * @param domain The domain of the MBean.
     * @param object The object to expose as an MBean.
     * @param asInterface The interface this MBean is exposed as.
     * @param mBeanServer The server to register the mbean in.
     * @throws ArgumentNotValid If domain is null or empty, or any other argument is null.
     */
    public SingleMBeanObject(String domain, I object, Class<I> asInterface, MBeanServer mBeanServer) {
        ArgumentNotValid.checkNotNullOrEmpty(domain, "String domain");
        ArgumentNotValid.checkNotNull(object, "I object");
        ArgumentNotValid.checkNotNull(asInterface, "Class asInterface");
        ArgumentNotValid.checkNotNull(mBeanServer, "MBeanServer mbeanServer");
        this.domain = domain;
        this.asInterface = asInterface;
        this.exposedObject = object;

        nameProperties.put(Constants.PRIORITY_KEY_LOCATION, Settings.get(CommonSettings.THIS_PHYSICAL_LOCATION));
        nameProperties.put(Constants.PRIORITY_KEY_MACHINE, SystemUtils.getLocalHostName());
        nameProperties.put(Constants.PRIORITY_KEY_APPLICATIONNAME, Settings.get(CommonSettings.APPLICATION_NAME));
        nameProperties.put(Constants.PRIORITY_KEY_APPLICATIONINSTANCEID,
                Settings.get(CommonSettings.APPLICATION_INSTANCE_ID));
        nameProperties.put(Constants.PRIORITY_KEY_HTTP_PORT, Settings.get(CommonSettings.HTTP_PORT_NUMBER));
        try {
            String val;
            val = Settings.get(HARVESTER_HARVEST_CONTROLLER_CHANNEL);
            nameProperties.put(Constants.PRIORITY_KEY_CHANNEL, val);
        } catch (UnknownID e) {
            nameProperties.put(Constants.PRIORITY_KEY_CHANNEL, "");
        }
        try {
            String val = Replica.getReplicaFromId(Settings.get(CommonSettings.USE_REPLICA_ID)).getName();
            nameProperties.put(Constants.PRIORITY_KEY_REPLICANAME, val);
        } catch (UnknownID e) {
            nameProperties.put(Constants.PRIORITY_KEY_REPLICANAME, "");
        }

        this.mBeanServer = mBeanServer;
    }

    /**
     * Create a single mbean object.
     * <p>
     * This is a helper method for the constructor taking a domain, which take the domain from a preconstructed
     * ObjectName and replaces the nameProperties with the properties from the given object name. Use this if you have
     * an object name created already, which you wish to use.
     *
     * @param name The object name to register under.
     * @param o The object to register.
     * @param asInterface The interface o should implement.
     * @param mBeanServer The mbean server to register o in.
     * @throws ArgumentNotValid on any null parameter.
     */
    public SingleMBeanObject(ObjectName name, I o, Class<I> asInterface, MBeanServer mBeanServer) {
        this(name.getDomain(), o, asInterface, mBeanServer);
        nameProperties.clear();
        nameProperties.putAll(name.getKeyPropertyList());
    }

    /**
     * Properties for the ObjectName name. Update these before registering. On construction, initialised with location,
     * hostname, httpport, priority, replica, applicationname, applicationinstid.
     *
     * @return Hashtable of the object name properties.
     */
    public Hashtable<String, String> getNameProperties() {
        return nameProperties;
    }

    /**
     * Registers this object as a standard MBean, with a name generated from domain given in constructor and the
     * nameProperties hashTable.
     *
     * @throws IllegalState if bean is already registered.
     * @throws IOFailure on trouble registering.
     */
    public void register() {
        try {
            name = new ObjectName(domain, nameProperties);
            mBeanServer.registerMBean(new StandardMBean(exposedObject, asInterface), name);
        } catch (InstanceAlreadyExistsException e) {
            throw new IllegalState("this MBean '" + name + "' is already registered on the MBeanServer", e);
        } catch (JMException e) {
            throw new IOFailure("Unable to register MBean '" + name + "'", e);
        }
    }

    /**
     * Unregister the object from the MBeanServer. Note: It is not checked that it is actually this mbean that is
     * registered under the name, this method simply unregisters any object with this object name.
     *
     * @throws IOFailure on trouble unregistering.
     */
    public void unregister() throws IOFailure {
        MBeanServer mbserver = mBeanServer;
        try {
            if (name != null) {
                name = new ObjectName(domain, nameProperties);
            }
            if (mbserver.isRegistered(name)) {
                mbserver.unregisterMBean(name);
            }
        } catch (JMException e) {
            throw new IOFailure("Unable to unregister MBean '" + name + "'", e);
        }
    }

    /** @return the name */
    public ObjectName getName() {
        return name;
    }

}
