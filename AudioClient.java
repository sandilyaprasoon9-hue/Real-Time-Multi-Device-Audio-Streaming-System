import javax.sound.sampled.*;
import java.net.*;

public class AudioClient {

    public static void main(String[] args) {
        try {

            String serverIP = "10.48.97.26";

            // Send registration message
            DatagramSocket discoverySocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);

            byte[] msg = "HELLO".getBytes();

            DatagramPacket discoveryPacket =
                    new DatagramPacket(msg, msg.length, serverAddress, 60000);

            discoverySocket.send(discoveryPacket);
            discoverySocket.close();

            System.out.println("Registered with server");

            // Audio format
            AudioFormat format = new AudioFormat(48000, 16, 2, true, false);

            SourceDataLine speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();

            // Audio receiving socket
            DatagramSocket socket = new DatagramSocket(50005);
            socket.setReceiveBufferSize(65536);

            byte[] buffer = new byte[8192];

            System.out.println("Client started... Receiving audio");

            // Small delay for stability
            Thread.sleep(100);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                speakers.write(packet.getData(), 0, packet.getLength());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
