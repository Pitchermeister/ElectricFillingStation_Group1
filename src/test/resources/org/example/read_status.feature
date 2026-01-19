Feature: Read Status
  As a customer or operator
  I want to view the network status
  So that I can see available chargers and current prices

  Scenario: View status of one location
    # UPDATED: Unique wording
    Given the status monitoring system is initialized
    # UPDATED: Unique wording + removed symbol
    And a status monitored location with ID 1 "City Center" exists with 2 chargers
    And status location 1 has pricing AC 0.45 EUR per kWh
    When I request the network status
    Then the status should show location "City Center"
    And the status should show AC price 0.45
    And the status should show 2 chargers
    And the status should show charger availability

  Scenario: View status shows occupied chargers
    # UPDATED: Unique wording
    Given the status monitoring system is initialized
    And a status monitored location with ID 1 exists with 2 chargers
    And a monitoring client with ID 1 exists with balance 50.00 EUR
    And the client is charging on charger 100
    When I request the network status
    Then charger 100 should show status OCCUPIED
    And charger 101 should show status AVAILABLE

  Scenario: View status across multiple locations
    # UPDATED: Unique wording
    Given the status monitoring system is initialized
    And a status monitored location with ID 1 "City" exists with 2 chargers
    And a status monitored location with ID 2 "Airport" exists with 3 chargers
    When I request the network status
    Then the status should show 2 locations
    And the status should show 5 total chargers