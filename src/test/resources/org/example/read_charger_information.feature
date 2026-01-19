Feature: Read Charger Information
  As a customer or operator
  I want to view charger details
  So that I can find suitable charging points

  Scenario: View charger details
    Given the system is initialized
    And a location with ID 1 exists
    And a charger with ID 101 and 150 kW exists at location 1
    When I request information for charger ID 101
    Then I should see charger ID 101
    And I should see power 150 kW
    And I should see the charger status

  Scenario: View charger with pricing
    Given the system is initialized
    And a location with ID 1 exists
    And a charger with ID 101 exists at location 1
    And location 1 has pricing AC €0.45/kWh
    When I request charger information for ID 101
    Then I should see AC price €0.45/kWh
