package sr.ice.server;

import Office.*;
import com.zeroc.Ice.Current;

import java.util.ArrayList;
import java.util.Arrays;

public class ClerkI implements Clerk {
    private IceServer server;

    public ClerkI(IceServer server) {
        this.server = server;
    }

    @Override
    public void sayHello(PersonData personData, ClientPrx client, Current current) {
        System.out.printf("Hello there! - name: %s, surname: %s, id: %s%n", personData.Name, personData.Surname, personData.id);

        server.clients.put(personData.id, client.ice_fixed(current.con));

        sendNotDelivered(personData);
    }


    @Override
    public long register(PersonData personData, RegistrationRequest request, Current current) {
        System.out.printf("Start registration! - brand: %s, engine: %s, year: %s%n", request.carBrand, request.engine, request.productionYear);
        long timeToWait = 10000;

        new Thread(() -> {
            try {
                Thread.sleep(timeToWait);
                ClientPrx client = server.clients.get(personData.id);
                RegistrationResponse response = new RegistrationResponse(true, "KRA " + request.carBrand.substring(0, 2) + request.productionYear);
                client.registrationCallbackAsync(response)
                        .whenCompleteAsync((msg, ex) -> {
                            if (ex != null) {
                                System.out.printf("register done exceptionally, id: %s%n", personData.id);
                                addToNotDelivered(personData, response);
                            } else {
                                System.out.printf("register done successfully, id: %s%n", personData.id);
                            }
                        });
            } catch (InterruptedException e) {
                System.out.println("exception in register");
            }
        }).start();

        return timeToWait;
    }

    @Override
    public long renewLicense(PersonData personData, RenewLicenseRequest request, Current current) {
        System.out.printf("Start license renew! - category: %s, valid health: %s, validity period: %s%n",
                Arrays.toString(request.categories), request.validHealth, request.validityPeriod);
        long timeToWait = 8000;

        new Thread(() -> {
            try {
                Thread.sleep(timeToWait);
                ClientPrx client = server.clients.get(personData.id);
                LicenseResponse response;
                if(request.validHealth){
                    response = new LicenseResponse(true, "CL " + personData.id.substring(0, 3) + request.categories[0], "Starosta Krakowski");
                }else{
                    response = new LicenseResponse(false, "", "");
                }
                client.licenseCallbackAsync(response)
                        .whenCompleteAsync((msg, ex) -> {
                            if (ex != null) {
                                System.out.printf("licence done exceptionally, id: %s%n", personData.id);
                                addToNotDelivered(personData, response);
                            } else {
                                System.out.printf("licence done successfully, id: %s%n", personData.id);
                            }
                        });
            } catch (InterruptedException e) {
                System.out.println("exception in licence");
            }
        }).start();

        return timeToWait;
    }

    @Override
    public long instructorLicense(PersonData personData, InstructorLicenseRequest request, Current current) {
        System.out.printf("Start license renew! - notConvicted: %s, psychoOK: %s, doneInstructorCourse: %s%n",
                request.notConvicted, request.psychoOK, request.doneInstructorCourse);
        long timeToWait = 15000;

        new Thread(() -> {
            try {
                Thread.sleep(timeToWait);
                ClientPrx client = server.clients.get(personData.id);
                InstructorResponse response;
                if (request.notConvicted && request.psychoOK && request.doneInstructorCourse) {
                    response = new InstructorResponse(true, "CL " + personData.id.substring(0, 3));
                } else {
                    response = new InstructorResponse(false, "");
                }
                client.instructorCallbackAsync(response)
                        .whenCompleteAsync((msg, ex) -> {
                            if (ex != null) {
                                System.out.printf("instructor done exceptionally, id: %s%n", personData.id);
                                addToNotDelivered(personData, response);
                            } else {
                                System.out.printf("instructor done successfully, id: %s%n", personData.id);
                            }
                        });
            } catch (InterruptedException e) {
                System.out.println("exception in instructor");
            }
        }).start();

        return timeToWait;
    }

    private void addToNotDelivered(PersonData personData, Response response) {
        if (server.notDelivered.get(personData.id) != null) {
            server.notDelivered.get(personData.id).add(response);
        } else {
            ArrayList<Response> list = new ArrayList<>();
            list.add(response);
            server.notDelivered.put(personData.id, list);
        }
    }

    private void sendNotDelivered(PersonData personData) {
        if (server.notDelivered.get(personData.id) != null) {
            ClientPrx batchClient = server.clients.get(personData.id).ice_batchOneway();
            for (Response r : server.notDelivered.get(personData.id)) {
                if (r instanceof RegistrationResponse) {
                    batchClient.registrationCallbackAsync((RegistrationResponse) r);
                } else if (r instanceof LicenseResponse) {
                    batchClient.licenseCallbackAsync((LicenseResponse) r);
                } else if (r instanceof InstructorResponse) {
                    batchClient.instructorCallbackAsync((InstructorResponse) r);
                }
            }
            batchClient.ice_flushBatchRequestsAsync().whenCompleteAsync((msg, ex) -> {
                if (ex != null) {
                    System.out.printf("FAILURE - Send old response to client %s%n ", personData.id);
                } else {
                    System.out.printf("SUCCESS - Send old response to client %s%n", personData.id);
                    server.notDelivered.remove(personData.id);
                    System.out.printf("     Removed old responses to client %s%n ", personData.id);
                }
            });
        }
    }

}
