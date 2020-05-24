package geo.azure.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

public class VmDiagnosticOperation {

	private String tentantId;
	private String clientId;
	private String clientSecret;
	private String subscriptionId;

	public VmDiagnosticOperation(String tentantId, String clientId, String clientSecret, String subscriptionId) {
		this.tentantId = tentantId;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.subscriptionId = subscriptionId;
	}

	private String putRequest(String accessToken, String requestUrl, String requestBody) {
		HttpClient httpclient = HttpClients.createDefault();

		try {
			URIBuilder builder = new URIBuilder(requestUrl);

			URI uri = builder.build();
			HttpPut request = new HttpPut(uri);
			request.setHeader("Authorization", "Bearer " + accessToken);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Host", "management.chinacloudapi.cn");

			StringEntity reqEntity = new StringEntity(requestBody.toString());
			request.setEntity(reqEntity);
			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				return EntityUtils.toString(entity);
			}

			return "No Result!";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String getAccessToken() {

		AuthenticationContext context = null;
		AuthenticationResult result = null;
		ExecutorService service = null;
		service = Executors.newFixedThreadPool(1);

		try {
			context = new AuthenticationContext(String.format("%s/%s", "https://login.chinacloudapi.cn", tentantId),
					true, service);
			ClientCredential cred = new ClientCredential(clientId, clientSecret);
			Future<AuthenticationResult> future = context.acquireToken("https://management.chinacloudapi.cn/", cred,
					null);

			result = future.get();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			service.shutdown();
		}

		return result.getAccessToken();
	}

	private String generatorWadCfg(String subscriptionId, String resourceName, String vmName) {

		String content = null;
		try {
			content = Files.toString(
					new File("C:\\Users\\guan.jun\\Desktop\\aboutCase\\WadCfg.xml"),
					Charsets.UTF_8);

			content = content.replace("{0}", subscriptionId).replace("{1}", resourceName).replace("{2}", vmName);
			System.out.println(content);

			final Base64.Encoder encoder = Base64.getEncoder();
			final byte[] textByte = content.getBytes("UTF-8");
			content = encoder.encodeToString(textByte);
			System.out.println(content);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}

	private String generatorBodyCfg(String subscriptionId, String resourceName, String vmName, String vmLocation,
			String xmlCfg, String storageAccountName, String storageSAStoken) {
		StringBuilder requestBody = new StringBuilder();

		requestBody.append("{'type':'Microsoft.Compute/virtualMachines/extensions',");
		requestBody.append("'id':'/subscriptions/" + subscriptionId + "/resourceGroups/" + resourceName + "/providers/Microsoft.Compute/virtualMachines/" + vmName + "/extensions/IaaSDiagnostics',");
		requestBody.append("'location':'" + vmLocation + "',");
		requestBody.append("'name':'IaaSDiagnostics',");
		requestBody.append("'properties':{'type':'LinuxDiagnostic','publisher':'Microsoft.Azure.Diagnostics','typeHandlerVersion':'3.0',");
		String resourceId="/subscriptions/7ca276d4-533f-40e0-840c-56c10ed516d7/resourceGroups/test0521/providers/Microsoft.Compute/virtualMachines/guanjuntest0521";
		requestBody.append("'settings':{'StorageAccount':'"+storageAccountName+"','ladCfg':{'diagnosticMonitorConfiguration':{'eventVolume':'Medium','metrics':{'metricAggregation':[{'scheduledTransferPeriod':'PT1M'},{'scheduledTransferPeriod':'PT1H'}], 'resourceId': '"+resourceId+"'},'syslogEvents':{'syslogEventConfiguration':{'LOG_AUTH':'LOG_DEBUG','LOG_AUTHPRIV':'LOG_DEBUG','LOG_CRON':'LOG_DEBUG','LOG_DAEMON':'LOG_DEBUG','LOG_FTP':'LOG_DEBUG','LOG_KERN':'LOG_DEBUG','LOG_LOCAL0':'LOG_DEBUG','LOG_LOCAL1':'LOG_DEBUG','LOG_LOCAL2':'LOG_DEBUG','LOG_LOCAL3':'LOG_DEBUG','LOG_LOCAL4':'LOG_DEBUG','LOG_LOCAL5':'LOG_DEBUG','LOG_LOCAL6':'LOG_DEBUG','LOG_LOCAL7':'LOG_DEBUG','LOG_LPR':'LOG_DEBUG','LOG_MAIL':'LOG_DEBUG','LOG_NEWS':'LOG_DEBUG','LOG_SYSLOG':'LOG_DEBUG','LOG_USER':'LOG_DEBUG','LOG_UUCP':'LOG_DEBUG'}},'performanceCounters':{'performanceCounterConfiguration':[{'annotation':[{'displayName':'CPU IO wait time','locale':'en-us'}],'class':'processor','condition':'IsAggregate=TRUE','counter':'percentiowaittime','counterSpecifier':'/builtin/processor/percentiowaittime','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'CPU user time','locale':'en-us'}],'class':'processor','condition':'IsAggregate=TRUE','counter':'percentusertime','counterSpecifier':'/builtin/processor/percentusertime','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'CPU nice time','locale':'en-us'}],'class':'processor','condition':'IsAggregate=TRUE','counter':'percentnicetime','counterSpecifier':'/builtin/processor/percentnicetime','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'CPU percentage guest OS','locale':'en-us'}],'class':'processor','condition':'IsAggregate=TRUE','counter':'percentprocessortime','counterSpecifier':'/builtin/processor/percentprocessortime','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'CPU interrupt time','locale':'en-us'}],'class':'processor','condition':'IsAggregate=TRUE','counter':'percentinterrupttime','counterSpecifier':'/builtin/processor/percentinterrupttime','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'CPU idle time','locale':'en-us'}],'class':'processor','condition':'IsAggregate=TRUE','counter':'percentidletime','counterSpecifier':'/builtin/processor/percentidletime','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'CPU privileged time','locale':'en-us'}],'class':'processor','condition':'IsAggregate=TRUE','counter':'percentprivilegedtime','counterSpecifier':'/builtin/processor/percentprivilegedtime','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Memory available','locale':'en-us'}],'class':'memory','counter':'availablememory','counterSpecifier':'/builtin/memory/availablememory','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Swap percent used','locale':'en-us'}],'class':'memory','counter':'percentusedswap','counterSpecifier':'/builtin/memory/percentusedswap','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Memory used','locale':'en-us'}],'class':'memory','counter':'usedmemory','counterSpecifier':'/builtin/memory/usedmemory','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Page reads','locale':'en-us'}],'class':'memory','counter':'pagesreadpersec','counterSpecifier':'/builtin/memory/pagesreadpersec','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Swap available','locale':'en-us'}],'class':'memory','counter':'availableswap','counterSpecifier':'/builtin/memory/availableswap','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Swap percent available','locale':'en-us'}],'class':'memory','counter':'percentavailableswap','counterSpecifier':'/builtin/memory/percentavailableswap','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Mem. percent available','locale':'en-us'}],'class':'memory','counter':'percentavailablememory','counterSpecifier':'/builtin/memory/percentavailablememory','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Pages','locale':'en-us'}],'class':'memory','counter':'pagespersec','counterSpecifier':'/builtin/memory/pagespersec','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Swap used','locale':'en-us'}],'class':'memory','counter':'usedswap','counterSpecifier':'/builtin/memory/usedswap','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Memory percentage','locale':'en-us'}],'class':'memory','counter':'percentusedmemory','counterSpecifier':'/builtin/memory/percentusedmemory','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Page writes','locale':'en-us'}],'class':'memory','counter':'pageswrittenpersec','counterSpecifier':'/builtin/memory/pageswrittenpersec','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Network in guest OS','locale':'en-us'}],'class':'network','counter':'bytesreceived','counterSpecifier':'/builtin/network/bytesreceived','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Network total bytes','locale':'en-us'}],'class':'network','counter':'bytestotal','counterSpecifier':'/builtin/network/bytestotal','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Network out guest OS','locale':'en-us'}],'class':'network','counter':'bytestransmitted','counterSpecifier':'/builtin/network/bytestransmitted','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Network collisions','locale':'en-us'}],'class':'network','counter':'totalcollisions','counterSpecifier':'/builtin/network/totalcollisions','type':'builtin','unit':'Count','sampleRate':'PT15S'},{'annotation':[{'displayName':'Packets received errors','locale':'en-us'}],'class':'network','counter':'totalrxerrors','counterSpecifier':'/builtin/network/totalrxerrors','type':'builtin','unit':'Count','sampleRate':'PT15S'},{'annotation':[{'displayName':'Packets sent','locale':'en-us'}],'class':'network','counter':'packetstransmitted','counterSpecifier':'/builtin/network/packetstransmitted','type':'builtin','unit':'Count','sampleRate':'PT15S'},{'annotation':[{'displayName':'Packets received','locale':'en-us'}],'class':'network','counter':'packetsreceived','counterSpecifier':'/builtin/network/packetsreceived','type':'builtin','unit':'Count','sampleRate':'PT15S'},{'annotation':[{'displayName':'Packets sent errors','locale':'en-us'}],'class':'network','counter':'totaltxerrors','counterSpecifier':'/builtin/network/totaltxerrors','type':'builtin','unit':'Count','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem transfers/sec','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'transferspersecond','counterSpecifier':'/builtin/filesystem/transferspersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem % free space','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'percentfreespace','counterSpecifier':'/builtin/filesystem/percentfreespace','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem % used space','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'percentusedspace','counterSpecifier':'/builtin/filesystem/percentusedspace','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem used space','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'usedspace','counterSpecifier':'/builtin/filesystem/usedspace','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem read bytes/sec','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'bytesreadpersecond','counterSpecifier':'/builtin/filesystem/bytesreadpersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem free space','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'freespace','counterSpecifier':'/builtin/filesystem/freespace','type':'builtin','unit':'Bytes','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem % free inodes','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'percentfreeinodes','counterSpecifier':'/builtin/filesystem/percentfreeinodes','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem bytes/sec','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'bytespersecond','counterSpecifier':'/builtin/filesystem/bytespersecond','type':'builtin','unit':'BytesPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem reads/sec','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'readspersecond','counterSpecifier':'/builtin/filesystem/readspersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem write bytes/sec','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'byteswrittenpersecond','counterSpecifier':'/builtin/filesystem/byteswrittenpersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem writes/sec','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'writespersecond','counterSpecifier':'/builtin/filesystem/writespersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Filesystem % used inodes','locale':'en-us'}],'class':'filesystem','condition':'IsAggregate=TRUE','counter':'percentusedinodes','counterSpecifier':'/builtin/filesystem/percentusedinodes','type':'builtin','unit':'Percent','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk read guest OS','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'readbytespersecond','counterSpecifier':'/builtin/disk/readbytespersecond','type':'builtin','unit':'BytesPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk writes','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'writespersecond','counterSpecifier':'/builtin/disk/writespersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk transfer time','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'averagetransfertime','counterSpecifier':'/builtin/disk/averagetransfertime','type':'builtin','unit':'Seconds','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk transfers','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'transferspersecond','counterSpecifier':'/builtin/disk/transferspersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk write guest OS','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'writebytespersecond','counterSpecifier':'/builtin/disk/writebytespersecond','type':'builtin','unit':'BytesPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk read time','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'averagereadtime','counterSpecifier':'/builtin/disk/averagereadtime','type':'builtin','unit':'Seconds','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk write time','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'averagewritetime','counterSpecifier':'/builtin/disk/averagewritetime','type':'builtin','unit':'Seconds','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk total bytes','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'bytespersecond','counterSpecifier':'/builtin/disk/bytespersecond','type':'builtin','unit':'BytesPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk reads','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'readspersecond','counterSpecifier':'/builtin/disk/readspersecond','type':'builtin','unit':'CountPerSecond','sampleRate':'PT15S'},{'annotation':[{'displayName':'Disk queue length','locale':'en-us'}],'class':'disk','condition':'IsAggregate=TRUE','counter':'averagediskqueuelength','counterSpecifier':'/builtin/disk/averagediskqueuelength','type':'builtin','unit':'Count','sampleRate':'PT15S'}]}},'sampleRateInSeconds':15}},");
		requestBody.append("'protectedSettings':{'storageAccountName':'" + storageAccountName + "','storageAccountSasToken':'" + storageSAStoken + "','storageAccountEndPoint':'https://core.chinacloudapi.cn'},");
		requestBody.append("'autoUpgradeMinorVersion':true,'instanceView':null}}");
		System.out.println(requestBody.toString());
		return requestBody.toString();
	}

	public String EnableVMDiagnostic(String resourceName, String vmName, String vmLocation, String storageAccountName,
			String storageAccountKey) {

		String token = this.getAccessToken();
		String xmlCfg = this.generatorWadCfg(subscriptionId, resourceName, vmName);
		String requestUrl = "https://management.chinacloudapi.cn/subscriptions/" + subscriptionId + "/resourceGroups/"+ resourceName + "/providers/Microsoft.Compute/virtualMachines/" + vmName + "/extensions/IaaSDiagnostics?api-version=2019-03-01";
		String requestBody = this.generatorBodyCfg(subscriptionId, resourceName, vmName, vmLocation, xmlCfg,storageAccountName, storageAccountKey);
		String result = this.putRequest(token, requestUrl, requestBody);
		return result;
	}

}
