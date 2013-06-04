package org.jboss.cartridgeDemo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.spy.memcached.MemcachedClient;

import org.apache.log4j.*;

/**
 * Servlet implementation class SfsbServlet
 */
@WebServlet("/demo")
public class DemoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static int key = 0;

	Logger LOG = Logger.getLogger(DemoServlet.class); 

    /**
     * Default constructor. 
     */
    public DemoServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		doIt(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		doIt(request, response);
	}
	
	protected void doIt(HttpServletRequest request, HttpServletResponse response) {
		
		PrintWriter out = null;
		Socket socket = null;
		try {
			response.setContentType("text/html");
			out = response.getWriter();
			
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
                    "Transitional//EN\">\n" +
                    "<HTML>\n" +
                    "<HEAD><TITLE>Hello WWW</TITLE></HEAD>\n" +
					"<BODY>\n" );
			
			
			out.println("<H2>Connecting to ActiveMQ using:</H2>");
			out.println("<H3>&nbsp&nbsp&nbsp" + System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST") +":" + System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT") + "</H3>");
			out.println("<H2>Connecting to ActiveMQ using:</H2>");
			out.println("<H3>&nbsp&nbsp&nbsp" + System.getenv("OPENSHIFT_INFINISPAN_HOST") +":" + System.getenv("OPENSHIFT_INFINISPAN_PORT") + "</H3>");
			
			MemcachedClient client = new MemcachedClient(new InetSocketAddress(System.getenv("OPENSHIFT_INFINISPAN_HOST"), Integer.parseInt(System.getenv("OPENSHIFT_INFINISPAN_PORT"))));
			
			SimpleConsumer consumer = new SimpleConsumer(Integer.toString(key), System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST"), System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT"), System.getenv("OPENSHIFT_INFINISPAN_HOST"), System.getenv("OPENSHIFT_INFINISPAN_PORT"));
			
			if (key == 0 ) {
				for (int i = 0 ; i < 20 ; ++i ){
					client.delete(Integer.toString(i));
				}
				
				consumer.clear();
			}
			
			SimpleProducer producer = new SimpleProducer();
			String message = producer.produce(System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST"), System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT"));
			
			LOG.info("!!!!!!!!!! sent " + message);
			
			out.println("<H2>Sent Message " + message + "</H2>");
			
			
			consumer.run();
			
			out.println("<H2>Infinispan Cache View</H2>");
			
			for (int i = 0 ; i < key ; ++i ){
				Object cached = client.get(Integer.toString(i));
				
				out.println("<H3>&nbsp&nbsp&nbsp" + i + "=" + cached + "</H3>");
			}
			
			out.println("</BODY></HTML>");
			
			++key;
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			if (out != null)
				try {out.close();} catch (Exception e){};
		}
		
	}

}
