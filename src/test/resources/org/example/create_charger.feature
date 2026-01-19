Feature: Create Charger
  As a station operator
  I want to add chargers to locations
  So that customers can charge their vehicles

  Scenario: Add a charger to a location
    Given the system is initialized
    And a location with ID 1 exists
    When I add a charger with ID 101 and power 150 kW to location 1
    Then the charger should be added to the location
    And the charger should have ID 101
    And the charger should have power 150 kW
    And the charger should be available

  Scenario: Add multiple chargers to a location
    Given the system is initialized
    And a location with ID 1 exists
    When I add charger ID 101 with 150 kW to location 1
    And I add charger ID 102 with 300 kW to location 1
    Then location 1 should have 2 chargers
