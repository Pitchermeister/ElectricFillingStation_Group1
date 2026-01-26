Feature: Delete Client
  As a system administrator
  I want to delete clients only if allowed
  So that account balances and active charging sessions are protected

  Scenario: Client cannot be deleted when account has credit
    Given the delete client management system is initialized
    And a client "Max" with email "max@test.com" is registered
    And the client has a balance of 10.0
    When I try to delete the client
    Then the client should still exist in the system

  Scenario: Client cannot be deleted when currently charging
    Given the delete client management system is initialized
    And a client "Eva" with email "eva@test.com" is registered
    And the client has an active charging session
    When I try to delete the client
    Then the client should still exist in the system

  Scenario: Client can be deleted when balance is zero and not charging
    Given the delete client management system is initialized
    And a client "Lena" with email "lena@test.com" is registered
    And the client has a finished charging session
    When I try to delete the client
    Then the client should be deleted from the system

  Scenario: Attempt to delete non-existent client (error case)
    Given the delete client management system is initialized
    When I attempt to delete a client with ID 999
    Then an error should be returned for client not found

  Scenario: Client cannot be deleted when account has very small balance (edge case)
    Given the delete client management system is initialized
    And a client "Small" with email "small@test.com" is registered
    And the client has a balance of 0.01
    When I try to delete the client
    Then the client should still exist in the system

  Scenario: Client cannot be deleted when account has large balance (edge case)
    Given the delete client management system is initialized
    And a client "Rich" with email "rich@test.com" is registered
    And the client has a balance of 10000.00
    When I try to delete the client
    Then the client should still exist in the system

  Scenario: Delete multiple eligible clients sequentially (edge case)
    Given the delete client management system is initialized
    And a client "Alice" with email "alice@test.com" is registered with zero balance
    And a client "Bob" with email "bob@test.com" is registered with zero balance
    And a client "Carol" with email "carol@test.com" is registered with zero balance
    When I delete all three clients
    Then no clients should exist in the system

  Scenario: Cannot delete client while multiple active sessions (error case)
    Given the delete client management system is initialized
    And a client "Busy" with email "busy@test.com" is registered
    And the client has 3 active charging sessions
    When I try to delete the client
    Then the client should still exist in the system