#ifndef OFF_ICE
#define OFF_ICE

module Office
{

  exception NoInput {};

  struct PersonData{
    string Name;
    string Surname;
    string id;
  };

  class Response{
    bool success;
  };

  enum engineType { GAS, DIESEL, ELECTRIC };
  struct RegistrationRequest{
    string carBrand;
    short productionYear;
    engineType engine;
  };
  class RegistrationResponse extends Response{
    string carRegistration;
  };

  enum licenseCategory {A, B, C, D};
  sequence<licenseCategory> categories;
  struct RenewLicenseRequest{
    bool validHealth;
    short validityPeriod;
    categories categories;
  };
  class LicenseResponse extends Response{
    string id;
    string issuer;
  };

  struct InstructorLicenseRequest{
    bool notConvicted;
    bool psychoOK;
    bool doneInstructorCourse;
  };
  class InstructorResponse extends Response{
    string id;
  };

  interface Client{
    void registrationCallback(RegistrationResponse response);
    void licenseCallback(LicenseResponse response);
    void instructorCallback(InstructorResponse response);
  };

  interface Clerk{
    void sayHello(PersonData personData, Client* client);
    long register(PersonData personData, RegistrationRequest request);
    long renewLicense(PersonData personData, RenewLicenseRequest request);
    long instructorLicense(PersonData personData, InstructorLicenseRequest request);
  };

};

#endif
