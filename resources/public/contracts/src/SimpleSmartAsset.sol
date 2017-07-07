pragma solidity ^0.4.10;


contract Greeter {
  function sayHello(string something) constant returns (string) {
    return "Hello";
  }
}


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
  uint usagePrice;
  Beneficiary[] beneficiaries;
  uint totalWeight; // to calculate percentage the beneficiary receive

  // Constructor
  function SimpleSmartAsset(uint _usagePrice,
                            address[] addresses,
                            uint[] weights) {
    owner = msg.sender;
    usagePrice = _usagePrice;

    uint beneficiaryCount = addresses.length;
    for (uint i = 0; i < beneficiaryCount; i++) {

      uint weight = weights[i];

      addBeneficiary(addresses[i], weight);
      totalWeight += weight;
    }
  }

  // Dapp can listen to events
  event BeneficiariesPaid;

  function pay() {
    uint beneficiaryCount = beneficiaries.length;
    for (uint i = 0; i < beneficiaryCount; i++) {

      require(this.balance > usagePrice);

      Beneficiary memory beneficiary = beneficiaries[i]; // memory does not use storage

      uint weight = beneficiary.weight;
      address addr = beneficiary.addr;

      uint percentage = weight / totalWeight; // FIXME: rounding errors when weight is small
      uint amount = percentage * usagePrice;

      addr.transfer(amount);
    }
    BeneficiariesPaid();
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

  // Payable fallback function to receive Ether...
  function() payable {}

}


contract SimpleSmartAssetManager is Mortal, Greeter {

  address owner;

  mapping(string => address) simpleSmartAssets;

  // Constructor
  function SimpleSmartAssetManager() {
    owner = msg.sender;
  }

  // Create smart asset and fund it.
  // When this function is called without Ether the Smart Asset cannot be
  // beneficiaries.
  function createSmartAsset (string name,
                             uint usagePrice,
                             address[] addresses,
                             uint[] weights) payable {

    require(addresses.length == weights.length);
    require(simpleSmartAssets[name] == address(0x0));

    address assetAddress = new SimpleSmartAsset(usagePrice, addresses, weights);

    simpleSmartAssets[name] = assetAddress;
    assetAddress.transfer(msg.value);
  }

  function selfdestructSmartAsset(address addr)
    onlyOwner {
    SimpleSmartAsset(addr).remove();
  }

  function assetUsed(string name) {
    address addr = simpleSmartAssets[name];
    SimpleSmartAsset(addr).pay();
  }

  function remove()
    onlyOwner {
    selfdestruct(msg.sender);
  }

  function() payable {}
}
