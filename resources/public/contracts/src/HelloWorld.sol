pragma solidity ^0.4.10;

contract mortal {
  /* Define variable owner of the type address*/
  address owner;

  /* this function is executed at initialization and sets the owner of the contract */
  function mortal() { owner = msg.sender; }

  /* Function to recover the funds on the contract */
  function kill() { if (msg.sender == owner) selfdestruct(owner); }
}

contract greeter is mortal {
  /* define variable greeting of the type string */
  string greeting;

  /* this runs when the contract is executed */
  function greeter(_greeting) public {
    greeting = "Hello world";
  }

  /* main function */
  function greet() constant returns (string) {
    return "HELLLLOOO";
  }
}
