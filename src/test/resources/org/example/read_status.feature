Feature: Read Status
  As a station operator
  I want to view the status of the entire charging network
  So that I can identify issues and availability

  Scenario: View network status with one location
    Given the status monitoring system is initialized
    And a status monitored location named "City Center" exists with 1 chargers
    When I request the network status
    Then the status should show location "City Center"
    And the status should show 1 chargers
    And charger 101 should show status AVAILABLE

  Scenario: View network status with pricing details
    Given the status monitoring system is initialized
    And a status monitored location named "City Center" exists with 1 chargers
    And location "City Center" has pricing AC 0.45 EUR per kWh
    When I request the network status
    Then the status should show AC price 0.45

  Scenario: View network status with multiple locations
    Given the status monitoring system is initialized
    And the following locations and chargers exist:
      | location    | chargerID |
      | City Center | 101       |
      | City Center | 102       |
      | Airport     | 201       |
      | Highway     | 301       |
    When I request the network status
    Then the status should show 3 locations
    And the status should show 4 total chargers

  Scenario: View charger status when occupied (edge case)
    Given the status monitoring system is initialized
    And a status monitored location named "City Center" exists with 1 chargers
    And a monitoring customer "Alice" exists with balance 50.00 EUR
    And the customer "Alice" is charging on charger 101
    When I request the network status
    Then charger 101 should show status OCCUPIED

  Scenario: Request status for non-existent location (error case)
    Given the status monitoring system is initialized
    When I attempt to request status for location "Mars Base"
    Then an error should be returned for location not found