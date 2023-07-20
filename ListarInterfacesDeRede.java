import java.net.*;
import java.util.Enumeration;

public class ListarInterfacesDeRede {
    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            System.out.println("Nome da interface: " + networkInterface.getName());
            System.out.println("Endereço MAC: " + getMACAddress(networkInterface));
            System.out.println("Endereços IP: " + getIPAddresses(networkInterface));
            System.out.println("---------------------------");
        }
    }

    private static String getMACAddress(NetworkInterface networkInterface) throws SocketException {
        byte[] mac = networkInterface.getHardwareAddress();
        if (mac == null) return "Não disponível";
        StringBuilder sb = new StringBuilder();
        for (byte b : mac) {
            sb.append(String.format("%02X:", b));
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String getIPAddresses(NetworkInterface networkInterface) {
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        StringBuilder sb = new StringBuilder();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address instanceof Inet4Address) {
                sb.append(address.getHostAddress()).append(", ");
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }
}
