package geo.azure.test;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.SharedAccessAccountPolicy;

public class test {

	public static void main(String[] args) throws Exception, Exception {
		// TODO Auto-generated method stub
		String resourceName ="";
		String vmName ="";
		String vmLocation ="";
		String storageAccountName = "";

		
		CloudStorageAccount storageAccount = CloudStorageAccount.parse("连接字符串");
		SharedAccessAccountPolicy policy = new SharedAccessAccountPolicy();
		policy.setPermissionsFromString("racupwdl");
		policy.setServiceFromString("bt");
		policy.setResourceTypeFromString("sco");
		long date = new Date().getTime();
		long expiryDate = new Date(date + 8640000).getTime();
		policy.setSharedAccessExpiryTime(new Date(expiryDate));
		String storageSAStoken=storageAccount.generateSharedAccessSignature(policy);
		VmDiagnosticOperation diagnosticOperation = new VmDiagnosticOperation(
		"tennet id",
		"clientid", 
		"secret",
		"sub id");

		String result = diagnosticOperation.EnableVMDiagnostic(
		resourceName, 
		vmName, 
		vmLocation,
		storageAccountName, 
		storageSAStoken);
		System.out.println(result);


	}

}
