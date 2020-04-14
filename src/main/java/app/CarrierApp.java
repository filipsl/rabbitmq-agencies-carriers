package app;

import model.Carrier;
import services.ServiceType;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CarrierApp {

    public static void main(String[] argv) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter carrier name: ");
        String name = br.readLine();

        boolean first_valid = false;
        boolean second_valid = false;

        ServiceType serviceType1 = null;
        ServiceType serviceType2 = null;

        while (!first_valid) {
            System.out.println("Enter first service type [P, C, S] (P-people, C-cargo, S-satellites): ");
            String serviceString = br.readLine();
            char serviceChar = serviceString.charAt(0);
            switch (serviceChar) {
                case 'P':
                    serviceType1 = ServiceType.PEOPLE;
                    first_valid = true;
                    break;
                case 'C':
                    serviceType1 = ServiceType.CARGO;
                    first_valid = true;
                    break;
                case 'S':
                    serviceType1 = ServiceType.SATELLITES;
                    first_valid = true;
                    break;
            }
        }

        while (!second_valid) {
            System.out.println("Enter second service type [P, C, S] (P-people, C-cargo, S-satellites): ");
            String serviceString = br.readLine();
            char serviceChar = serviceString.charAt(0);
            switch (serviceChar) {
                case 'P':
                    serviceType2 = ServiceType.PEOPLE;
                    break;
                case 'C':
                    serviceType2 = ServiceType.CARGO;
                    break;
                case 'S':
                    serviceType2 = ServiceType.SATELLITES;
                    break;
            }
            if (serviceType2 != null) {
                if (serviceType1 == serviceType2) {
                    System.out.println("Second service must be different than the first.");
                } else {
                    second_valid = true;
                }
            }
        }


        Carrier carrier = new Carrier(name, serviceType1, serviceType2);
        carrier.start();

        System.out.println("Enter E to close carrier");

        boolean isWorking = true;

        while (isWorking) {
            String serviceString = br.readLine();
            char serviceChar = serviceString.charAt(0);
            if (serviceChar == 'E') {
                carrier.close();
                isWorking = false;
            }
        }

    }
}