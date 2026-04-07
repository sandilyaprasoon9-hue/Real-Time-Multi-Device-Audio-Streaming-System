import javax.sound.sampled.*;
import java.net.*;
import java.util.*;

public class AudioProjectMain{   

    static List<String> devices = new ArrayList<>();
    static List<InetAddress> selectedDevices = new ArrayList<>();

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            AudioFormat format = new AudioFormat(48000, 16, 2, true, false);

            // 🔍 Find BlackHole
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            TargetDataLine line = null;

            for (Mixer.Info info : mixers) {
                try {
                    Mixer mixer = AudioSystem.getMixer(info);
                    DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, format);

                    if (mixer.isLineSupported(dataInfo) &&
                        info.getName().toLowerCase().contains("blackhole")) {

                        line = (TargetDataLine) mixer.getLine(dataInfo);
                        line.open(format);
                        line.start();

                        System.out.println("Using BlackHole: " + info.getName());
                        break;
                    }
                } catch (Exception ignored) {}
            }

            if (line == null) {
                System.out.println(" BlackHole not found!");
                return;
            }

            DatagramSocket socket = new DatagramSocket();
            socket.setSendBufferSize(65536);

            int audioPort = 50005;

            // STEP 1: NUMBER OF DEVICES
            System.out.print("🔢 Enter number of devices: ");
            int n = Integer.parseInt(scanner.nextLine());

            // STEP 2: ENTER IPS
            for (int i = 0; i < n; i++) {
                System.out.print(" Enter IP for device " + (i + 1) + ": ");
                String ip = scanner.nextLine();
                devices.add(ip);
            }

            // SHOW DEVICES
            printDevices();

            // STEP 3: SELECT DEVICES
            System.out.print(" Select devices (e.g., 1,2): ");
            String input = scanner.nextLine();

            String[] choices = input.split(",");
            for (String choice : choices) {
                int index = Integer.parseInt(choice.trim()) - 1;

                if (index >= 0 && index < devices.size()) {
                    selectedDevices.add(
                        InetAddress.getByName(devices.get(index))
                    );
                }
            }

            System.out.println(" Streaming to selected devices...");
            System.out.println("Server started\n");

            byte[] buffer = new byte[8192];

            while (true) {
                int bytesRead = line.read(buffer, 0, buffer.length);

                for (InetAddress address : selectedDevices) {
                    DatagramPacket packet =
                            new DatagramPacket(buffer, bytesRead, address, audioPort);

                    socket.send(packet);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printDevices() {
        System.out.println("\n Device List:");
        for (int i = 0; i < devices.size(); i++) {
            System.out.println((i + 1) + " → " + devices.get(i));
        }
    }
}

