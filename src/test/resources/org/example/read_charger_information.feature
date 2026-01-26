Feature: Read Charger Information
  As a customer or operator
  I want to view charger details
  So that I can find suitable charging points

  Scenario: View charger details
    Given the charger info service is initialized
    And an info-service location named "City Center" exists
    And an info-service charger with ID 101 and 150.0 kW exists at location "City Center"
    When I request information for charger ID 101
    Then I should see charger ID 101
    And I should see power 150.0 kW
    And I should see the charger status

  Scenario: View charger with pricing
    Given the charger info service is initialized
    And an info-service location named "City Center" exists
    And an info-service charger with ID 101 exists at location "City Center"
    And info-service location "City Center" has pricing AC 0.45 EUR per kWh
    When I request charger information for ID 101
    Then I should see AC price 0.45 EUR per kWh

  Scenario: Cannot retrieve information for non-existent charger (error case)
    Given the charger info service is initialized
    And an info-service location named "City Center" exists
    When I request information for charger ID 999
    Then an error should be returned for charger not found

  Scenario: View charger status when occupied (edge case)
    Given the charger info service is initialized
    And an info-service location named "City Center" exists
    And an info-service charger with ID 101 and 150.0 kW exists at location "City Center"
    And the charger is currently occupied by a charging session
    When I request information for charger ID 101
    Then I should see charger ID 101
    And I should see the charger status as OCCUPIED

  Scenario: View charger without pricing configured (edge case)
    Given the charger info service is initialized
    And an info-service location named "City Center" exists
    And an info-service charger with ID 102 and 200.0 kW exists at location "City Center"
    And location "City Center" has no pricing configured
    When I request information for charger ID 102
    Then I should see charger ID 102
    And the pricing information should indicate "Not configured"
