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
import java.util.List;

import org.apache.log4j.Logger;

import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.FileInfo;
import com.vmware.vim25.HostDatastoreBrowserSearchResults;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.FileManager;
import com.vmware.vim25.mo.PropertyCollector;
import com.vmware.vim25.mo.Task;

import com.cloud.hypervisor.vmware.util.VmwareContext;
import com.cloud.utils.Pair;

public class DatastoreMO extends BaseMO {
	private static final Logger s_logger = Logger.getLogger(DatastoreMO.class);

	private String _name;
	private Pair<DatacenterMO, String> _ownerDc;
	protected Datastore _datastore;

	public DatastoreMO(VmwareContext context, ManagedObjectReference morDatastore) {
		super(context, morDatastore);
		_datastore = (Datastore) this.getManagedEntity();
	}

	public DatastoreMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
		_datastore = (Datastore) this.getManagedEntity();
	}

	@Override
    public String getName() throws Exception {
		if(_name == null)
			_name = (String)_context.getVimClient().getDynamicProperty(_mor, "name");

		return _name;
	}

	public DatastoreSummary getSummary() throws Exception {
		return (DatastoreSummary)_context.getVimClient().getDynamicProperty(_mor, "summary");
	}

	public HostDatastoreBrowserMO getHostDatastoreBrowserMO() throws Exception {
		return new HostDatastoreBrowserMO(_context,
				(ManagedObjectReference)_context.getVimClient().getDynamicProperty(_mor, "browser"));
	}

	public String getInventoryPath() throws Exception {
		Pair<DatacenterMO, String> dcInfo = getOwnerDatacenter();
		return dcInfo.second() + "/" + getName();
	}

	public Pair<DatacenterMO, String> getOwnerDatacenter() throws Exception {
		if(_ownerDc != null)
			return _ownerDc;

		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("Datacenter");
		pSpec.setPathSet(new String[] { "name" });

	    TraversalSpec folderParentTraversal = new TraversalSpec();
	    folderParentTraversal.setType("Folder");
	    folderParentTraversal.setPath("parent");
	    folderParentTraversal.setName("folderParentTraversal");
	    SelectionSpec sSpec = new SelectionSpec();
	    sSpec.setName("folderParentTraversal");
	    folderParentTraversal.setSelectSet( new SelectionSpec[] { sSpec });

	    TraversalSpec dsParentTraversal = new TraversalSpec();
	    dsParentTraversal.setType("Datastore");
	    dsParentTraversal.setPath("parent");
	    dsParentTraversal.setName("dsParentTraversal");
	    dsParentTraversal.setSelectSet(new TraversalSpec[] { folderParentTraversal });

	    ObjectSpec oSpec = new ObjectSpec();
	    oSpec.setObj(getMor());
	    oSpec.setSkip(Boolean.TRUE);
	    oSpec.setSelectSet(new TraversalSpec[] { dsParentTraversal });

	    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
	    pfSpec.setPropSet(new PropertySpec[] { pSpec });
	    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
        List<PropertyFilterSpec> pfSpecArr = new ArrayList<PropertyFilterSpec>();
        pfSpecArr.add(pfSpec);

        PropertyCollector pc = _context.getPropertyCollector();
	    ObjectContent[] ocs = pc.retrieveProperties(pfSpecArr.toArray(new PropertyFilterSpec[0]));

	    assert(ocs != null && ocs.length > 0);
	    assert(ocs[0].getObj() != null);
	    assert(ocs[0].getPropSet() != null);
	    String dcName = ocs[0].getPropSet()[0].getVal().toString();
	    _ownerDc = new Pair<DatacenterMO, String>(new DatacenterMO(_context, ocs[0].getObj()), dcName);
	    return _ownerDc;
	}

	public void makeDirectory(String path, ManagedObjectReference morDc) throws Exception {
		String datastoreName = getName();
		Datacenter datacenter = new Datacenter(_context.getServerConnection(), morDc);
		FileManager fileManager = _context.getFileManager();

		String fullPath = path;
		if(!DatastoreFile.isFullDatastorePath(fullPath))
			fullPath = String.format("[%s] %s", datastoreName, path);

		fileManager.makeDirectory(fullPath, datacenter, true);
	}
	
	public String getDatastoreRootPath() throws Exception {
		return String.format("[%s]", getName());
	}
	
	public String getDatastorePath(String relativePathWithoutDatastoreName) throws Exception {
		return getDatastorePath(relativePathWithoutDatastoreName, false);
	}
	
	public String getDatastorePath(String relativePathWithoutDatastoreName, boolean endWithPathDelimiter) throws Exception {
		String path = String.format("[%s] %s", getName(), relativePathWithoutDatastoreName);
		if(endWithPathDelimiter) {
			if(!path.endsWith("/"))
				return path + "/";
		}
		return path;
	}

	public boolean deleteFile(String path, ManagedObjectReference morDc, boolean testExistence) throws Exception {
		String datastoreName = getName();
		ManagedObjectReference morFileManager = _context.getServiceContent().getFileManager();

		String fullPath = path;
		if(!DatastoreFile.isFullDatastorePath(fullPath))
			fullPath = String.format("[%s] %s", datastoreName, path);
        DatastoreFile file = new DatastoreFile(fullPath);
        // Test if file specified is null or empty. We don't need to attempt to delete and return success.
        if (file.getFileName() == null || file.getFileName().isEmpty()) {
            return true;
        }

		try {
			if(testExistence && !fileExists(fullPath)) {
                String searchResult = searchFileInSubFolders(file.getFileName(), true);
                if (searchResult == null) {
                    return true;
                } else {
                    fullPath = searchResult;
                }
			}
		} catch(Exception e) {
			s_logger.info("Unable to test file existence due to exception " + e.getClass().getName() + ", skip deleting of it");
			return true;
		}
		Datacenter datacenter = new Datacenter(_context.getServerConnection(), morDc);
		Task task = _context.getFileManager().deleteDatastoreFile_Task(fullPath, datacenter);
		ManagedObjectReference morTask = task.getMOR();

		boolean result = _context.getVimClient().waitForTask(morTask);
		if(result) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware deleteDatastoreFile_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		return false;
	}

	public boolean copyDatastoreFile(String srcFilePath, ManagedObjectReference morSrcDc,
		ManagedObjectReference morDestDs, String destFilePath, ManagedObjectReference morDestDc,
		boolean forceOverwrite) throws Exception {

		String srcDsName = getName();
		DatastoreMO destDsMo = new DatastoreMO(_context, morDestDs);
		String destDsName = destDsMo.getName();

		Datacenter sourceDatacenter = new Datacenter(_context.getServerConnection(), morSrcDc);
		String srcFullPath = srcFilePath;
		if(!DatastoreFile.isFullDatastorePath(srcFullPath))
			srcFullPath = String.format("[%s] %s", srcDsName, srcFilePath);

		Datacenter destinationDatacenter = new Datacenter(_context.getServerConnection(), morDestDc);
		String destFullPath = destFilePath;
		if(!DatastoreFile.isFullDatastorePath(destFullPath))
			destFullPath = String.format("[%s] %s", destDsName, destFilePath);

		Task task = _context.getFileManager().copyDatastoreFile_Task(srcFullPath, sourceDatacenter, destFullPath, destinationDatacenter, forceOverwrite);
		ManagedObjectReference morTask = task.getMOR();

		boolean result = _context.getVimClient().waitForTask(morTask);
		if(result) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware copyDatastoreFile_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		return false;
	}

	public boolean moveDatastoreFile(String srcFilePath, ManagedObjectReference morSrcDc,
		ManagedObjectReference morDestDs, String destFilePath, ManagedObjectReference morDestDc,
		boolean forceOverwrite) throws Exception {

		String srcDsName = getName();
		DatastoreMO destDsMo = new DatastoreMO(_context, morDestDs);
		String destDsName = destDsMo.getName();

        Datacenter sourceDatacenter = new Datacenter(_context.getServerConnection(), morSrcDc);
		String srcFullPath = srcFilePath;
		if(!DatastoreFile.isFullDatastorePath(srcFullPath))
			srcFullPath = String.format("[%s] %s", srcDsName, srcFilePath);

        Datacenter destinationDatacenter = new Datacenter(_context.getServerConnection(), morDestDc);
		String destFullPath = destFilePath;
		if(!DatastoreFile.isFullDatastorePath(destFullPath))
			destFullPath = String.format("[%s] %s", destDsName, destFilePath);

		Task task = _context.getFileManager().moveDatastoreFile_Task(srcFullPath, sourceDatacenter, destFullPath, destinationDatacenter, forceOverwrite);
		ManagedObjectReference morTask = task.getMOR();

		boolean result = _context.getVimClient().waitForTask(morTask);
		if(result) {
			_context.waitForTaskProgressDone(morTask);
			return true;
		} else {
        	s_logger.error("VMware moveDatgastoreFile_Task failed due to " + TaskMO.getTaskFailureInfo(_context, morTask));
		}
		return false;
	}

	public String[] getVmdkFileChain(String rootVmdkDatastoreFullPath) throws Exception {
		Pair<DatacenterMO, String> dcPair = getOwnerDatacenter();

		List<String> files = new ArrayList<String>();
		files.add(rootVmdkDatastoreFullPath);

		String currentVmdkFullPath = rootVmdkDatastoreFullPath;
		while(true) {
			String url = getContext().composeDatastoreBrowseUrl(dcPair.second(), currentVmdkFullPath);
			byte[] content = getContext().getResourceContent(url);
			if(content == null || content.length == 0)
				break;

			VmdkFileDescriptor descriptor = new VmdkFileDescriptor();
			descriptor.parse(content);

			String parentFileName = descriptor.getParentFileName();
			if(parentFileName == null)
				break;

			if(parentFileName.startsWith("/")) {
				// when parent file is not at the same directory as it is, assume it is at parent directory
				// this is only valid in cloud.com primary storage deployment
				DatastoreFile dsFile = new DatastoreFile(currentVmdkFullPath);
				String dir = dsFile.getDir();
				if(dir != null && dir.lastIndexOf('/') > 0)
					dir = dir.substring(0, dir.lastIndexOf('/'));
				else
					dir = "";

				currentVmdkFullPath = new DatastoreFile(dsFile.getDatastoreName(), dir,
					parentFileName.substring(parentFileName.lastIndexOf('/') + 1)).getPath();
				files.add(currentVmdkFullPath);
			} else {
				currentVmdkFullPath = DatastoreFile.getCompanionDatastorePath(currentVmdkFullPath, parentFileName);
				files.add(currentVmdkFullPath);
			}
		}

		return files.toArray(new String[0]);
	}

	@Deprecated
	public String[] listDirContent(String path) throws Exception {
		String fullPath = path;
		if(!DatastoreFile.isFullDatastorePath(fullPath))
			fullPath = String.format("[%s] %s", getName(), fullPath);

		Pair<DatacenterMO, String> dcPair = getOwnerDatacenter();
		String url = getContext().composeDatastoreBrowseUrl(dcPair.second(), fullPath);

		// TODO, VMware currently does not have a formal API to list Datastore directory content,
		// folloing hacking may have performance hit if datastore has a large number of files
		return _context.listDatastoreDirContent(url);
	}

	public boolean fileExists(String fileFullPath) throws Exception {
		DatastoreFile file = new DatastoreFile(fileFullPath);
		DatastoreFile dirFile = new DatastoreFile(file.getDatastoreName(), file.getDir());

		HostDatastoreBrowserMO browserMo = getHostDatastoreBrowserMO();

		s_logger.info("Search file " + file.getFileName() + " on " + dirFile.getPath());
		HostDatastoreBrowserSearchResults results = browserMo.searchDatastore(dirFile.getPath(), file.getFileName(), true);
		if(results != null) {
			FileInfo[] info = results.getFile();
			if(info != null && info.length > 0) {
				s_logger.info("File " + fileFullPath + " exists on datastore");
				return true;
			}
		}

		s_logger.info("File " + fileFullPath + " does not exist on datastore");
		return false;
	}

	public boolean folderExists(String folderParentDatastorePath, String folderName) throws Exception {
		HostDatastoreBrowserMO browserMo = getHostDatastoreBrowserMO();

		HostDatastoreBrowserSearchResults results = browserMo.searchDatastore(folderParentDatastorePath, folderName, true);
		if(results != null) {
			FileInfo[] info = results.getFile();
			if(info != null && info.length > 0) {
				s_logger.info("Folder " + folderName + " exists on datastore");
				return true;
			}
		}

		s_logger.info("Folder " + folderName + " does not exist on datastore");
		return false;
	}

    public String searchFileInSubFolders(String fileName, boolean caseInsensitive) throws Exception {
        String datastorePath = "[" + getName() + "]";
        String rootDirectoryFilePath = String.format("%s %s", datastorePath, fileName);
        if(fileExists(rootDirectoryFilePath)) {
            return rootDirectoryFilePath;
        }

        String parentFolderPath = null;
        String absoluteFileName = null;
        s_logger.info("Searching file " + fileName + " in " + datastorePath);

        HostDatastoreBrowserMO browserMo = getHostDatastoreBrowserMO();
        ArrayList<HostDatastoreBrowserSearchResults> results = browserMo.searchDatastoreSubFolders("[" + getName() + "]", fileName, caseInsensitive);
        if (results != null && results.size() > 1) {
            s_logger.warn("Multiple files with name " + fileName + " exists in datastore " + datastorePath + ". Trying to choose first file found in search attempt.");
        }
        for (HostDatastoreBrowserSearchResults result : results) {
            FileInfo[] info = result.getFile();
            if (info != null && info.length > 0) {
                for (FileInfo fi : info) {
                    absoluteFileName = parentFolderPath = result.getFolderPath();
                    s_logger.info("Found file " + fileName + " in datastore at " + absoluteFileName);
                    if(parentFolderPath.endsWith("]"))
                        absoluteFileName += " ";
                    absoluteFileName += fi.getPath();
                    break;
                }
            }
        }
        return absoluteFileName;
    }
}
