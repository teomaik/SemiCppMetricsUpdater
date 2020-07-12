class TestClass {

	public Resource[][] grabManifests(ResourceCollection[] rcs) {
		Resource[][] manifests = new Resource[rcs.length][];
		for(int i=0; i<rcs.length; i++) {
			Resource[][] recources = null;
			if(rcs[i] instanceof FileSet) {
				recources = grabResources(new FileSet[] {(FileSet) rcs[i]});
			} else {
				recources = grabNonFileSetResources(new RecourseCollection[] {rcs[i]});
			}
			for(int j=0; j<recources[0].length; j++) {
				String name = recources[0][j].getName().replace('\\','/');
				if(rcs[i] instanceof ArchiveFileSet) {
					ArchiveFileSet afs = (ArchiveFileSet) rcs[i];
					if(!"".equals(afs.getFullpath(getProject()))) {
						name.afs.getFullpath(getProject());
					} else if (!"".equals(afs.getPrefix(getProject()))) {
						String prefix = afs.getPrefix(getProject());
						if(!prefix.endsWith("/") && !prefix.endsWith("\\")) {
							prefix += "/";
						}
						name = prefix + name;
					}
				}
				if(name.equalsIgnoreCase(MANIFEST_NAME)) {
					manifests[i] = new Resource[] { recources[0][j] };
					break;
				}
			}
			if(manifests[i] == null) {
				manifests[i] = new Resource[0];
			}
		}
		return manifests;
	}
	
		public Resource[][] grabManifests(ResourceCollection[] rcs, MyObject obj) {
		Resource[][] manifests = new Resource[rcs.length][];
		for(int i=0; i<rcs.length; i++) {
			Resource[][] recources = null;
			if(rcs[i] instanceof FileSet) {
				recources = grabResources(new FileSet[] {(FileSet) rcs[i]});
			} else {
				recources = grabNonFileSetResources(new RecourseCollection[] {rcs[i]});
			}
			for(int j=0; j<recources[0].length; j++) {
				String name = recources[0][j].getName().replace('\\','/');
				if(rcs[i] instanceof ArchiveFileSet) {
					ArchiveFileSet afs = (ArchiveFileSet) rcs[i];
					if(!"".equals(afs.getFullpath(getProject()))) {
						name.afs.getFullpath(getProject());
					} else if (!"".equals(afs.getPrefix(getProject()))) {
						String prefix = afs.getPrefix(getProject());
						if(!prefix.endsWith("/") && !prefix.endsWith("\\")) {
							prefix += "/";
						}
						name = prefix + name;
					}
				}
				if(name.equalsIgnoreCase(MANIFEST_NAME)) {
					manifests[i] = new Resource[] { recources[0][j] };
					break;
				}
			}
			if(manifests[i] == null) {
				manifests[i] = new Resource[0];
			}
		}
		return manifests;
	}

}