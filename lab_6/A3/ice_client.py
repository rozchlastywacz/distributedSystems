import Ice
import sys

slice_dir = Ice.getSliceDir()
if not slice_dir:
    print(sys.argv[0] + ': Slice directory not found.')
    sys.exit(1)

Ice.loadSlice("'-I" + slice_dir + "' slice/office.ice")


import Office


class ClientI(Office.Client):
    def registrationCallback(self, response, current=None):
        if response.success:
            print("SUCCESS: Received - Registration: ", response.carRegistration)
        else:
            print("FAILURE: Registration not completed")

    def instructorCallback(self, response, current=None):
        if response.success:
            print("SUCCESS: Received - Instructor: id: ", response.id)
        else:
            print("FAILURE: Instructor not completed")

    def licenseCallback(self, response, current=None):
        if response.success:
            print("SUCCESS: Received - License: id: ", response.id, "issuer: ", response.issuer)
        else:
            print("FAILURE: License not completed")


with Ice.initialize(sys.argv) as communicator:
    clerk = Office.ClerkPrx.checkedCast(communicator.stringToProxy('Clerk/Anetka : tcp -h 127.0.0.2 -p 10000 -z : udp -h 127.0.0.2 -p 10000 -z'))
    if not clerk:
        print("invalid proxy")
        sys.exit(1)

    #
    # Create an object adapter with no name and no endpoints for receiving callbacks
    # over bidirectional connections.
    #
    adapter = communicator.createObjectAdapter("")

    #
    # Register the callback receiver servant with the object adapter and activate
    # the adapter.
    #
    client_proxy = Office.ClientPrx.uncheckedCast(adapter.addWithUUID(ClientI()))
    adapter.activate()

    #
    # Associate the object adapter with the bidirectional connection.
    #
    clerk.ice_getConnection().setAdapter(adapter)

    #
    # Provide the proxy of the callback receiver object to the server and wait for
    # shutdown.
    #
    data = Office.PersonData("Arkadiusz", "Cwikla", "505abc")
    clerk.sayHello(data, client_proxy)

    while (True):
        text = input("==>")
        if text == "reg":
            time = clerk.register(data, Office.RegistrationRequest("Opel", 2003,
                                  Office.engineType.GAS))
            print("Time to wait: ", time)
        elif text == "ren":
            categories = [Office.licenseCategory.A, Office.licenseCategory.B]
            time = clerk.renewLicense(data, Office.RenewLicenseRequest(True, 5, categories))
            print("Time to wait: ", time)
        elif text == "ren-bad":
            categories = [Office.licenseCategory.A, Office.licenseCategory.B]
            time = clerk.renewLicense(data, Office.RenewLicenseRequest(False, 5, categories))
            print("Time to wait: ", time)
        elif text == "ins":
            time = clerk.instructorLicense(data, Office.InstructorLicenseRequest(True, True, True))
            print("Time to wait: ", time)
        elif text == "ins-bad":
            time = clerk.instructorLicense(data, Office.InstructorLicenseRequest(True, False, True))
            print("Time to wait: ", time)
        elif text == "x":
            break

    communicator.waitForShutdown()
