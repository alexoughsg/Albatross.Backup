// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.hypervisor.vmware.mo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cloud.hypervisor.vmware.util.VmwareContext;

import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.DatastoreInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostNasVolumeSpec;
import com.vmware.vim25.HostScsiDisk;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NasDatastoreInfo;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VmfsDatastoreCreateSpec;
import com.vmware.vim25.VmfsDatastoreOption;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.HostDatastoreSystem;
import com.vmware.vim25.mo.PropertyCollector;

public class HostDatastoreSystemMO extends BaseMO {

    protected HostDatastoreSystem _hostDatastoreSystem;
    
	public HostDatastoreSystemMO(VmwareContext context, ManagedObjectReference morHostDatastore) {
		super(context, morHostDatastore);
		_hostDatastoreSystem = new HostDatastoreSystem(context.getServerConnection(), morHostDatastore);
	}

	public HostDatastoreSystemMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
		_hostDatastoreSystem = new HostDatastoreSystem(context.getServerConnection(), this._mor);
	}

	public ManagedObjectReference findDatastore(String name) throws Exception {
		// added cloud.com specific name convention, we will use custom field "cloud.uuid" as datastore name as well
		CustomFieldsManagerMO cfmMo = new CustomFieldsManagerMO(_context,
			_context.getServiceContent().getCustomFieldsManager());
		int key = cfmMo.getCustomFieldKey("Datastore", CustomFieldConstants.CLOUD_UUID);
		assert(key != 0);

		ObjectContent[] ocs = getDatastorePropertiesOnHostDatastoreSystem(
			new String[] { "name", String.format("value[%d]", key) });
		if(ocs != null) {
			for(ObjectContent oc : ocs) {
				if(oc.getPropSet()[0].getVal().equals(name))
					return oc.getObj();

				if(oc.getPropSet().length > 1) {
					DynamicProperty prop = oc.getPropSet()[1];
					if(prop != null && prop.getVal() != null) {
						if(prop.getVal() instanceof CustomFieldStringValue) {
							String val = ((CustomFieldStringValue)prop.getVal()).getValue();
							if(val.equalsIgnoreCase(name))
								return oc.getObj();
						}
					}
				}
			}
		}
		return null;
	}

	// storeUrl in nfs://host/exportpath format
	public ManagedObjectReference findDatastoreByUrl(String storeUrl) throws Exception {
		assert(storeUrl != null);

		List<ManagedObjectReference> datastores = getDatastores();
		if(datastores != null && datastores.size() > 0) {
			for(ManagedObjectReference morDatastore : datastores) {
				NasDatastoreInfo info = getNasDatastoreInfo(morDatastore);
				if(info != null) {
					URI uri = new URI(storeUrl);
					String vmwareStyleUrl = "netfs://" + uri.getHost() + "/" + uri.getPath() + "/";
					if(info.getUrl().equals(vmwareStyleUrl))
						return morDatastore;
				}
			}
		}

		return null;
	}

    public ManagedObjectReference findDatastoreByName(String datastoreName) throws Exception {
        assert(datastoreName != null);

        List<ManagedObjectReference> datastores = getDatastores();

        if (datastores != null) {
            for (ManagedObjectReference morDatastore : datastores) {
                DatastoreInfo info = getDatastoreInfo(morDatastore);

                if (info != null) {
                    if (info.getName().equals(datastoreName))
                        return morDatastore;
                }
            }
        }

        return null;
    }

	// TODO this is a hacking helper method, when we can pass down storage pool info along with volume
	// we should be able to find the datastore by name
	public ManagedObjectReference findDatastoreByExportPath(String exportPath) throws Exception {
		assert(exportPath != null);

		List<ManagedObjectReference> datastores = getDatastores();
		if(datastores != null && datastores.size() > 0) {
			for(ManagedObjectReference morDatastore : datastores) {
				DatastoreMO dsMo = new DatastoreMO(_context, morDatastore);
				if(dsMo.getInventoryPath().equals(exportPath))
					return morDatastore;

				NasDatastoreInfo info = getNasDatastoreInfo(morDatastore);
				if(info != null) {
					String vmwareUrl = info.getUrl();
					if(vmwareUrl.charAt(vmwareUrl.length() - 1) == '/')
						vmwareUrl = vmwareUrl.substring(0, vmwareUrl.length() - 1);

					URI uri = new URI(vmwareUrl);
					if(uri.getPath().equals("/" + exportPath))
						return morDatastore;
				}
			}
		}

		return null;
	}

	public HostScsiDisk[] queryAvailableDisksForVmfs() throws Exception {
		return _hostDatastoreSystem.queryAvailableDisksForVmfs(null);
	}

	public Datastore createVmfsDatastore(String datastoreName, HostScsiDisk hostScsiDisk) throws Exception {
		// just grab the first instance of VmfsDatastoreOption
		VmfsDatastoreOption vmfsDatastoreOption = _hostDatastoreSystem.queryVmfsDatastoreCreateOptions(hostScsiDisk.getDevicePath(), 5)[0];

		VmfsDatastoreCreateSpec vmfsDatastoreCreateSpec = (VmfsDatastoreCreateSpec)vmfsDatastoreOption.getSpec();

		// set the name of the datastore to be created
		vmfsDatastoreCreateSpec.getVmfs().setVolumeName(datastoreName);

		return _hostDatastoreSystem.createVmfsDatastore(vmfsDatastoreCreateSpec);
	}

	public boolean deleteDatastore(String name) throws Exception {
		ManagedObjectReference morDatastore = findDatastore(name);
		Datastore datastore = new Datastore(_context.getServerConnection(), morDatastore);
		if(morDatastore != null) {
			_hostDatastoreSystem.removeDatastore(datastore);
			return true;
		}
		return false;
	}

	public Datastore createNfsDatastore(String host, int port,
		String exportPath, String uuid) throws Exception {

		HostNasVolumeSpec spec = new HostNasVolumeSpec();
		spec.setRemoteHost(host);
		spec.setRemotePath(exportPath);
		spec.setType("nfs");
		spec.setLocalPath(uuid);

		// readOnly/readWrite
		spec.setAccessMode("readWrite");
		return _hostDatastoreSystem.createNasDatastore(spec);
	}

	public List<ManagedObjectReference> getDatastores() throws Exception {
		return (List<ManagedObjectReference>)_context.getVimClient().getDynamicProperty(
			_mor, "datastore");
	}

	public DatastoreInfo getDatastoreInfo(ManagedObjectReference morDatastore) throws Exception {
		return (DatastoreInfo)_context.getVimClient().getDynamicProperty(morDatastore, "info");
	}

	public NasDatastoreInfo getNasDatastoreInfo(ManagedObjectReference morDatastore) throws Exception {
		DatastoreInfo info = (DatastoreInfo)_context.getVimClient().getDynamicProperty(morDatastore, "info");
		if(info instanceof NasDatastoreInfo)
			return (NasDatastoreInfo)info;
		return null;
	}

	public ObjectContent[] getDatastorePropertiesOnHostDatastoreSystem(String[] propertyPaths) throws Exception {

		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Datastore");
		pSpec.setPathSet(propertyPaths);

	    TraversalSpec hostDsSys2DatastoreTraversal = new TraversalSpec();
	    hostDsSys2DatastoreTraversal.setType("HostDatastoreSystem");
	    hostDsSys2DatastoreTraversal.setPath("datastore");
	    hostDsSys2DatastoreTraversal.setName("hostDsSys2DatastoreTraversal");

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(_mor);
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new TraversalSpec[] { hostDsSys2DatastoreTraversal});

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
        List<PropertyFilterSpec> pfSpecArr = new ArrayList<PropertyFilterSpec>();
        pfSpecArr.add(pfSpec);
        
        PropertyCollector pc = _context.getPropertyCollector();
	    return pc.retrieveProperties(pfSpecArr.toArray(new PropertyFilterSpec[0]));
	}
}
