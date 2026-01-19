Feature: Read Charger Information
  As a customer or operator
  I want to view charger details
  So that I can find suitable charging points

  Scenario: View charger details
    # UPDATED: Unique wording
    Given the charger info service is initialized
    # UPDATED: Unique wording
    And an info-service location with ID 1 exists
    # UPDATED: Unique wording + 150.0 (decimal) for safety
    And an info-service charger with ID 101 and 150.0 kW exists at location 1
    When I request information for charger ID 101
    Then I should see charger ID 101
    # UPDATED: 150.0
    And I should see power 150.0 kW
    And I should see the charger status

  Scenario: View charger with pricing
    # UPDATED: Unique wording
    Given the charger info service is initialized
    And an info-service location with ID 1 exists
    And an info-service charger with ID 101 exists at location 1
    # UPDATED: Unique wording + removed symbols
    And info-service location 1 has pricing AC 0.45 EUR per kWh
    When I request charger information for ID 101
    # UPDATED: Removed symbols
    Then I should see AC price 0.45 EUR per kWh