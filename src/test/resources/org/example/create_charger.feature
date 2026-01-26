Feature: Create Charger
  As a station operator
  I want to add chargers to locations
  So that customers can charge their vehicles

  Scenario: Add a charger to a location
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a charger with ID 101 and power 150.0 kW to location "City Center"
    Then the new charger should be registered in location "City Center"
    And the new charger should have ID 101
    And the new charger should have power 150.0 kW
    And the new charger status should be IN_OPERATION_FREE

  Scenario: Add multiple chargers to a location
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a specific charger ID 101 with 150.0 kW to location "City Center"
    And I add a specific charger ID 102 with 300.0 kW to location "City Center"
    Then location "City Center" should contain 2 chargers
  Scenario: Add charger with minimum power (edge case)
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a charger with ID 103 and power 1.0 kW to location "City Center"
    Then the new charger should be registered in location "City Center"
    And the new charger should have power 1.0 kW

  Scenario: Add charger with very high power (edge case)
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a charger with ID 104 and power 1000.0 kW to location "City Center"
    Then the new charger should be registered in location "City Center"
    And the new charger should have power 1000.0 kW

  Scenario: Add multiple chargers with same power (edge case)
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a charger with ID 105 and power 150.0 kW to location "City Center"
    And I add a charger with ID 106 and power 150.0 kW to location "City Center"
    Then location "City Center" should contain 2 chargers

  Scenario: Attempt to add charger to non-existent location (error case)
    Given the station manager is initialized
    When I attempt to add a charger with ID 107 and power 150.0 kW to location "NonExistent"
    Then the charger should not be registered
    And the system should show an error for non-existent location