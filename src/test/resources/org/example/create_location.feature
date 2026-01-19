Feature: Create Location
  As a station owner
  I want to create new locations
  So that I can assign chargers to them

  # Scenario 1
  Scenario: Create a new location successfully
    # UPDATED LINE BELOW:
    Given the location service is initialized
    When I create a location with ID 1, name "City Center" and address "Main St 1"
    Then the location should be saved
    And the location should have ID 1
    And the location should have name "City Center"
    And the location should have address "Main St 1"

  # Scenario 2
  Scenario: Create multiple locations
    # UPDATED LINE BELOW:
    Given the location service is initialized
    When I create location ID 10 "West Side" at "West Ave"
    And I create location ID 20 "East Side" at "East Ave"
    Then the system should have 2 locations