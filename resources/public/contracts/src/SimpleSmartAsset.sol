pragma solidity ^0.4.10;


contract Owned {

  address owner;

  function owned() {
    owner = msg.sender;
  }

  modifier onlyOwner {
    require(msg.sender == owner);
    _;
  }
}


contract Mortal is Owned {
  function remove() onlyOwner {
    selfdestruct(owner);
  }
}


contract SimpleSmartAsset is Mortal {

  address owner;
  Beneficiary[] beneficiaries;

  // Constructor
  function SimpleSmartAsset(address[] addresses,
                            uint[] weights) {
    owner = msg.sender;

    uint beneficiaryCount = addresses.length;
    for (uint i = 0; i < beneficiaryCount; i++) {
      addBeneficiary(addresses[i], weights[i]);
    }
  }

  struct Beneficiary {
    address addr;
    uint weight;
  }

  function addBeneficiary(address addr, uint weight) {
    beneficiaries.push(Beneficiary({
        addr: addr,
        weight: weight
    }));
  }

}


contract SimpleSmartAssetManager is Mortal {

  address owner;

  mapping(string => address) simpleSmartAssets;

  // Constructor
  function SimpleSmartAssetManager() {
    owner = msg.sender;
  }

  function createSmartAsset(string name,
                            address[] addresses,
                            uint[] weights) {

    require(addresses.length == weights.length);
    require(simpleSmartAssets[name] == address(0x0));

    simpleSmartAssets[name] =
      new SimpleSmartAsset(addresses, weights);
  }

  function sayHello() constant returns (string) {
    return "HELLO";
  }

  function selfdestructSmartAsset(address addr)
    onlyOwner {
    SimpleSmartAsset(addr).remove();
  }

  function remove()
    onlyOwner {
    selfdestruct(msg.sender);
  }
}
