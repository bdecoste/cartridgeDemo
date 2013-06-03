package org.jboss.cartridgeDemo;

import java.io.IOException;
import java.io.OutputStream;
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
		
		OutputStream out = null;
		Socket socket = null;
		try {
			out = response.getOutputStream();
			String value = "OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST " + System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST") + "\n";
			out.write(value.getBytes());
			value = "OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT " + System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT") + "\n";
			out.write(value.getBytes());
			value = "OPENSHIFT_ACTIVEMQ_IP " + System.getenv("OPENSHIFT_ACTIVEMQ_IP") + "\n";
			out.write(value.getBytes());
			value = "---------------------------------------------------\n";
			out.write(value.getBytes());
			value = "OPENSHIFT_INFINISPAN_HOST " + System.getenv("OPENSHIFT_INFINISPAN_HOST") + "\n";
			out.write(value.getBytes());
			value = "OPENSHIFT_INFINISPAN_PORT " + System.getenv("OPENSHIFT_INFINISPAN_PORT") + "\n";
			out.write(value.getBytes());
			value = "OPENSHIFT_INFINISPAN_IP " + System.getenv("OPENSHIFT_INFINISPAN_IP") + "\n";
			out.write(value.getBytes());
			
			try {
				socket = new Socket(System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST"), Integer.parseInt(System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT")));
				value = "connected " + socket.isConnected() + "\n";
				out.write(value.getBytes());
			} catch (Exception e){
				e.printStackTrace();
				value = "caught " + e + "\n";
				out.write(value.getBytes());
			}
			
			try {
				socket = new Socket(System.getenv("OPENSHIFT_INFINISPAN_HOST"), Integer.parseInt(System.getenv("OPENSHIFT_INFINISPAN_PORT")));
				value = "connected " + socket.isConnected() + "\n";
				out.write(value.getBytes());
			} catch (Exception e){
				e.printStackTrace();
				value = "caught " + e + "\n";
				out.write(value.getBytes());
			}
			
			Thread consumer = new Thread(new SimpleConsumer(Integer.toString(key), System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST"), System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT"), System.getenv("OPENSHIFT_INFINISPAN_HOST"), System.getenv("OPENSHIFT_INFINISPAN_PORT")));
			consumer.run();
			
			SimpleProducer producer = new SimpleProducer();
			producer.produce(System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_HOST"), System.getenv("OPENSHIFT_ACTIVEMQ_OPENWIRE_PORT"));
			
			value = "Produced!!\n";
			out.write(value.getBytes());
			
			MemcachedClient client = new MemcachedClient(new InetSocketAddress(System.getenv("OPENSHIFT_INFINISPAN_HOST"), Integer.parseInt(System.getenv("OPENSHIFT_INFINISPAN_PORT"))));
			for (int i = 0 ; i < key ; ++i ){
				Object cached = client.get(Integer.toString(i));
			
				value = "Cached " + i + "=" + cached + "\n";
				out.write(value.getBytes());
			}
			
			++key;
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			if (out != null)
				try {out.close();} catch (Exception e){};
		}
		
	}

}
