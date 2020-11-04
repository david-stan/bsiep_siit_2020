package scripts.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;

public class RestrictFileAccess {
	
	public static void restrictAccess(String filePath) {
		Path path = Paths.get(filePath);
		try {
			UserPrincipal authenticatedUsers = path.getFileSystem().getUserPrincipalLookupService()
			        .lookupPrincipalByName("Administrators");
			
			AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
			
			List<AclEntry> acl = new ArrayList<AclEntry>();

			AclEntry adminAclEntry = null;
			for(AclEntry aclEntry: view.getAcl()) {
				if (aclEntry.principal().equals(authenticatedUsers)) {
					adminAclEntry = aclEntry;
				}
			}
			acl.add(adminAclEntry);
			view.setAcl(acl);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String absFilePath = new File("").getAbsolutePath();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(absFilePath + "/paths.txt"));
			String line = reader.readLine();
			while (line != null) {
				restrictAccess(line);
				line = reader.readLine();
			}
			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}
