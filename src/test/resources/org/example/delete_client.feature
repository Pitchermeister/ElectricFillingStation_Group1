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
