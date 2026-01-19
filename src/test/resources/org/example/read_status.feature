Feature: Read Status
  As a customer or operator
  I want to view the network status
  So that I can see available chargers and current prices

  Scenario: View status of one location
    Given the system is initialized
    And a location with ID 1 "City Center" exists with 2 chargers
    And location 1 has pricing AC €0.45/kWh
    When I request the network status
    Then the status should show location "City Center"
    And the status should show AC price €0.45/kWh
    And the status should show 2 chargers
    And the status should show charger availability

  Scenario: View status shows occupied chargers
    Given the system is initialized
    And a location with ID 1 exists with 2 chargers
    And a client with ID 1 exists with balance €50.00
    And the client is charging on charger 100
    When I request the network status
    Then charger 100 should show status OCCUPIED
    And charger 101 should show status AVAILABLE

  Scenario: View status across multiple locations
    Given the system is initialized
    And a location with ID 1 "City" exists with 2 chargers
    And a location with ID 2 "Airport" exists with 3 chargers
    When I request the network status
    Then the status should show 2 locations
    And the status should show 5 total chargers
