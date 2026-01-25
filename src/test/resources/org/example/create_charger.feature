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
