Feature: Read Invoice
  As a customer
  I want to view my invoice history
  So that I can track my charging expenses

  Scenario: View invoice after charging
    Given the invoice service is initialized
    And an invoicing customer "Alice" exists with balance 100.00 EUR
    And a charging location named "City Center" exists with 1 charger
    And "Alice" completes a charging session at "City Center"
    When "Alice" requests their invoice
    #!!!Details nochmal überprüfen!!! hier war vorher auskommentiert, dass uns die details fehlen
    Then the invoice should list 1 charging sessions
    And the invoice should include location "City Center"
    And the invoice should include charged energy 12.50 kWh
    And the invoice should include a total price

  Scenario: Invoice sorted by start time
    Given the invoice service is initialized
    And an invoicing customer "Alice" exists with balance 100.00 EUR
    And a charging location named "City Center" exists with 1 charger
    And "Alice" completes 3 charging sessions at different times at "City Center"
    When "Alice" requests their invoice
    Then the invoice sessions should be sorted chronologically
    And the invoice should show 3 items

  Scenario: Invoice shows balance information
    Given the invoice service is initialized
    And an invoicing customer "Alice" exists
    And "Alice" has topped up 100.00 EUR total
    And "Alice" has spent 30.00 EUR on charging
    When "Alice" requests their invoice
    Then the invoice should show total top-ups 100.00 EUR
    And the invoice should show total spent 30.00 EUR
    And the invoice should show remaining balance 70.00 EUR

  Scenario: Invoice shows session with minimal energy charged (edge case)
    Given the invoice service is initialized
    And an invoicing customer "Alice" exists with balance 100.00 EUR
    And a charging location named "City Center" exists with 1 charger
    And "Alice" completes a charging session with 0.1 kWh at "City Center"
    When "Alice" requests their invoice
    Then the invoice should list 1 charging sessions
    And the invoice should include charged energy 0.1 kWh

  Scenario: Invoice shows multiple locations (edge case)
    Given the invoice service is initialized
    And an invoicing customer "Alice" exists with balance 200.00 EUR
    And a charging location named "City Center" exists with 1 charger
    And a charging location named "Airport" exists with 1 charger
    And "Alice" completes a charging session at "City Center"
    And "Alice" completes a charging session at "Airport"
    When "Alice" requests their invoice
    Then the invoice should list 2 charging sessions
    And the invoice should include location "City Center"
    And the invoice should include location "Airport"

  Scenario: Invoice customer balance becomes negative after charging (edge case)
    Given the invoice service is initialized
    And an invoicing customer "Alice" exists with balance 100.00 EUR
    And a charging location named "City Center" exists with 1 charger
    And "Alice" completes multiple charging sessions at "City Center"
    When "Alice" requests their invoice
    Then the invoice should show all sessions
    And the invoice should correctly show the balance impact