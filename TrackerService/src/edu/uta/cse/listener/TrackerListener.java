package edu.uta.cse.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Lifecycle Listener implementation class TrackerListener
 *
 */
@WebListener
public class TrackerListener implements ServletContextListener {
	private static Logger LOGGER = LoggerFactory.getLogger(TrackerListener.class);
    /**
     * Default constructor. 
     */
    public TrackerListener() {
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    	LOGGER.info("Context is being destroyed here...");
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent init) {
    	LOGGER.info("context initialization..!");
    	Map<String, Object> peersTable = new HashMap<String, Object>();
    	Map<String, Object> seedersTable = new HashMap<String, Object>();
		Map<String, Object> leechersTable = new HashMap<String, Object>();

/*    	// There should be atleast one seed present
    	List<String> peerList = new ArrayList<String>();
    	peerList.add("id_peer1");
    	seedersTable.put("p2pexamples.pdf", peerList);
    	
    	// Add the first seed information in the peers table
    	Map<String, String> peerInfo = new HashMap<String, String>();
    	peerInfo.put("ip", "localhost");
    	peerInfo.put("port", "14001");
    	peerInfo.put("parts", "11111");
    	peersTable.put("id_peer1", peerInfo);
    	*/
    	// add the hash tables to the application context
    	ServletContext application = init.getServletContext();
    	application.setAttribute("peersTable", peersTable);
    	application.setAttribute("seedersTable", seedersTable);
    	application.setAttribute("leechersTable", leechersTable);
    }
	
}
