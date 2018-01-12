package psx.capture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.PcapPacket;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.TcpPacket;

public class Main {

	final String FEED_IDENTIFIER = "FEED";

	public static void main(String[] args) throws Exception {
		new Main().startCapture();
	}

	private void startCapture() throws Exception {
		
		FileOutputStream file = new FileOutputStream(new File("C:/TEMP/feed.txt"), true);

		InetAddress addr = InetAddress.getByName("192.168.206.183");

		PcapNetworkInterface nif = Pcaps.getDevByAddress(addr);

		if (nif == null)
			throw new IllegalStateException("PcapNetworkInterface is null");

		int snapLen = 65536;
		PromiscuousMode mode = PromiscuousMode.PROMISCUOUS;
		int timeout = 10;
		PcapHandle handle = null;
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		try {
			handle = nif.openLive(snapLen, mode, timeout);
			// handle.setDirection(PcapDirection.IN);
			handle.setFilter("ip src 202.125.136.82", BpfCompileMode.OPTIMIZE);
			final ByteBuffer buffer = ByteBuffer.allocate(1024);
			file.write(new String("Started at: "+new Date()+"\n").getBytes());
			while (true) {
				handle.dispatch(1024 * 5, new PacketListener() {
					public void gotPacket(PcapPacket packet) {
						if (packet.getPayload() != null) {
							TcpPacket tcpPacket = packet.get(TcpPacket.class);
							if (tcpPacket != null && tcpPacket.getPayload() != null
									&& tcpPacket.getPayload().getRawData().length > 8) {
								String data = new String(tcpPacket.getPayload().getRawData(), 0, 8);
								if (data.contains(FEED_IDENTIFIER))
									try {
										file.write(tcpPacket.getPayload().getRawData());
									} catch (IOException e) {
										e.printStackTrace();
									}
							}
						}
					}
				}, executorService);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
