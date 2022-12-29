package de.intranda.goobi.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.intranda.goobi.plugins.licence.Contract;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j2
public class LicencesAdministrationPlugin implements IAdministrationPlugin {

    @Getter
    private String title = "intranda_administration_licences";
    private List<Contract> contracts;
    
    @Override
    public PluginType getType() {
        return PluginType.Administration;
    }

    @Override
    public String getGui() {
        return "/uii/plugin_administration_licences.xhtml";
    }

    public List<Contract> getContracts(){
    	if (contracts == null) {
    		loadContracts();
    	}
    	return contracts;
    }
    
    /**
	 * get all contracts via REST
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void loadContracts() {
		String url = ConfigPlugins.getPluginConfig(title).getString("url");
        String login = ConfigPlugins.getPluginConfig(title).getString("login");
        String password = ConfigPlugins.getPluginConfig(title).getString("password");
        String auth = Base64.getEncoder().encodeToString((login + ":" + password).getBytes());
		
		// send a GET request to localhost
		try {
			HttpResponse response;
			response = Request.Get(url + "/api/contracts/")
					.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth)
					.addHeader("accept", "application/json")
					.useExpectContinue()
				    .execute().returnResponse();
		
			// if response code is 200 analyse the result
			if (response.getStatusLine().getStatusCode() == 200) {
				contracts = new ArrayList<Contract>();
				
				// get result as string and convert it into JSON object
				String responseAsString = EntityUtils.toString(response.getEntity());
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(responseAsString);
				
				// run through all contract nodes
				ArrayNode arrayNode = (ArrayNode) jsonNode;
				for (int i = 0; i < arrayNode.size(); i++) {
					JsonNode node = arrayNode.get(i);
					
					Contract c = new Contract();
					c.setName(node.get("name").asText());
					c.setType(node.get("type").asText());
					c.setOrdernumber(node.get("ordernumber").asText());
					c.setEnabled(node.get("enabled").asBoolean());
					c.setSize(node.get("size").asLong());
					c.setUsedSize(node.get("usedSize").asLong());
					c.setUsedPercent(node.get("usedPercent").asLong());
					contracts.add(c);
				}
			}
		} catch (IOException e) {
			Helper.setFehlerMeldung("Error while contacting the licence server", e);
			log.error("Error while contacting the licence server", e);
		}
	}
    
    
    /**
     * Constructor
     */
    public LicencesAdministrationPlugin() {
        log.debug("Licence admnistration plugin started");
    }   
}
