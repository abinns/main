import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import abinns.appserv.backend.U;

public class PrinterMon
{

	public PrinterMon(String string)
	{
		// setting up target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(new IpAddress(string));
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version1);
		// creating PDU
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(new int[] { 1, 3, 6, 1, 2, 1, 1, 1 })));
		pdu.add(new VariableBinding(new OID(new int[] { 1, 3, 6, 1, 2, 1, 1, 2 })));
		pdu.setType(PDU.GETNEXT);
		// sending request
		ResponseListener listener = new ResponseListener()
		{
			@Override
			public void onResponse(ResponseEvent event)
			{
				// Always cancel async request when response has been received
				// otherwise a memory leak is created! Not canceling a request
				// immediately can be useful when sending a request to a
				// broadcast
				// address.
				((Snmp) event.getSource()).cancel(event.getRequest(), this);
				U.p("Received response PDU is: " + event.getResponse());
			}
		};
		Snmp snmp = null;
		try
		{
			snmp = new Snmp(new DefaultUdpTransportMapping());
			snmp.send(pdu, target, null, listener);
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
