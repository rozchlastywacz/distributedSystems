# Middleware
Zostało zdefiniowanych 8 zadań - 3 aplikacyjne (An) i 5 infrastrukturalnych (Im). Należy zrealizować jedno wybrane zadanie aplikacyjne i jedno (lub więcej) zadanie infrastrukturalne; dodatkowym wymogiem jest by w wybranym zestawie znalazły się zadania (lub ich części) dotyczące (gRPC oraz (Ice lub Thrift)). Każde zadanie ma określoną maksymalną punktację (nominalnie maksimum za całe laboratorium wynosi 15 pkt.).

# Wybrane zadania

## Zadanie A3 - Sprawy urzędowe
Celem zadania jest skonstruowanie aplikacji klient-serwer służącej do zlecania spraw urzędowych i uzyskiwania informacji o ich wyniku. 
Czas rozpatrzenia sprawy przez urząd jest zgrubnie określany już w momencie jej zgłaszania i może wynosić około godziny-dwóch (na potrzeby realizacji i demonstracji zadania należy oczywiście przyjąć, że jest krótszy). 
Zlecający (klient) jest zainteresowany jak najszybszym dowiedzeniem się o wyniku zgłoszonej przez siebie sprawy, jednak spóźnienie rzędu minuty czy dwóch nie będzie stanowić problemu. 
Nie można założyć, że każdy klient będzie chciał mieć uruchomioną aplikację przez cały czas realizacji sprawy.
Urząd obsługuje wiele różnych typów spraw (różniących się zestawem przesyłanych lub zwracanych informacji), można przyjąć, że wszystkie typy spraw są znane w czasie tworzenia systemu. (na potrzeby realizacji zadania wystarczy przewidzieć trzy przykładowe).

Priorytetem w realizacji zadania jest dobór, zaprojektowanie i realizacja właściwego sposobu komunikacji – minimalizującego liczbę niepotrzebnych wywołań. Dopuszczalne są wszystkie eleganckie opcje realizacji komunikacji.

- Technologia middleware: dowolna
- Demonstracja zadania: aplikacja kliencka musi pozwalać na efektywne przetestowanie różnych scenariuszy
- Języki programowania: dwa (jeden dla klienta, drugi dla serwera)
- Maksymalna punktacja: 7


## Zadanie I6. Reverse-proxy technologii gRPC
Celem zadania jest demonstracja (na bardzo prostym przykładzie - można nawet wykorzystać elementy kodu z laboratorium) mechanizmu reverse-proxy technologii gRPC.

Demonstrowane zadanie nie może być wierną kopią rozwiązań znalezionych w Internecie lub w dokumentacji technologii.

- Technologia middleware: gRPC,
- Języki programowania: wystarczy jeden
- Maksymalna punktacja: 8
