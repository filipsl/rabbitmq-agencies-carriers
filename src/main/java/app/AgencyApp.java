package app;

import model.Agency;
import services.ServiceType;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AgencyApp {

    public static void main(String[] argv) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter agency name: ");
        String name = br.readLine();

        Agency agency = new Agency(name);

        while (true) {
            System.out.println("Enter service type [P, C, S] (P-people, C-cargo, S-satellites): ");
            String serviceString = br.readLine();
            char serviceChar = serviceString.charAt(0);
            switch (serviceChar) {
                case 'P':
                    agency.addTask(ServiceType.PEOPLE);
                    break;
                case 'C':
                    agency.addTask(ServiceType.CARGO);
                    break;
                case 'S':
                    agency.addTask(ServiceType.SATELLITES);
                    break;
            }
        }
    }
}
