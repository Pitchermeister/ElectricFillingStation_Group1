Feature: Create Charger
  As a station operator
  I want to add chargers to locations
  So that customers can charge their vehicles

  Scenario: Add a charger to a location
    # Made unique:
    Given the station manager is initialized
    And a target location with ID 1 exists

    # 150.0 fixed here:
    When I add a charger with ID 101 and power 150.0 kW to location 1

    # Made unique:
    Then the new charger should be registered in the location
    And the new charger should have ID 101
    And the new charger should have power 150.0 kW
    And the new charger status should be IN_OPERATION_FREE

  Scenario: Add multiple chargers to a location
    # Made unique:
    Given the station manager is initialized
    And a target location with ID 1 exists

    # Made unique:
    When I add a specific charger ID 101 with 150.0 kW to location 1
    And I add a specific charger ID 102 with 300.0 kW to location 1
    Then location 1 should contain 2 chargers