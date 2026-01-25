Feature: Read Status
  As a customer or operator
  I want to view the network status
  So that I can see available chargers and current prices

  s̈
  Scenario: View status of one location
    Given the status monitoring system is initialized
    And a status monitored location named "City Center" exists with 2 chargers
    And location "City Center" has pricing AC 0.45 EUR per kWh
    When I request the network status
    Then the status should show location "City Center"
    And the status should show AC price 0.45
    And the status should show 2 chargers
    And the status should show charger availability

  Scenario: View status shows occupied chargers
    Given the status monitoring system is initialized
    And a status monitored location named "City Center" exists with 2 chargers
    And a monitoring customer "Alice" exists with balance 50.00 EUR
    And the customer "Alice" is charging on charger 100
    When I request the network status
    Then charger 100 should show status OCCUPIED
    And charger 101 should show status AVAILABLE

  Scenario: View status across multiple locations
    Given the status monitoring system is initialized
    And a status monitored location named "City" exists with 2 chargers
    And a status monitored location named "Airport" exists with 3 chargers
    When I request the network status
    Then the status should show 2 locations
    And the status should show 5 total chargers
