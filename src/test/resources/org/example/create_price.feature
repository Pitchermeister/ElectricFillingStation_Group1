Feature: Create Price
  As a station operator
  I want to set prices for charging
  So that I can bill customers correctly

  Scenario: Set initial pricing for a location
    Given the system is initialized
    And a location with ID 1 exists with 1 charger
    When I set pricing for location 1: AC €0.45/kWh, DC €0.65/kWh, €0.20/min
    Then location 1 should have AC price €0.45/kWh
    And location 1 should have DC price €0.65/kWh
    And all chargers at location 1 should have these prices

  Scenario: Different locations have different prices
    Given the system is initialized
    And a location with ID 1 exists with 1 charger
    And a location with ID 2 exists with 1 charger
    When I set location 1 pricing: AC €0.40/kWh
    And I set location 2 pricing: AC €0.50/kWh
    Then location 1 should have AC price €0.40/kWh
    And location 2 should have AC price €0.50/kWh
