package app;

import model.Admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AdminApp {

    public static void main(String[] argv) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Admin admin = new Admin();

        System.out.println("Enter E to close admin");
        while (true) {
            System.out.println("Enter message sending mode type [A, C, AC] (A-agencies, C-carriers, AC-all): ");
            String modeString = br.readLine();

            if (modeString.contains("AC")) {
                System.out.println("Enter message: ");
                String message = br.readLine();
                admin.sendMessageToAll(message);
            } else if (modeString.contains("A")) {
                System.out.println("Enter message: ");
                String message = br.readLine();
                admin.sendMessageToAgencies(message);
            } else if (modeString.contains("C")) {
                System.out.println("Enter message: ");
                String message = br.readLine();
                admin.sendMessageToCarriers(message);
            } else if(modeString.contains("E")){
                admin.close();
                break;
            }
        }
    }
}
