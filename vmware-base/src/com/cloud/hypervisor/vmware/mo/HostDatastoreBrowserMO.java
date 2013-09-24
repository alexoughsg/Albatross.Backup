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

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;

import com.vmware.vim25.HostDatastoreBrowserSearchResults;
import com.vmware.vim25.HostDatastoreBrowserSearchSpec;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.HostDatastoreBrowser;
import com.vmware.vim25.mo.Task;

public class HostDatastoreBrowserMO extends BaseMO {

    private static final Logger s_logger = Logger.getLogger(HostDatastoreBrowserMO.class);
    
    protected HostDatastoreBrowser _hostDatastoreBrowser;

	public HostDatastoreBrowserMO(VmwareContext context, ManagedObjectReference morHostDatastoreBrowser) {
		super(context, morHostDatastoreBrowser);
		_hostDatastoreBrowser = new HostDatastoreBrowser(context.getServerConnection(), morHostDatastoreBrowser);
	}

	public HostDatastoreBrowserMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
		_hostDatastoreBrowser = new HostDatastoreBrowser(context.getServerConnection(), this._mor);
	}

	public void DeleteFile(String datastoreFullPath) throws Exception {
		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - deleteFile(). target mor: " + _mor.getVal() + ", file datastore path: " + datastoreFullPath);

		_hostDatastoreBrowser.deleteFile(datastoreFullPath);

		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - deleteFile() done");
	}

	public HostDatastoreBrowserSearchResults searchDatastore(String datastorePath, HostDatastoreBrowserSearchSpec searchSpec) throws Exception {
		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - searchDatastore(). target mor: " + _mor.getVal() + ", file datastore path: " + datastorePath);

		try {
		    Task task = _hostDatastoreBrowser.searchDatastore_Task(datastorePath, searchSpec);
			ManagedObjectReference morTask = task.getMOR();

			boolean result = _context.getVimClient().waitForTask(morTask);
			if(result) {
				_context.waitForTaskProgressDone(morTask);

				return (HostDatastoreBrowserSearchResults)_context.getVimClient().getDynamicProperty(morTask, "info.result");
			} else {
	        	s_logger.error("VMware searchDaastore_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
			}
		} finally {
			if(s_logger.isTraceEnabled())
				s_logger.trace("vCenter API trace - searchDatastore() done");
		}

		return null;
	}

	public HostDatastoreBrowserSearchResults searchDatastore(String datastorePath, String fileName, boolean caseInsensitive) throws Exception {
		HostDatastoreBrowserSearchSpec spec = new HostDatastoreBrowserSearchSpec();
		spec.setSearchCaseInsensitive(caseInsensitive);
		spec.setMatchPattern(new String[] {fileName});

		return searchDatastore(datastorePath, spec);
	}

    @SuppressWarnings("unchecked")
    public ArrayList<HostDatastoreBrowserSearchResults> searchDatastoreSubFolders(String datastorePath, HostDatastoreBrowserSearchSpec searchSpec) throws Exception {
		if(s_logger.isTraceEnabled())
			s_logger.trace("vCenter API trace - searchDatastoreSubFolders(). target mor: " + _mor.getVal() + ", file datastore path: " + datastorePath);

		try {
		    Task task = _hostDatastoreBrowser.searchDatastoreSubFolders_Task(datastorePath, searchSpec);
			ManagedObjectReference morTask = task.getMOR();

			boolean result = _context.getVimClient().waitForTask(morTask);
			if(result) {
				_context.waitForTaskProgressDone(morTask);

				return (ArrayList<HostDatastoreBrowserSearchResults>) _context.getVimClient().getDynamicProperty(morTask, "info.result");
			} else {
	        	s_logger.error("VMware searchDaastoreSubFolders_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
			}
		} finally {
			if(s_logger.isTraceEnabled())
				s_logger.trace("vCenter API trace - searchDatastore() done");
		}

		return null;
	}

    public ArrayList<HostDatastoreBrowserSearchResults> searchDatastoreSubFolders(String datastorePath, String fileName, boolean caseInsensitive) throws Exception {
        HostDatastoreBrowserSearchSpec spec = new HostDatastoreBrowserSearchSpec();
        spec.setSearchCaseInsensitive(caseInsensitive);
        spec.setMatchPattern(new String[] {fileName});

        return searchDatastoreSubFolders(datastorePath, spec);
    }
}
