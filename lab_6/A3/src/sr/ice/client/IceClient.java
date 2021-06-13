package sr.ice.client;

import Office.*;
import com.zeroc.Ice.*;

import java.io.IOException;
import java.lang.Exception;
import java.util.concurrent.CompletableFuture;

public class IceClient {
    public static void main(String[] args) {
        int status = 0;
        Communicator communicator = null;

        try {
            communicator = Util.initialize(args);

            ObjectPrx server = communicator.propertyToProxy("Clerk.Proxy");

            ClerkPrx clerk = ClerkPrx.checkedCast(server);
            if (clerk == null) throw new Error("Invalid proxy");

            ObjectAdapter adapter = communicator.createObjectAdapter("");
            ClientPrx clientPrx = ClientPrx.uncheckedCast(adapter.addWithUUID(new ClientI()));
            adapter.activate();
            clerk.ice_getConnection().setAdapter(adapter);

            CompletableFuture<Long> cfl = null;
            String line = null;
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

            PersonData personData = new PersonData("Arkadiusz", "Cwikla", "505abc");

            clerk.sayHello(personData, clientPrx);

            do {
                try {
                    System.out.println("==> ");
                    line = in.readLine();
                    if (line.equals("reg")) {
                        long time = clerk.register(
                                personData,
                                new RegistrationRequest("Opel", (short) (2003), engineType.GAS)
                        );
                        System.out.printf("Time to wait: %s [ms]%n", time);
                    } else if (line.equals("ren")) {
                        licenseCategory[] categories = {licenseCategory.A, licenseCategory.B};

                        long time = clerk.renewLicense(
                                personData,
                                new RenewLicenseRequest(true, (short) 5, categories)
                        );
                        System.out.printf("Time to wait: %s [ms]%n", time);
                    } else if (line.equals("ren-bad")) {
                        licenseCategory[] categories = {licenseCategory.A, licenseCategory.B};

                        long time = clerk.renewLicense(
                                personData,
                                new RenewLicenseRequest(false, (short) 5, categories)
                        );
                        System.out.printf("Time to wait: %s [ms]%n", time);
                    } else if (line.equals("ins")) {
                        long time = clerk.instructorLicense(
                                personData,
                                new InstructorLicenseRequest(true, true, true)
                        );
                        System.out.printf("Time to wait: %s [ms]%n", time);
                    } else if (line.equals("ins-bad")) {
                        long time = clerk.instructorLicense(
                                personData,
                                new InstructorLicenseRequest(true, false, true)
                        );
                        System.out.printf("Time to wait: %s [ms]%n", time);
                    }
                } catch (IOException | TwowayOnlyException ex) {
                    System.err.println(ex);
                }
            }
            while (!line.equals("x"));


        } catch (LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;
        }
        if (communicator != null) { //clean
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }

}