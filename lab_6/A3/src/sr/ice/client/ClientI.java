package sr.ice.client;

import Office.*;
import com.zeroc.Ice.Current;

public class ClientI implements Client {
    @Override
    public void registrationCallback(RegistrationResponse response, Current current) {
        if(response.success){
            System.out.printf("SUCCESS: Received - Registration: %s%n", response.carRegistration);
        }else{
            System.out.println("FAILURE: Registration not completed");
        }
    }

    @Override
    public void instructorCallback(InstructorResponse response, Current current) {
        if(response.success){
            System.out.printf("SUCCESS: Received - Instructor: id:%s%n", response.id);
        }else{
            System.out.println("FAILURE: Instructor not completed");
        }
    }

    @Override
    public void licenseCallback(LicenseResponse response, Current current) {
        if(response.success){
            System.out.printf("SUCCESS: Received - License: id:%s, issuer:%s%n", response.id, response.issuer);
        }else{
            System.out.println("FAILURE: License not completed");
        }
    }
}
